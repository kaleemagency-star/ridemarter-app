package com.ridemarter.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridemarter.app.ui.theme.AccentGold
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.BrandOrangeSecondary
import com.ridemarter.app.ui.theme.DarkCardBorder
import com.ridemarter.app.ui.theme.DarkSurfaceVariant
import com.ridemarter.app.ui.theme.DarkTextMuted
import com.ridemarter.app.ui.theme.DarkTextSecondary
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.ThemeMode

data class PlanItem(
    val id: String,
    val duration: String,
    val price: String,
    val badgeText: String,
    val badgeColor: Color,
    val isHighlighted: Boolean = false,
    val ribbonText: String? = null
)

@Composable
fun PlanCard(
    plan: PlanItem,
    modifier: Modifier = Modifier
) {
    val borderColor = if (plan.isHighlighted) BrandOrangeSecondary else DarkCardBorder
    val backgroundColor = if (plan.isHighlighted) Color(0xFF222222) else DarkSurfaceVariant

    Card(
        modifier = modifier
            .width(145.dp)
            .testTag("plan_card_${plan.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (plan.isHighlighted) 1.5.dp else 1.dp, borderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(plan.badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = plan.badgeText,
                        color = plan.badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Duration
                Text(
                    text = plan.duration,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price
                Text(
                    text = plan.price,
                    color = BrandGreenPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Ribbon for Plan 2 ("MOST POPULAR")
            if (plan.ribbonText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 14.dp))
                        .background(BrandOrangeSecondary)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = plan.ribbonText,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

val defaultPlans = listOf(
    PlanItem(
        id = "plan_3_days",
        duration = "3 Days",
        price = "Rs.59",
        badgeText = "STARTER",
        badgeColor = DarkTextMuted,
        isHighlighted = false
    ),
    PlanItem(
        id = "plan_7_days",
        duration = "7 Days",
        price = "Rs.129",
        badgeText = "POPULAR",
        badgeColor = BrandOrangeSecondary,
        isHighlighted = true,
        ribbonText = "MOST POPULAR"
    ),
    PlanItem(
        id = "plan_15_days",
        duration = "15 Days",
        price = "Rs.199",
        badgeText = "VALUE",
        badgeColor = Color(0xFF64B5F6),
        isHighlighted = false
    ),
    PlanItem(
        id = "plan_1_month",
        duration = "1 Month",
        price = "Rs.329",
        badgeText = "BEST DEAL",
        badgeColor = AccentGold,
        isHighlighted = false
    )
)

@Composable
fun PlansHorizontalRow(
    plans: List<PlanItem> = defaultPlans,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(plans, key = { it.id }) { plan ->
            PlanCard(plan = plan)
        }
    }
}

@Preview(name = "PlanCard Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PlanCardDarkPreview() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanCard(plan = defaultPlans[0])
            PlanCard(plan = defaultPlans[1])
        }
    }
}

@Preview(name = "PlanCard Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun PlanCardLightPreview() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanCard(plan = defaultPlans[0])
            PlanCard(plan = defaultPlans[1])
        }
    }
}
