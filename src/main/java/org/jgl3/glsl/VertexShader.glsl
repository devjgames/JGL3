#version 150

#define MAX_LIGHTS 8

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec2 vsInTextureCoordinate2;
in vec3 vsInNormal;
in vec4 vsInColor;

out vec2 fsInTextureCoordinate;
out vec2 fsInTextureCoordinate2;
out vec4 fsInColor;
out vec2 fsInN;

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

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModelViewMatrix;
uniform mat4 uNormalMatrix;
uniform mat4 uModel;
uniform mat4 uModelIT;

void main() {
    vec3 p = vsInPosition;

    if(uWarp != 0) {
        p.x += uWarpAmplitude.x * sin(0.05 * p.z + uWarpTime) * cos(0.05 * p.y + uWarpTime);
        p.y += uWarpAmplitude.y * cos(0.05 * p.x + uWarpTime) * sin(0.05 * p.z + uWarpTime);
        p.z += uWarpAmplitude.z * sin(0.05 * p.x + uWarpTime) * cos(0.05 * p.y + uWarpTime);
    }

    vec4 position = uModel * vec4(p, 1.0);
    vec4 color = uColor;
    vec3 e = normalize((uModelViewMatrix * vec4(p, 1.0)).xyz);
    vec3 n = normalize((uNormalMatrix * vec4(vsInNormal, 0.0)).xyz);
    vec3 r = reflect(e, n);
    
    fsInN = r.xy / (sqrt(r.x * r.x + r.y * r.y + pow(r.z + 1.0, 2.0)) * 2.0) + vec2(0.5, 0.5);
    fsInN.y = 1.0 - fsInN.y;

    if(uVertexColorEnabled != 0) {
        color = vsInColor;
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
    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInTextureCoordinate2 = vsInTextureCoordinate2;
    fsInColor = color;

    gl_Position = uProjection * uView * position;
}