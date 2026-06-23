#version 150

uniform sampler2D Sampler0;   // Atlas (mask shape)
uniform sampler2D Sampler1;   // MISSING_TEXTURE (content)

uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec4 SpriteUV;        // (u0, v0, u1, v1)

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const float TILES   = 4.0;
const float SPEED   = 240.0;
// glitch gain
const float SLICES        = 12.0;   // Number of slices
const float SLICE_AMT     = 0.20;   // Slice amount (tiles)
const float RGB_AMT       = 0.06;   // RGB split
const float RGB_VIB       = 0.012;  // RGB split vibration magnitude (small)
const float RGB_VIB_SPEED = 40.0;   // RGB split vibration speed
const float SCANLINE      = 0.25;   // Thickness of scanline
const float FLICKER       = 0.15;   // Flicker width

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

vec3 sampleMiss(vec2 muv) {
    return texture(Sampler1, fract(muv)).rgb;
}

void main() {
    // mask with texture
    float a = texture(Sampler0, texCoord0).a;
    if (a < 0.1) {
        discard;
    }

    vec2 local = (texCoord0 - SpriteUV.xy) / max(SpriteUV.zw - SpriteUV.xy, vec2(1e-6));
    float t = GameTime * SPEED;

    // basic scroll
    vec2 base = vec2(t, t * 0.6) + 0.5 * vec2(sin(t * 1.4), cos(t * 1.8));

    // (1) slice
    float row = floor(local.y * SLICES);
    float sliceShift = (hash(vec2(row, floor(t * 3.0))) - 0.5) * 2.0 * SLICE_AMT;

    vec2 uv = local * TILES + base + vec2(sliceShift * TILES, 0.0);

    // (2) RGB split with small time-based vibration (fringe shakes)
    vec2 vib = RGB_VIB * vec2(sin(t * RGB_VIB_SPEED), cos(t * RGB_VIB_SPEED * 1.7));
    vec3 col;
    col.r = sampleMiss(uv + vec2( RGB_AMT, 0.0) + vib).r;
    col.g = sampleMiss(uv).g;
    col.b = sampleMiss(uv - vec2( RGB_AMT, 0.0) - vib).b;

    // (3) scanline + brightness
    float scan = 1.0 - SCANLINE * step(0.5, fract(gl_FragCoord.y * 0.5));
    float flick = 1.0 - FLICKER * hash(vec2(floor(t * 6.0), 7.0));
    col *= scan * flick;

    fragColor = vec4(col, 1.0) * vertexColor * ColorModulator;
}
