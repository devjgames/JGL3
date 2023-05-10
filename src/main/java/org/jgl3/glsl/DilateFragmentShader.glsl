#version 150

in vec2 fsInTextureCoordinate;

out vec4 fsOutColor;

uniform sampler2D uTexture;
uniform float uWidth;
uniform vec2 uPixelSize;

void main() {
    vec4 color = texture(uTexture, fsInTextureCoordinate);

    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(-uWidth, 0.0) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(+uWidth, 0.0) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(0.0, -uWidth) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(0.0, +uWidth) * uPixelSize));

    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(-uWidth, -uWidth) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(+uWidth, +uWidth) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(+uWidth, -uWidth) * uPixelSize));
    color = max(color, texture(uTexture, fsInTextureCoordinate + vec2(-uWidth, +uWidth) * uPixelSize));

    if(color.a > 0.1) {
        color.a = 1.0;
    } else {
        color.a = 0.0;
    }

    fsOutColor = color;
}