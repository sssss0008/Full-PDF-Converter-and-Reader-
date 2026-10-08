package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurfaceHighlight
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPageData(
            title = "Ultra-Fast Reading & Markup",
            description = "View massive documents with zero lag. Highlight, draw, add sticky notes, and read comfortably with true OLED dark mode.",
            icon = Icons.Default.EditNote,
            badge = "SUB-PIXEL RENDERING"
        ),
        OnboardingPageData(
            title = "Complete Page & Document Control",
            description = "Merge, split, extract, rotate, and reorder pages effortlessly right from your device's local storage.",
            icon = Icons.Default.Layers,
            badge = "LOCAL ENGINE"
        ),
        OnboardingPageData(
            title = "100% Offline, Secure & Private",
            description = "Zero cloud uploads. Password-protect files, redact sensitive data, and run on-device OCR without spending a cent.",
            icon = Icons.Default.Security,
            badge = "ZERO BACKEND COST"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Skip Button top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.testTag("onboarding_skip_button")
            ) {
                Text(
                    text = "Skip",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("onboarding_pager")
            ) { pageIdx ->
                val page = pages[pageIdx]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Stylized Illustration Badge Card
                    Surface(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        color = ObsidianSurfaceHighlight,
                        tonalElevation = 6.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            NeonIndigo.copy(alpha = 0.25f),
                                            ElectricCyan.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(96.dp),
                                    shape = CircleShape,
                                    color = NeonIndigo.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = page.icon,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(54.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NeonIndigo.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = page.badge,
                                        color = NeonIndigo,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Bottom Navigation & Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .size(if (isSelected) 28.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonIndigo else MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }

                // Action Button
                val isLastPage = pagerState.currentPage == pages.size - 1

                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinish()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_action_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonIndigo
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = if (isLastPage) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
