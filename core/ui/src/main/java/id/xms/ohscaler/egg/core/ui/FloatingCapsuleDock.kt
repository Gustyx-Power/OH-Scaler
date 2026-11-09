package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

/**
 * Floating frosted capsule navigation dock modeled after Android 17 Volume Slider pill.
 */
@Composable
fun FloatingCapsuleDock(
    currentTab: Int, // 0 = Enhance, 1 = Creations
    onSelectTab: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    val dockBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val dockBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val shadowColor = if (isDarkTheme) Color(0x40000000) else Color(0x1A1E293B)

    val activePillBg = if (isDarkTheme) DarkPrimary else LightPrimary
    val activeIconColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val inactiveIconColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(12.dp, CircleShape, spotColor = shadowColor, ambientColor = shadowColor)
                .clip(CircleShape)
                .background(dockBg)
                .border(1.dp, dockBorder, CircleShape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Home / Enhance
            DockItem(
                isSelected = currentTab == 0,
                activeBg = activePillBg,
                activeColor = activeIconColor,
                inactiveColor = inactiveIconColor,
                icon = Icons.Rounded.AutoAwesome,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectTab(0)
                }
            )

            // Tab 1: Creations Gallery
            DockItem(
                isSelected = currentTab == 1,
                activeBg = activePillBg,
                activeColor = activeIconColor,
                inactiveColor = inactiveIconColor,
                icon = Icons.Rounded.History,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelectTab(1)
                }
            )

            // Theme Toggle (Sun / Moon)
            DockItem(
                isSelected = false,
                activeBg = activePillBg,
                activeColor = activeIconColor,
                inactiveColor = inactiveIconColor,
                icon = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleTheme()
                }
            )

            // Help & Tutorial
            DockItem(
                isSelected = false,
                activeBg = activePillBg,
                activeColor = activeIconColor,
                inactiveColor = inactiveIconColor,
                icon = Icons.AutoMirrored.Rounded.HelpOutline,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenHelp()
                }
            )
        }
    }
}

@Composable
private fun DockItem(
    isSelected: Boolean,
    activeBg: Color,
    activeColor: Color,
    inactiveColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (isSelected) activeBg else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
