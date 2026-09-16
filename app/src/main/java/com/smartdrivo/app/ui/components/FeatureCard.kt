package com.smartdrivo.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdrivo.app.ui.theme.AccentBlue
import com.smartdrivo.app.ui.theme.AccentPurple
import com.smartdrivo.app.ui.theme.AccentRed
import com.smartdrivo.app.ui.theme.AccentTeal
import com.smartdrivo.app.ui.theme.BrandGreenPrimary
import com.smartdrivo.app.ui.theme.BrandOrangeSecondary
import com.smartdrivo.app.ui.theme.DarkSurfaceVariant
import com.smartdrivo.app.ui.theme.DarkTextSecondary
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.ThemeMode

data class FeatureItem(
    val id: String,
    val icon: ImageVector,
    val circleColor: Color,
    val title: String,
    val description: String
)

@Composable
fun FeatureCard(
    feature: FeatureItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feature_card_${feature.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant
        ),
        border = BorderStroke(1.dp, Color(0xFF262626))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon background circle (40dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(feature.circleColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.circleColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Text column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = feature.description,
                    color = DarkTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

val defaultFeatures = listOf(
    FeatureItem(
        id = "ultra_fast_accept",
        icon = Icons.Default.Bolt,
        circleColor = BrandGreenPrimary,
        title = "Ultra Fast Accept",
        description = "Accept orders in as fast as 1ms before anyone else on Rapido, Uber and Ola"
    ),
    FeatureItem(
        id = "smart_area_filters",
        icon = Icons.Default.LocationOn,
        circleColor = AccentBlue,
        title = "Smart Area Filters",
        description = "Set GO TO and NO GO zones. Only accept rides from your preferred areas"
    ),
    FeatureItem(
        id = "full_order_history",
        icon = Icons.Default.History,
        circleColor = BrandOrangeSecondary,
        title = "Full Order History",
        description = "Every accepted, rejected and ignored order saved with time, date, fare and drop details"
    ),
    FeatureItem(
        id = "auto_bike_car",
        icon = Icons.Default.DirectionsCar,
        circleColor = AccentPurple,
        title = "Auto, Bike & Car",
        description = "Works for Auto Rickshaw, Bike and Car drivers on Rapido, Uber and Ola apps"
    ),
    FeatureItem(
        id = "auto_reject_unwanted",
        icon = Icons.Default.Shield,
        circleColor = AccentRed,
        title = "Auto Reject Unwanted",
        description = "Automatically reject or ignore out of range and NO GO area orders instantly"
    ),
    FeatureItem(
        id = "order_alert_popup",
        icon = Icons.Default.Notifications,
        circleColor = AccentTeal,
        title = "Order Alert Popup",
        description = "Side popup shows fare, pickup distance, drop area after every accepted order"
    )
)

@Preview(name = "FeatureCard Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun FeatureCardDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FeatureCard(feature = defaultFeatures[0])
            Spacer(modifier = Modifier.height(8.dp))
            FeatureCard(feature = defaultFeatures[1])
        }
    }
}

@Preview(name = "FeatureCard Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun FeatureCardLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            FeatureCard(feature = defaultFeatures[0])
            Spacer(modifier = Modifier.height(8.dp))
            FeatureCard(feature = defaultFeatures[1])
        }
    }
}
