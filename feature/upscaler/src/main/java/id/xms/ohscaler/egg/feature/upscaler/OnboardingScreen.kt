package id.xms.ohscaler.egg.feature.upscaler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainerHigh
import id.xms.ohscaler.egg.core.designsystem.DarkSurfaceContainerHighest
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSurfaceContainerHigh
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary
import id.xms.ohscaler.egg.core.designsystem.SpecularEdgeTop
import id.xms.ohscaler.egg.core.ui.GradientButton
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val iconRes: Int,
    val titleRes: Int,
    val descRes: Int
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val pages = listOf(
        OnboardingPageData(
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_onboard_neural,
            titleRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_title_1,
            descRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_desc_1
        ),
        OnboardingPageData(
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_onboard_clarity,
            titleRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_title_2,
            descRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_desc_2
        ),
        OnboardingPageData(
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_onboard_studio,
            titleRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_title_3,
            descRes = id.xms.ohscaler.egg.core.designsystem.R.string.onboard_desc_3
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.size - 1

    val textPrimary = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textMuted = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val containerHigh = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val borderCol = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val dotInactive = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x33334155)
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Skip Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.onboard_skip),
                            color = textMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Carousel Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(if (isDarkTheme) 0.dp else 8.dp, RoundedCornerShape(32.dp), spotColor = Color(0x1F1E293B))
                            .size(150.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(primaryAccent.copy(alpha = 0.28f), containerHigh)
                                )
                            )
                            .border(1.dp, borderCol, RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = page.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(id = page.titleRes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(id = page.descRes),
                        fontSize = 14.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(durationMillis = 250),
                        label = "dotWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isSelected) primaryAccent else dotInactive)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            if (isLastPage) {
                GradientButton(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.onboard_start),
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccent,
                        contentColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                    )
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.onboard_next),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
