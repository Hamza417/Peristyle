package app.simple.peri.utils

import app.simple.peri.models.Effect
import kotlin.random.Random

/**
 * Utility class for generating random but aesthetically pleasing effect values.
 * The algorithm ensures that generated effects don't feel too chaotic by using
 * constrained randomization and theme-based generation.
 */
object EffectGenerator {

    /**
     * Effect generation themes that produce different visual styles
     */
    enum class EffectTheme {
        SUBTLE,      // Minimal changes, subtle enhancement
        WARM,        // Warm color tones
        COOL,        // Cool color tones
        VIBRANT,     // High saturation and contrast
        VINTAGE,     // Retro/faded look
        DRAMATIC,    // Strong contrast and effects
        MONOCHROME,  // Reduced color, high contrast
        DREAMY       // Soft, ethereal look
    }

    /**
     * Generates a random effect with a randomly selected theme
     */
    fun generateRandomEffect(): Effect {
        val theme = EffectTheme.entries.toTypedArray().random()
        return generateEffectWithTheme(theme)
    }

    /**
     * Generates an effect based on a specific theme
     */
    fun generateEffectWithTheme(theme: EffectTheme): Effect {
        return when (theme) {
            EffectTheme.SUBTLE -> generateSubtleEffect()
            EffectTheme.WARM -> generateWarmEffect()
            EffectTheme.COOL -> generateCoolEffect()
            EffectTheme.VIBRANT -> generateVibrantEffect()
            EffectTheme.VINTAGE -> generateVintageEffect()
            EffectTheme.DRAMATIC -> generateDramaticEffect()
            EffectTheme.MONOCHROME -> generateMonochromeEffect()
            EffectTheme.DREAMY -> generateDreamyEffect()
        }
    }

    private fun generateSubtleEffect(): Effect {
        return Effect(
                0f,                              // No blur
                randomInRange(-40f, 40f),        // Wider brightness adjustment
                randomInRange(0.9f, 1.25f),      // Wider contrast change
                randomInRange(0.85f, 1.35f),     // Wider saturation adjustment
                randomInRange(0f, 30f),          // Wider hue shifts
                randomInRange(0f, 30f),
                randomInRange(0f, 30f),
                randomInRange(0.9f, 1f),         // Wider color scale
                randomInRange(0.9f, 1f),
                randomInRange(0.9f, 1f)
        )
    }

    private fun generateWarmEffect(): Effect {
        val warmHue = randomInRange(0f, 50f)  // Wider orange-red range
        return Effect(
                0f,                              // No blur
                randomInRange(0f, 80f),          // Much brighter
                randomInRange(1.0f, 1.6f),       // Wider contrast range
                randomInRange(1.0f, 1.6f),       // Wider saturation
                warmHue + randomInRange(0f, 40f), // Stronger red-orange tones
                randomInRange(0f, 30f),
                randomInRange(0f, 20f),          // More blue reduction
                randomInRange(0.9f, 1f),
                randomInRange(0.85f, 0.98f),     // More green reduction
                randomInRange(0.7f, 0.9f)        // Stronger blue scale reduction
        )
    }

    private fun generateCoolEffect(): Effect {
        val coolHue = randomInRange(180f, 270f)  // Wider blue-cyan range
        return Effect(
                0f,                              // No blur
                randomInRange(-60f, 20f),        // Wider darker range
                randomInRange(1.0f, 1.5f),       // Wider contrast
                randomInRange(0.9f, 1.5f),       // Wider saturation
                randomInRange(0f, 20f),          // More red reduction
                randomInRange(0f, 40f),
                coolHue - 180f + randomInRange(0f, 50f), // Stronger blue-cyan tones
                randomInRange(0.7f, 0.9f),       // More red scale reduction
                randomInRange(0.85f, 0.98f),
                randomInRange(0.9f, 1f)          // Preserve blue
        )
    }

