#version 150

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec4 vsInColor;

out vec2 fsInTextureCoordinate;
out vec4 fsInColor;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    gl_Position = uProjection * uView * uModel * vec4(vsInPosition, 1.0);
    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInColor = vsInColor;
}
