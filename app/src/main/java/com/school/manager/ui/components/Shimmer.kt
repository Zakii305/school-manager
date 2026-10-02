package com.school.manager.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    corner: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "translate"
    )

    val base = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val highlight = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(
                Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(translate, 0f),
                    end = Offset(translate + 300f, 300f)
                )
            )
    )
}

/** A full "list loading" placeholder — 4 fake rows with avatar + 2 text lines each. */
@Composable
fun ShimmerListPlaceholder(rows: Int = 4, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        repeat(rows) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                ShimmerBox(Modifier.height(44.dp).padding(end = 12.dp), height = 44.dp, corner = 22.dp)
                Column(Modifier.fillMaxWidth()) {
                    ShimmerBox(height = 14.dp)
                    Spacer(Modifier.height(8.dp))
                    ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 12.dp)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

/** A dashboard placeholder — a hero card + 2 stat cards + 2 action tiles. */
@Composable
fun ShimmerDashboardPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier.padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(height = 100.dp, corner = 24.dp)
        androidx.compose.foundation.layout.Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(Modifier.weight(1f), height = 100.dp)
            ShimmerBox(Modifier.weight(1f), height = 100.dp)
        }
        androidx.compose.foundation.layout.Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBox(Modifier.weight(1f), height = 100.dp)
            ShimmerBox(Modifier.weight(1f), height = 100.dp)
        }
    }
}
