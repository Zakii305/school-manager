package com.school.manager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.ArabicStyles
import com.school.manager.ui.theme.IndigoDark
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.util.DailyAyat

@Composable
fun AyatCard(modifier: Modifier = Modifier) {
    val ayat = remember { DailyAyat.today() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(IndigoPrimary, IndigoDark)))
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("﷽", color = AmberAccent, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))

            Text(
                text = ayat.arabic,
                color = Color.White,
                style = ArabicStyles.ayat,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(
                color = AmberAccent.copy(alpha = 0.45f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(Modifier.height(14.dp))

            Text(
                text = "\u201C${ayat.english}\u201D",
                color = Color.White.copy(alpha = 0.92f),
                style = ArabicStyles.translation,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "— ${ayat.reference}",
                color = AmberAccent.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
