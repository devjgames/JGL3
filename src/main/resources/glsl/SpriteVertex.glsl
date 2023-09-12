#version 150

in vec2 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec4 vsInColor;

out vec2 fsInTextureCoordinate;
out vec4 fsInColor;

uniform mat4 uProjection;

void main() {

    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInColor = vsInColor;

    gl_Position = uProjection * vec4(vsInPosition, 0.0, 1.0);
}