package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

/**
 * Android 17 Frosted Glass Tool Card.
 * Semi-translucent frosted glass, specular rim lighting, and a soft elevation shadow
 * so cards are always distinct and tactile in both Light and Dark themes.
 */
@Composable
fun DribbbleToolCard(
    title: String,
    description: String,
    badgeText: String,
    iconRes: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "toolCardPress"
    )

    val shadowSpotColor = if (isDarkTheme) Color(0x33000000) else Color(0x1A1E293B)
    val shadowAmbientColor = if (isDarkTheme) Color(0x1F000000) else Color(0x0A1E293B)

    val cardColor = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val borderColor = if (isDarkTheme) DarkGlassBorder else LightGlassBorder

    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val descColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val badgeBg = if (isDarkTheme) Color(0x2BFFFFFF) else Color(0x14334155)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = shadowSpotColor,
                ambientColor = shadowAmbientColor
            )
            .clip(RoundedCornerShape(22.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in Squircle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = if (isDarkTheme) 0.20f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = descColor,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBg)
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = descColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
