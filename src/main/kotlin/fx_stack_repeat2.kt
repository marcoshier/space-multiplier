import org.intellij.lang.annotations.Language

@Language("GLSL")
val fx_stack_repeat2 = """
#version 330

in vec2 v_texCoord0;
uniform sampler2D tex0; // content texture
uniform sampler2D tex1; // SDF mask texture

// Viewport and repeat controls
uniform vec2 screenSize;
uniform int repeats;
uniform float zoom;
uniform vec2 origin; // Combined xOrigin and yOrigin
uniform vec2 offset; // Combined xOffset and yOffset

// Mask controls
uniform vec2 maskBoundsPosition; // Top-left corner of the mask shape's bounds (in pixels)
uniform vec2 maskBoundsSize;     // Width and height of the mask shape's bounds (in pixels)
uniform float shapeSmoothness;

out vec4 o_color;

// Calculates the mask value by correctly mapping a screen position to the SDF texture UV
float getSDFTextureMask(vec2 sampleScreenPos) {
    // 1. Calculate the UV coordinate *within the mask's own bounding box*
    vec2 maskUV = (sampleScreenPos - maskBoundsPosition) / maskBoundsSize;

    // 2. Check if the sample position is inside the bounds
    if (maskUV.x >= 0.0 && maskUV.y >= 0.0 && maskUV.x <= 1.0 && maskUV.y <= 1.0) {
        // 3. Sample the SDF texture using this correct UV
        float sdfValue = texture(tex1, maskUV).r; // Assuming SDF is in red channel

        // 4. Convert SDF value to a smooth mask
        // SDF is often stored in [0, 1], representing a distance from [-1, 1]
        float distance = (sdfValue - 0.5) * 2.0;
        return 1.0 - smoothstep(-shapeSmoothness, shapeSmoothness, distance);
    }

    return 0.0; // Outside the bounds, so mask is 0
}

void main() {
    // Convert UVs to pixel coordinates
    vec2 screenPos = v_texCoord0 * screenSize;
    vec2 originScreen = (origin * 0.5 + 0.5) * screenSize;
    vec2 offsetScreen = offset * screenSize * 0.1;

    vec4 final_color = texture(tex0, v_texCoord0);

    for (int i = 1; i <= repeats; ++i) {
        float i_f = float(i);

        // Calculate the new sample position in screen space (pixels)
        vec2 centeredScreen = screenPos - originScreen;
        vec2 scaledScreen = centeredScreen * pow(1.0 + zoom, i_f);
        vec2 samplePos_screen = scaledScreen + originScreen + offsetScreen * i_f;

        // Get the mask value using the correct pixel-based function
        float mask = getSDFTextureMask(samplePos_screen);

        // Convert the final sample position back to UV to sample the content texture
        vec2 samplePos_uv = samplePos_screen / screenSize;
        vec4 sample_color = texture(tex0, samplePos_uv);

        // Blend based on the mask
        final_color = mix(final_color, sample_color, sample_color.a * mask);
    }

    o_color = final_color;
}
""".trimIndent()