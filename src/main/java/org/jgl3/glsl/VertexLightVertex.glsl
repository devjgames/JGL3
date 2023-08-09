#version 150

#define MAX_LIGHTS 16

#define AMBIENT_LIGHT 0
#define DIRECTIONAL_LIGHT 1
#define POINT_LIGHT 2

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec3 vsInNormal;

out vec2 fsInTextureCoordinate;
out vec4 fsInColor;

uniform vec3 uLightVector[MAX_LIGHTS];
uniform vec4 uLightColor[MAX_LIGHTS];
uniform float uLightRange[MAX_LIGHTS];
uniform int uLightType[MAX_LIGHTS];

uniform int uLightCount;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;
uniform mat4 uModelIT;

uniform vec4 uColor;

void main() {
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
    vec4 position = uModel * vec4(vsInPosition, 1.0);
    vec3 normal = normalize((uModelIT * vec4(vsInNormal, 0.0)).xyz);

    for(int i = 0; i != uLightCount; i++) {
        int type = uLightType[i];

        if(type == AMBIENT_LIGHT) {
            color += uLightColor[i];
        } else if(type == DIRECTIONAL_LIGHT) {
            color += uLightColor[i] * uColor * clamp(dot(normalize(-uLightVector[i]), normal), 0.0, 1.0);
        } else {
            vec3 lightOffset = uLightVector[i] - position.xyz;
            vec3 lightNormal = normalize(lightOffset);
            float lDotN = clamp(dot(lightNormal, normal), 0.0, 1.0);
            float atten = 1.0 - clamp(length(lightOffset) / uLightRange[i], 0.0, 1.0);

            color += uLightColor[i] * uColor * lDotN * atten;
        }
    }
    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInColor = color;

    gl_Position = uProjection * uView * position;
}
