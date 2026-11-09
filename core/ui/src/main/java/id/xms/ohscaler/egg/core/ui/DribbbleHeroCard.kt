package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkHeroGradient
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkTertiary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightHeroGradient
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSecondary
import id.xms.ohscaler.egg.core.designsystem.LightTertiary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

/**
 * Featured Hero Card modeled directly on Android 17 / Dribbble reference.
 * Frosted glass styling with Slate Blue & Coral highlights, elevation shadow,
 * and high-contrast pill action button.
 */
@Composable
fun DribbbleHeroCard(
    isEngineReady: Boolean,
    onSelectPhoto: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "heroPress"
    )

    val gradientColors = if (isDarkTheme) DarkHeroGradient else LightHeroGradient
    val shadowColor = if (isDarkTheme) Color(0x33000000) else Color(0x1A1E293B)
    val borderColor = if (isDarkTheme) DarkGlassBorder else LightGlassBorder

    val pillBgColor = if (isDarkTheme) Color(0x2BFFFFFF) else Color(0x40CBD5E1)
    val pillTextColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val actionBtnBg = if (isDarkTheme) Color(0xFFFFFFFF) else LightPrimary
    val actionBtnTextColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = shadowColor,
                ambientColor = shadowColor
            )
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(gradientColors))
            .border(1.dp, borderColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectPhoto()
                }
            )
            .padding(22.dp)
    ) {
        Column {
            // Top Row: Pills (100% Offline with lock + Active Engine)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Offline Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(pillBgColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = pillTextColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "100% Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pillTextColor
                    )
                }

                // Active Engine Pill (Clean, steady)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color(0xFFFFFFFF) else LightPrimary)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isEngineReady) "Active" else "Ready",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Title
            Text(
                text = "AI for Image Quality",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) DarkTextPrimary else LightTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Meta tags row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Real-ESRGAN Compact",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DarkTextSecondary else LightTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DarkTextMuted else LightTextMuted
                )
                Text(
                    text = "Up to 8× Scale",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DarkSecondary else LightSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DarkTextMuted else LightTextMuted
                )
                Text(
                    text = "Private",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) DarkTertiary else LightTertiary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Bottom Row: Step indicator + "Select Photo >" Pill Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to begin",
                    fontSize = 13.sp,
                    color = if (isDarkTheme) DarkTextSecondary else LightTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                // Pill Action CTA
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(actionBtnBg)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.hero_cta_select),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionBtnTextColor
                    )
                }
            }
        }
    }
}