    private fun generateVibrantEffect(): Effect {
        return Effect(
                0f,                              // No blur for sharpness
                randomInRange(10f, 100f),        // Much brighter
                randomInRange(1.2f, 2.2f),       // Much higher contrast
                randomInRange(1.3f, 2.0f),       // Much higher saturation
                randomInRange(0f, 80f),          // Wider hue range
                randomInRange(0f, 80f),
                randomInRange(0f, 80f),
                randomInRange(0.9f, 1f),
                randomInRange(0.9f, 1f),
                randomInRange(0.9f, 1f)
        )
    }

    private fun generateVintageEffect(): Effect {
        val vintageHue = randomInRange(15f, 50f)  // Wider sepia-ish tones
        return Effect(
                0f,                              // No blur
                randomInRange(-80f, -5f),        // Much darker range
                randomInRange(0.7f, 1.2f),       // Wider contrast range
                randomInRange(0.5f, 0.9f),       // More desaturation
                vintageHue + randomInRange(-20f, 20f),
                vintageHue * 0.7f + randomInRange(-20f, 20f),
                vintageHue * 0.4f + randomInRange(-20f, 20f),
                randomInRange(0.9f, 1f),
                randomInRange(0.8f, 0.95f),      // More green reduction
                randomInRange(0.65f, 0.85f)      // Stronger blue reduction for warmth
        )
    }

    private fun generateDramaticEffect(): Effect {
        return Effect(
                0f,                              // No blur
                randomInRange(-120f, 120f),      // Very wide brightness range
                randomInRange(1.4f, 3.0f),       // Much higher contrast
                randomInRange(0.7f, 1.7f),       // Wider saturation range
                randomInRange(0f, 100f),         // Much wider hue range
                randomInRange(0f, 100f),
                randomInRange(0f, 100f),
                randomInRange(0.8f, 1f),         // Wider scale range
                randomInRange(0.8f, 1f),
                randomInRange(0.8f, 1f)
        )
    }

    private fun generateMonochromeEffect(): Effect {
        val monoValue = randomInRange(0f, 60f)  // Wider hue for all channels
        return Effect(
                0f,                              // No blur
                randomInRange(-60f, 60f),        // Wider brightness range
                randomInRange(1.2f, 2.5f),       // Much higher contrast
                randomInRange(0.2f, 0.7f),       // Wider low saturation range
                monoValue,
                monoValue,
                monoValue,
                randomInRange(0.85f, 1f),        // Wider scale range
                randomInRange(0.85f, 1f),
                randomInRange(0.85f, 1f)
        )
    }

    private fun generateDreamyEffect(): Effect {
        val softHue = randomInRange(250f, 340f)  // Wider purple-pink range
        return Effect(
                0f,                              // No blur
                randomInRange(20f, 100f),        // Much brighter
                randomInRange(0.6f, 1.1f),       // Wider lower contrast
                randomInRange(1.0f, 1.6f),       // Wider enhanced saturation
                softHue * 0.5f + randomInRange(-30f, 30f),
                softHue * 0.3f + randomInRange(-30f, 30f),
                softHue - 250f + randomInRange(0f, 60f),
                randomInRange(0.9f, 0.98f),
                randomInRange(0.85f, 0.96f),     // Wider range
                randomInRange(0.92f, 1f)
        )
    }

    /**
     * Helper function to generate a random float within a range
     */
    private fun randomInRange(min: Float, max: Float): Float {
        return min + Random.nextFloat() * (max - min)
    }

    /**
     * Generates effect values and returns them as a tuple of 10 floats
     * for direct use in dialog callbacks
     */
    fun generateRandomEffectValues(): Effect {
        val effect = generateRandomEffect()
        return Effect(
                effect.blurValue,
                effect.brightnessValue,
                effect.contrastValue,
                effect.saturationValue,
                effect.hueRedValue,
                effect.hueGreenValue,
                effect.hueBlueValue,
                effect.scaleRedValue,
                effect.scaleGreenValue,
                effect.scaleBlueValue
        )
    }
}
