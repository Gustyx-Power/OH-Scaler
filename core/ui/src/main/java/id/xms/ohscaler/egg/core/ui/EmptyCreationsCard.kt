package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted

@Composable
fun EmptyCreationsCard(
    onSelectPhoto: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    FrostedGlassCard(
        isDarkTheme = isDarkTheme,
        contentPadding = 24.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_empty_photo_wand),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.recent_empty),
                fontSize = 13.sp,
                color = if (isDarkTheme) DarkTextMuted else LightTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSelectPhoto,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkTheme) DarkPrimary else LightPrimary,
                    contentColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                )
            ) {
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_quick_pick),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
