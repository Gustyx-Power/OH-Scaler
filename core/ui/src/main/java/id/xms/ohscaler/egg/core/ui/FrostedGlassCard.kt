package id.xms.ohscaler.egg.core.ui

import android.graphics.RuntimeShader
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSpecularBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurfaceFallback
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSpecularBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurfaceFallback

/**
 * Base blur radius discovered from Android 17 SystemUI:
 * R.dimen.notification_background_blur_radius = 25.0dp
 * R.dimen.volume_dialog_background_surface_blur_radius = 25.0dp
 * InfinityBlurUtilsKt.translucentBlurBackground = 25.0dp
 */
val Android17BaseBlurRadius: Dp = 25.dp

/**
 * Android 17 SystemUI AGSL triangleNoise procedural grain shader.
 * Extracted from com.android.systemui.surfaceeffects.core.turbulencenoise.TurbulenceNoiseShader.
 * Provides micro-dithered grain texture to avoid color banding and deliver tangible frosted acrylic texture.
 */
private const val ANDROID_17_GRAIN_SHADER = """
    uniform vec4 in_tint;
    uniform float in_grainIntensity;

    float triangleNoise(vec2 n) {
        n  = fract(n * vec2(5.3987, 5.4421));
        n += dot(n.yx, n.xy + vec2(21.5351, 14.3137));
        float xy = n.x * n.y;
        return fract(xy * 95.4307) + fract(xy * 75.04961) - 1.0;
    }

    vec4 main(vec2 p) {
        float noise = triangleNoise(p) * in_grainIntensity;
        vec3 c = clamp(in_tint.rgb + vec3(noise), 0.0, 1.0);
        return vec4(c * in_tint.a, in_tint.a);
    }
"""

/**
 * Creates and caches an AGSL ShaderBrush implementing Android 17's triangleNoise grain texture.
 * On Android 13+ (API 33+), uses native RuntimeShader.
 * On earlier API levels (or if grainIntensity <= 0), returns null with graceful fallback to solid tint.
 */
@Composable
fun rememberAndroid17GrainBrush(tint: Color, grainIntensity: Float = 0.035f): Brush? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || grainIntensity <= 0f) {
        return null
    }
    return remember(tint, grainIntensity) {
        try {
            val shader = RuntimeShader(ANDROID_17_GRAIN_SHADER)
            shader.setFloatUniform("in_tint", tint.red, tint.green, tint.blue, tint.alpha)
            shader.setFloatUniform("in_grainIntensity", grainIntensity)
            ShaderBrush(shader)
        } catch (e: Throwable) {
            null
        }
    }
}

/**
 * Android 17 SystemUI Specular Rim Border Brush.
 * Replicates the dual-tone specular highlight found on Android 17 frosted surfaces:
 * A subtle high-contrast sheen on the top edge fading into a soft boundary contour on the bottom edge.
 */
fun android17GlassBorderBrush(isDarkTheme: Boolean): Brush {
    val gradientColors = if (isDarkTheme) DarkGlassSpecularBorder else LightGlassSpecularBorder
    return Brush.verticalGradient(gradientColors)
}

/**
 * Replicates com.android.systemui.common.ui.compose.InfinityBlurUtilsKt.rememberIsBlurSupported.
 * Dynamically queries whether cross-window blur is supported and enabled by the system on Android 12+ (API 31+).
 * Gracefully provides a fallback when blur is disabled (e.g. low-power mode, accessibility, or unsupported GPU).
 */
@Composable
fun rememberIsBlurSupported(): Boolean {
    val context = LocalContext.current
    var isSupported by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val wm = context.getSystemService(WindowManager::class.java)
                    wm?.isCrossWindowBlurEnabled ?: true
                } catch (e: Throwable) {
                    true
                }
            } else {
                false
            }
        )
    }

    DisposableEffect(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = context.getSystemService(WindowManager::class.java)
            val listener = java.util.function.Consumer<Boolean> { enabled ->
                isSupported = enabled
            }
            try {
                wm?.addCrossWindowBlurEnabledListener(listener)
            } catch (e: Throwable) { }
            onDispose {
                try {
                    wm?.removeCrossWindowBlurEnabledListener(listener)
                } catch (e: Throwable) { }
            }
        } else {
            onDispose { }
        }
    }
    return isSupported
}

