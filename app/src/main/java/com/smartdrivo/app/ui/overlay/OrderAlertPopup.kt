package com.smartdrivo.app.ui.overlay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdrivo.app.model.OrderAlertData
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun OrderAlertPopupContent(
    data: OrderAlertData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = onDismiss,
    onReject: () -> Unit = onDismiss,
    onIgnore: () -> Unit = onDismiss
) {
    val offsetX = remember { Animatable(350f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        offsetX.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        )
    }

    fun dismissWithAnimation(action: () -> Unit = onDismiss) {
        scope.launch {
            offsetX.animateTo(
                targetValue = 350f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
            action()
        }
    }

    // Platform theme accent color
    val platformColor = when (data.platform.trim().lowercase()) {
        "rapido" -> Color(0xFFFF5722)
        "uber" -> Color(0xFF1A1A1A)
        "ola" -> Color(0xFF00B140)
        else -> Color(0xFF00C853)
    }

    Card(
        modifier = modifier
            .width(290.dp)
            .graphicsLayer { translationX = offsetX.value }
            .testTag("order_alert_popup_card"),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            bottomStart = 16.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. TOP PLATFORM BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(platformColor)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.platform.ifBlank { "Ride Alert" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("alert_platform_name")
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (data.action.equals("rejected", ignoreCase = true)) Color(0xFFD50000) else Color(0xFF00C853)
                ) {
                    Text(
                        text = if (data.action.isNotBlank()) data.action.uppercase() else "ACCEPTED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("alert_action_pill")
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { dismissWithAnimation(onDismiss) },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("alert_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 2. MAIN CONTENT (Clean white background)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                // FARE ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fare Amount",
                            color = Color(0xFF666666),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (data.fare.startsWith("Rs") || data.fare.startsWith("₹")) data.fare else "₹${data.fare}",
                            color = Color(0xFF00C853),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp,
                            modifier = Modifier.testTag("alert_fare_text")
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Speed",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = data.speed.ifBlank { "10ms" },
                                color = Color(0xFF00C853),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // PICKUP DISTANCE ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Pickup",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Pickup Distance",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                        Text(
                            text = data.pickupDistance.ifBlank { "0.0 km" },
                            color = Color(0xFF212121),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // DROP DISTANCE ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBE9E7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Drop Distance",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Drop Distance",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                        Text(
                            text = data.dropDistance.ifBlank { "0.0 km" },
                            color = Color(0xFF212121),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(
                    color = Color(0xFFEEEEEE),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // DROP AREA & ADDRESS ROW
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Drop Area",
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Drop Area",
                            color = Color(0xFF666666),
                            fontSize = 11.sp
                        )
                        Text(
                            text = data.dropArea.ifBlank { "Unknown Destination" },
                            color = Color(0xFF212121),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (data.dropAddress.isNotBlank()) {
                            Text(
                                text = data.dropAddress,
                                color = Color(0xFF757575),
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. ACTION BUTTONS (ACCEPT / REJECT / IGNORE)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // ACCEPT BUTTON (Green)
                    Button(
                        onClick = { dismissWithAnimation(onAccept) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp)
                            .testTag("alert_accept_btn")
                    ) {
                        Text(
                            text = "ACCEPT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // REJECT BUTTON (Red)
                    Button(
                        onClick = { dismissWithAnimation(onReject) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD50000),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("alert_reject_btn")
                    ) {
                        Text(
                            text = "REJECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // IGNORE BUTTON (Grey)
                    Button(
                        onClick = { dismissWithAnimation(onIgnore) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF757575),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("alert_ignore_btn")
                    ) {
                        Text(
                            text = "IGNORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. BOTTOM STATUS BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-dismisses in 10s",
                    color = Color(0xFF888888),
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { dismissWithAnimation(onDismiss) },
                    modifier = Modifier.testTag("alert_dismiss_text_btn")
                ) {
                    Text(
                        text = "Dismiss",
                        color = Color(0xFF00C853),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Wrapper for alternative invocation
@Composable
fun OrderAlertPopup(
    data: OrderAlertData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = onDismiss,
    onReject: () -> Unit = onDismiss,
    onIgnore: () -> Unit = onDismiss
) {
    OrderAlertPopupContent(
        data = data,
        onDismiss = onDismiss,
        modifier = modifier,
        onAccept = onAccept,
        onReject = onReject,
        onIgnore = onIgnore
    )
}

@Preview(name = "Alert Rapido Dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun OrderAlertPopupPreviewRapidoDark() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000)),
            contentAlignment = Alignment.TopEnd
        ) {
            OrderAlertPopupContent(
                data = OrderAlertData(
                    platform = "Rapido",
                    fare = "84",
                    pickupDistance = "1.2 km",
                    dropDistance = "5.4 km",
                    dropArea = "Koramangala 5th Block",
                    dropAddress = "Near Forum Mall, 80ft Road",
                    speed = "10ms",
                    action = "Accepted"
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Alert Uber Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun OrderAlertPopupPreviewUberLight() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x88000000)),
            contentAlignment = Alignment.TopEnd
        ) {
            OrderAlertPopupContent(
                data = OrderAlertData(
                    platform = "Uber",
                    fare = "145",
                    pickupDistance = "0.8 km",
                    dropDistance = "7.2 km",
                    dropArea = "HSR Layout Sector 2",
                    dropAddress = "27th Main Road HSR Layout",
                    speed = "1ms",
                    action = "Accepted"
                ),
                onDismiss = {}
            )
        }
    }
}
