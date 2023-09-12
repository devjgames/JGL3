#version 150

in vec2 fsInTextureCoordinate;
in vec4 fsInColor;

out vec4 fsOutColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

void main() {
    vec4 color = fsInColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    fsOutColor = color;
}