#version 150

in vec2 fsInTextureCoordinate;
in vec2 fsInTextureCoordinate2;
in vec4 fsInColor;

out vec4 fsOutColor1;
out vec4 fsOutColor2;
out vec4 fsOutColor3;

uniform sampler2D uTexture;
uniform int uTextureEnabled;

uniform sampler2D uTexture2;
uniform int uTexture2Enabled;

uniform vec4 uLayerColor;

void main() {
    vec4 color = fsInColor;

    if(uTextureEnabled != 0) {
        color *= texture(uTexture, fsInTextureCoordinate);
    }
    if(uTexture2Enabled != 0) {
        color *= texture(uTexture2, fsInTextureCoordinate2);
    }
    fsOutColor1 = color;
    fsOutColor2 = uLayerColor;
    if(uLayerColor.a > 0.1) {
        fsOutColor3 = color;
    } else {
        fsOutColor3 = vec4(0.0, 0.0, 0.0, 0.0);
    }
}