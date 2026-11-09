package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.ElectricVioletLight
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary

@Composable
fun ResolutionBadge(
    srcWidth: Int,
    srcHeight: Int,
    scaleFactor: Int,
    modifier: Modifier = Modifier
) {
    val targetWidth = srcWidth * scaleFactor
    val targetHeight = srcHeight * scaleFactor

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${srcWidth}×${srcHeight}",
            color = TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "➔",
            color = CyanAccent,
            fontSize = 12.sp
        )
        Text(
            text = "${targetWidth}×${targetHeight}",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "${scaleFactor}×",
            color = ElectricVioletLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x337C3AED))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
