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

uniform vec3 uLightPosition[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRadius[MAX_LIGHTS];

uniform int uLightCount;
uniform int uLightingEnabled;
uniform int uVertexColorEnabled;

uniform vec4 uAmbientColor;
uniform vec4 uDiffuseColor;
uniform vec4 uColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

void main() {
    vec4 position = uModel * vec4(vsInPosition, 1.0);
    vec4 color = uColor;

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