#version 150

uniform sampler2D Sampler0;   // Texture Atlas (Mask)
uniform sampler2D Sampler1;   // MISSING_TEXTURE

uniform vec4 ColorModulator;
uniform float GameTime;
uniform vec4 SpriteUV;        // (u0, v0, u1, v1)

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const float TILES     = 4.0;   //  Number of tiles in a row
const float SPEED     = 240.0; // Scroll speed

// 疑似乱数
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    float a = texture(Sampler0, texCoord0).a;
    if (a < 0.1) {
        discard;
    }

    vec2 local = (texCoord0 - SpriteUV.xy) / max(SpriteUV.zw - SpriteUV.xy, vec2(1e-6));

    vec2 uv = local * TILES;
    float t = GameTime * SPEED;

    vec2 base = vec2(t, t * 0.6);
    base += 0.5 * vec2(sin(t * 1.4), cos(t * 1.8));

    vec2 muv = fract(uv + base);
    vec4 miss = texture(Sampler1, muv);
    fragColor = vec4(miss.rgb, 1.0) * vertexColor * ColorModulator;
}
