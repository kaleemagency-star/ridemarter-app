package com.ridemarter.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val strokeWidth = size.width * 0.22f
        val radius = (size.width - strokeWidth) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Blue right / bottom-right arc
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        // Green bottom arc
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 55f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        // Yellow bottom-left arc
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 155f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        // Red top arc
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 235f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
        // Center horizontal bar for "G"
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x - 2f, center.y - strokeWidth / 2f),
            size = Size(radius + 2f, strokeWidth)
        )
    }
}

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F),
            disabledContainerColor = Color(0xFFE0E0E0),
            disabledContentColor = Color(0xFF757575)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("google_sign_in_button")
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color(0xFF1F1F1F),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoogleLogoIcon()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F1F1F)
                )
            }
        }
    }
}
