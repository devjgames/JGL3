#version 150

in vec2 fsInTextureCoordinate;
in vec2 fsInTextureCoordinate2;
in vec4 fsInColor;

out vec4 fsOutColor;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uTexture2;
uniform int uTexture2Enabled;

void main() {
    vec4 color = fsInColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    if(uTexture2Enabled != 0) {
        color *= texture(uTexture2, fsInTextureCoordinate2);
    }
    fsOutColor = color;
}