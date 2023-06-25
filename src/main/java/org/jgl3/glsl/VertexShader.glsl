#version 150

#define MAX_LIGHTS 16

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec2 vsInTextureCoordinate2;
in vec3 vsInNormal;
in vec4 vsInColor;

out vec2 fsInTextureCoordinate;
out vec2 fsInTextureCoordinate2;
out vec4 fsInColor;

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform int uLightCount;
uniform int uLightingEnabled;
uniform int uVertexColorEnabled;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;
uniform vec4 uColor;

uniform vec3 uWarpAmplitude;
uniform float uWarpTime;
uniform int uWarp;
uniform float uWarpFrequency;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

uniform int uTextureUnit;

void main() {
    vec3 p = vsInPosition;

    if(uWarp != 0) {
        float f = uWarpFrequency;
        vec3 b = p;

        p.x = b.x + uWarpAmplitude.x * sin(f * b.z + uWarpTime) * cos(f * b.y + uWarpTime);
        p.y = b.y + uWarpAmplitude.y * cos(f * b.x + uWarpTime) * sin(f * b.z + uWarpTime);
        p.z = b.z + uWarpAmplitude.z * sin(f * b.y + uWarpTime) * cos(f * b.x + uWarpTime);
    }

    vec4 position = uModel * vec4(p, 1.0);
    vec4 color = uColor;

    if(uVertexColorEnabled != 0) {
        color *= vsInColor;
    }

    if(uLightingEnabled != 0) {
        vec3 normal = normalize((uModelIT * vec4(vsInNormal, 0.0)).xyz);

        color = uAmbientColor;
        for(int i = 0; i != uLightCount; i++) {
            vec3 lightOffset = uLightPosition[i] - position.xyz;
            vec3 lightNormal = normalize(lightOffset);
            float lDotN = clamp(dot(lightNormal, normal), 0.0, 1.0);
            float atten = 1.0 - clamp(length(lightOffset) / uLightRadius[i], 0.0, 1.0);

            color += atten * lDotN *  uDiffuseColor * uLightColor[i];
        }
    }

    vec2 coord = vsInTextureCoordinate;

    if(uTextureUnit > 0) {
        vec3 n = abs(vsInNormal);
        vec3 p = vsInPosition;

        if(n.x >= n.y && n.x >= n.z) {
            coord = vec2(p.z, p.y) / float(uTextureUnit);
        } else if(n.y >= n.x && n.y >= n.z) {
            coord = vec2(p.x, p.z) / float(uTextureUnit);
        } else {
            coord = vec2(p.x, p.y) / float(uTextureUnit);
        }
    }

    fsInTextureCoordinate = coord;
    fsInTextureCoordinate2 = vsInTextureCoordinate2;
    fsInColor = color;

    gl_Position = uProjection * uView * position;
}