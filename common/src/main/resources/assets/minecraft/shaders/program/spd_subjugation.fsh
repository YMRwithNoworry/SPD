#version 150

uniform sampler2D DiffuseSampler;
uniform float Progress;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 texel = texture(DiffuseSampler, texCoord);
    float luma = dot(texel.rgb, vec3(0.299, 0.587, 0.114));
    vec3 gray = vec3(luma);
    vec3 color = mix(texel.rgb, gray, clamp(Progress, 0.0, 1.0));

    float contrast = 1.0 + 0.14 * Progress;
    color = (color - 0.5) * contrast + 0.5;

    fragColor = vec4(clamp(color, 0.0, 1.0), texel.a);
}
