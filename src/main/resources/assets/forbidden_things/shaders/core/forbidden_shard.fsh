#version 150

uniform vec4 ColorModulator;
uniform float GameTime;

in vec4 vertexColor;
in vec2 seedCoord;

out vec4 fragColor;

float hash(float x) {
    return fract(sin(x * 127.1) * 43758.5453);
}

void main() {
    float seed = seedCoord.x;     // seed(0-1)
    float t = GameTime * 12000.0;  // Speed

    float blink = step(0.4, hash(seed + floor(t)));
    if (blink < 0.5) {
        discard;
    }

    float alpha = 0.45 + 0.55 * hash(seed + floor(t * 1.7) + 9.0);
    fragColor = vec4(vertexColor.rgb, alpha) * ColorModulator;
}
