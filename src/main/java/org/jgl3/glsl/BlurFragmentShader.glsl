#version 150

#define SAMPLE_COUNT 8

in vec2 fsInTextureCoordinate;

out vec4 fsOutColor;

uniform sampler2D uTexture;
uniform vec2 uOffset[SAMPLE_COUNT];
uniform vec2 uPixelSize;

void main() {
    vec4 color = texture(uTexture, fsInTextureCoordinate);

    for(int i = 0; i != SAMPLE_COUNT; i++) {
        color += texture(uTexture, fsInTextureCoordinate - uOffset[i] * uPixelSize);
        color += texture(uTexture, fsInTextureCoordinate + uOffset[i] * uPixelSize);
    }

    fsOutColor = color /= float(SAMPLE_COUNT * 2 + 1);
}