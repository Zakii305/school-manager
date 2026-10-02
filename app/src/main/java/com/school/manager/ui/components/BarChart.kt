package com.school.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// HORIZONTAL BAR CHART (used in AnalyticsScreen)
// ============================================================
data class BarEntry(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun HorizontalBarChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    maxValue: Float = 100f
) {
    if (entries.isEmpty()) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("No data", color = Color(0xFF64748B), fontSize = 12.sp)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEach { entry ->
            val fraction = (entry.value / maxValue).coerceIn(0f, 1f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.label,
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    modifier = Modifier.width(64.dp),
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(entry.color.copy(alpha = 0.15f))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(fraction)
                            .height(20.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(entry.color)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    entry.value.toInt().toString(),
                    color = entry.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ============================================================
// SIMPLE VERTICAL BAR CHART (for dashboard)
// ============================================================
data class BarData(val label: String, val value: Float, val color: Color)

@Composable
fun SimpleBarChart(
    data: List<BarData>,
    modifier: Modifier = Modifier,
    height: Int = 180
) {
    if (data.isEmpty()) return
    val maxValue = data.maxOf { it.value }.coerceAtLeast(1f)

    Column(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().height(height.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { item ->
                    val fraction = (item.value / maxValue).coerceIn(0f, 1f)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Box(
                            Modifier
                                .width(24.dp)
                                .height((height * 0.85f * fraction).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(item.color)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    item.label,
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
