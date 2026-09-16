package com.ridemarter.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.ThemeMode

data class StatItem(
    val title: String,
    val subtitle: String
)

@Composable
fun StatCard(
    stat: StatItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .testTag("stat_card_${stat.title.replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stat.title,
                color = BrandGreenPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = stat.subtitle,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun StatsBannerRow(
    stats: List<StatItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stats.forEach { stat ->
            StatCard(
                stat = stat,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(name = "StatCard Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun StatCardDarkPreview() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        StatsBannerRow(
            stats = listOf(
                StatItem("50+", "Commands"),
                StatItem("1ms", "Speed"),
                StatItem("3 Apps", "Supported")
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "StatCard Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun StatCardLightPreview() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        StatsBannerRow(
            stats = listOf(
                StatItem("50+", "Commands"),
                StatItem("1ms", "Speed"),
                StatItem("3 Apps", "Supported")
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
