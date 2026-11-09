package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import java.util.Locale

@Composable
fun ResolutionBadge(
    srcWidth: Int,
    srcHeight: Int,
    scaleFactor: Float,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val targetWidth = (srcWidth * scaleFactor).toInt()
    val targetHeight = (srcHeight * scaleFactor).toInt()
    val mp = (targetWidth.toLong() * targetHeight.toLong()) / 1_000_000.0
    val scaleLabel = if (scaleFactor % 1f == 0f) "${scaleFactor.toInt()}×" else "${scaleFactor}×"
    val mpLabel = String.format(Locale.US, "%.1f MP", mp)

    val badgeBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val badgeBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val primaryText = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val mutedText = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val accentTagColor = if (isDarkTheme) DarkPrimary else LightPrimary

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(badgeBg)
            .border(1.dp, badgeBorder, CircleShape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${srcWidth}×${srcHeight}",
            color = mutedText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "➔",
            color = accentTagColor,
            fontSize = 11.sp
        )
        Text(
            text = "${targetWidth}×${targetHeight}",
            color = primaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "$scaleLabel • $mpLabel",
            color = accentTagColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(accentTagColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ResolutionBadge(
    srcWidth: Int,
    srcHeight: Int,
    scaleFactor: Int,
    modifier: Modifier = Modifier
) {
    ResolutionBadge(srcWidth, srcHeight, scaleFactor.toFloat(), modifier)
}
