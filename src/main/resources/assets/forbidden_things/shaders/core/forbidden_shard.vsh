#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;     // x = シャード固有シード, y = ドリフト量(JSON由来)
in ivec2 UV1;   // overlay（未使用・宣言のみ）
in ivec2 UV2;   // light（未使用・宣言のみ）
in vec3 Normal;  // 未使用・宣言のみ

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 vertexColor;
out vec2 seedCoord;

const float DRIFT_SPEED = 600.0;  // ドリフト速度（GameTimeは約20分で0→1なので大きめ）

void main() {
    float seed = UV0.x;
    float amp = UV0.y;
    float t = GameTime * DRIFT_SPEED + seed * 6.2831853;

    // モデル空間でふわふわ漂わせる（quadの4頂点は同じseed/ampなので剛体移動）
    vec3 drift = amp * vec3(sin(t), cos(t * 1.3), sin(t * 0.7));

    gl_Position = ProjMat * ModelViewMat * vec4(Position + drift, 1.0);
    vertexColor = Color;
    seedCoord = UV0;
}