/**
 * Android 17 Frosted Glass Modifier.
 * Directly applies Android 17 SystemUI translucent surface styling:
 * - 58% alpha surface tint (Dark) / 62% alpha milk glass (Light) when blur is enabled.
 * - Solid high-opacity surface container fallback when blur is disabled.
 * - Android 17 AGSL triangleNoise tactile grain micro-texture (API 33+).
 * - Specular vertical gradient rim outline (sheen top, soft contour bottom).
 */
@Composable
fun Modifier.android17FrostedGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    forceBlurEnabled: Boolean? = null,
    enableGrain: Boolean = true,
    grainIntensity: Float = 0.035f
): Modifier {
    val blurSupported = forceBlurEnabled ?: rememberIsBlurSupported()
    val surfaceColor = if (blurSupported) {
        if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    } else {
        if (isDarkTheme) DarkGlassSurfaceFallback else LightGlassSurfaceFallback
    }
    val specularBrush = borderBrush ?: android17GlassBorderBrush(isDarkTheme)
    val grainBrush = if (enableGrain) rememberAndroid17GrainBrush(surfaceColor, grainIntensity) else null

    val bgModifier = if (grainBrush != null) {
        Modifier.background(grainBrush)
    } else {
        Modifier.background(surfaceColor)
    }

    return this
        .clip(shape)
        .then(bgModifier)
        .border(borderWidth, specularBrush, shape)
}

/**
 * Android 17 SystemUI Frosted Glass Card component.
 * Reverse-engineered from SystemUI (InfinityBlurUtils, NotificationBackgroundView, TurbulenceNoiseShader):
 * 1. Translucent 58% alpha diffuse surface tint over ambient depth layers.
 * 2. Android 17 AGSL triangleNoise grain texture for subtle tactile micro-finish.
 * 3. Specular rim lighting: crisp gradient edge with high-angle reflection.
 * 4. Soft ambient & spot elevation shadows matching Android 17 Material Expressive specs.
 * 5. Dynamic fallback to high-opacity surface container when system blur is turned off.
 * 6. Tactile spring press feedback with haptic response.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: Dp = 16.dp,
    elevation: Dp = 6.dp,
    customBackgroundBrush: Brush? = null,
    customBorderBrush: Brush? = null,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    forceBlurEnabled: Boolean? = null,
    enableGrain: Boolean = true,
    grainIntensity: Float = 0.035f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val blurSupported = forceBlurEnabled ?: rememberIsBlurSupported()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "frostedCardPress"
    )

    // Android 17 diffuse ambient & spot shadows
    val shadowSpotColor = if (isDarkTheme) Color(0x38000000) else Color(0x141E293B)
    val shadowAmbientColor = if (isDarkTheme) Color(0x1F000000) else Color(0x0A1E293B)

    // Android 17 translucent surface color with dynamic non-blur fallback
    val cardColor = if (blurSupported) {
        if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    } else {
        if (isDarkTheme) DarkGlassSurfaceFallback else LightGlassSurfaceFallback
    }

    // Android 17 specular rim lighting gradient
    val borderBrush = customBorderBrush ?: android17GlassBorderBrush(isDarkTheme)

    // Android 17 AGSL triangleNoise tactile grain texture (API 33+)
    val grainBrush = if (enableGrain && customBackgroundBrush == null) {
        rememberAndroid17GrainBrush(cardColor, grainIntensity)
    } else {
        null
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else {
        Modifier
    }

    val backgroundModifier = when {
        customBackgroundBrush != null -> Modifier.background(customBackgroundBrush)
        grainBrush != null -> Modifier.background(grainBrush)
        else -> Modifier.background(cardColor)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = shadowSpotColor,
                ambientColor = shadowAmbientColor
            )
            .clip(shape)
            .then(backgroundModifier)
            .border(1.dp, borderBrush, shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}
