package com.ridemarter.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ridemarter.app.model.DateFilter
import com.ridemarter.app.model.OrderHistory
import com.ridemarter.app.ui.theme.RideMarterTheme
import com.ridemarter.app.viewmodel.HistoryViewModel
import com.ridemarter.app.viewmodel.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HistoryScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val filteredList by viewModel.filteredList.collectAsStateWithLifecycle()
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val selectedPlatforms by viewModel.selectedPlatforms.collectAsStateWithLifecycle()
    val selectedActions by viewModel.selectedActions.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshHistory() }
    )

    val todayDateFormatted = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("history_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // SECTION 1 — TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Order History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todayDateFormatted,
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }
                IconButton(
                    onClick = { viewModel.refreshHistory() },
                    modifier = Modifier.testTag("history_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF00C853)
                    )
                }
            }

            // SECTION 2 — STATS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(
                    value = stats.total.toString(),
                    label = "Total",
                    valueColor = Color.White
                )
                StatMiniCard(
                    value = stats.accepted.toString(),
                    label = "Accepted",
                    valueColor = Color(0xFF00C853)
                )
                StatMiniCard(
                    value = stats.rejected.toString(),
                    label = "Rejected",
                    valueColor = Color(0xFFD50000)
                )
                StatMiniCard(
                    value = stats.ignored.toString(),
                    label = "Ignored",
                    valueColor = Color(0xFFFF6D00)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SECTION 3 — FILTER CHIPS
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group 1: Date Chips
                val dateFilters = listOf(
                    "Today" to DateFilter.TODAY,
                    "Yesterday" to DateFilter.YESTERDAY,
                    "Last 7 Days" to DateFilter.LAST_7_DAYS,
                    "Last 30 Days" to DateFilter.LAST_30_DAYS,
                    "All Time" to DateFilter.ALL_TIME
                )
                items(dateFilters) { (title, filter) ->
                    FilterChip(
                        selected = selectedDateFilter == filter,
                        onClick = { viewModel.setDateFilter(filter) },
                        label = { Text(title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C853),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1A1A1A),
                            labelColor = Color(0xFFAAAAAA)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = null
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF333333))
                    )
                }

                // Group 2: Platform Chips
                val platforms = listOf("All", "Rapido", "Uber", "Ola")
                items(platforms) { platform ->
                    val isSelected = if (platform == "All") selectedPlatforms.isEmpty() else platform in selectedPlatforms
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.togglePlatformFilter(platform) },
                        label = { Text(platform, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C853),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1A1A1A),
                            labelColor = Color(0xFFAAAAAA)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = null
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(Color(0xFF333333))
                    )
                }

                // Group 3: Action Chips
                val actions = listOf("All", "Accepted", "Rejected", "Ignored")
                items(actions) { action ->
                    val isSelected = if (action == "All") selectedActions.isEmpty() else action in selectedActions
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleActionFilter(action) },
                        label = { Text(action, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C853),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1A1A1A),
                            labelColor = Color(0xFFAAAAAA)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // SECTION 4 — ORDER LIST
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    isLoading && filteredList.isEmpty() -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(5) {
                                ShimmerCard()
                            }
                        }
                    }
                    filteredList.isEmpty() -> {
                        EmptyHistoryState(onNavigateToDashboard = onNavigateToDashboard)
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("history_list"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredList, key = { it.docId }) { order ->
                                OrderHistoryCard(order = order)
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color(0xFF1A1A1A),
                    contentColor = Color(0xFF00C853)
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatMiniCard(
    value: String,
    label: String,
    valueColor: Color
) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFAAAAAA)
            )
        }
    }
}

@Composable
private fun OrderHistoryCard(order: OrderHistory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_history_card_${order.docId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Platform, Vehicle Type & Action Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val platformColor = when (order.platform.lowercase(Locale.ROOT)) {
                    "rapido" -> Color(0xFFFFD600)
                    "uber" -> Color.White
                    "ola" -> Color(0xFF00C853)
                    else -> Color(0xFF4FC3F7)
                }

                val platformTextColor = if (order.platform.equals("rapido", ignoreCase = true)) Color.Black else Color.Black

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(platformColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.platform,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = platformTextColor
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF282828))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = order.vehicleType,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCCCCCC)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val (actionColor, actionIcon) = when (order.action.lowercase(Locale.ROOT)) {
                    "accepted" -> Pair(Color(0xFF00C853), Icons.Default.Check)
                    "rejected" -> Pair(Color(0xFFD50000), Icons.Default.Close)
                    else -> Pair(Color(0xFFFF6D00), Icons.Default.Remove)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(actionColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.action,
                        color = actionColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Drop Area & Fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = Color(0xFF00C853),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.dropArea,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹${order.fare}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF00C853)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trip Distance & Speed Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pickup: ${order.pickupDistance}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trip: ${order.dropDistance}",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "⚡ ${order.speed}",
                    color = Color(0xFF888888),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer: Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${order.date} • ${order.time}",
                    color = Color(0xFF777777),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ShimmerCard() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color(0xFF222222),
        Color(0xFF2E2E2E),
        Color(0xFF222222)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(width = 80.dp, height = 18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .size(width = 220.dp, height = 16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(width = 90.dp, height = 14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(onNavigateToDashboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF00C853).copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = Color(0xFF00C853),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Orders Found",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No rides match the selected filters or date range.",
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToDashboard,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("history_empty_go_dashboard")
        ) {
            Text(
                text = "Go to Dashboard",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(name = "HistoryScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HistoryScreenDarkPreview() {
    RideMarterTheme(themeMode = ThemeMode.DARK) {
        HistoryScreen(onNavigateToDashboard = {})
    }
}

@Preview(name = "HistoryScreen Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun HistoryScreenLightPreview() {
    RideMarterTheme(themeMode = ThemeMode.LIGHT) {
        HistoryScreen(onNavigateToDashboard = {})
    }
}
