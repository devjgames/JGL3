#version 150

in vec2 vsInPosition;

out vec2 fsInTextureCoordinate;

void main() {
    fsInTextureCoordinate = vsInPosition * 0.5 + vec2(0.5, 0.5);
    
    gl_Position = vec4(vsInPosition, 0.0, 1.0);
}