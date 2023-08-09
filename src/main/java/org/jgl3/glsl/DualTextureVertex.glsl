#version 150

in vec3 vsInPosition;
in vec2 vsInTextureCoordinate;
in vec2 vsInTextureCoordinate2;

out vec2 fsInTextureCoordinate;
out vec2 fsInTextureCoordinate2;

uniform mat4 uProjection;
uniform mat4 uView;
uniform mat4 uModel;

void main() {
    fsInTextureCoordinate = vsInTextureCoordinate;
    fsInTextureCoordinate2 = vsInTextureCoordinate2;

    gl_Position = uProjection * uView * uModel * vec4(vsInPosition, 1.0);
}