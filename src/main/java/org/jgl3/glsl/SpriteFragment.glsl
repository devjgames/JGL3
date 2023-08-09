#version 150

in vec2 fsInTextureCoordinate;
in vec4 fsInColor;

out vec4 fsOutColor;

uniform sampler2D uTexture;

void main() {
    fsOutColor = fsInColor * texture(uTexture, fsInTextureCoordinate);
}