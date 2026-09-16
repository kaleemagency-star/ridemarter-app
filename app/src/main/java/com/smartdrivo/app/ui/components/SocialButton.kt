package com.smartdrivo.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartdrivo.app.ui.theme.InstagramOrange
import com.smartdrivo.app.ui.theme.InstagramPink
import com.smartdrivo.app.ui.theme.InstagramPurple
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.ui.theme.TelegramBlue
import com.smartdrivo.app.ui.theme.WhatsAppGreen
import com.smartdrivo.app.viewmodel.ThemeMode

sealed class SocialPlatform(
    val name: String,
    val url: String,
    val icon: ImageVector,
    val backgroundBrush: Brush
) {
    object WhatsApp : SocialPlatform(
        name = "WhatsApp",
        url = "https://whatsapp.com/channel/placeholder",
        icon = Icons.Default.Chat,
        backgroundBrush = Brush.linearGradient(listOf(WhatsAppGreen, WhatsAppGreen))
    )

    object Telegram : SocialPlatform(
        name = "Telegram",
        url = "https://t.me/placeholder",
        icon = Icons.Default.Send,
        backgroundBrush = Brush.linearGradient(listOf(TelegramBlue, TelegramBlue))
    )

    object Instagram : SocialPlatform(
        name = "Instagram",
        url = "https://instagram.com/SmartDrivo",
        icon = Icons.Default.CameraAlt,
        backgroundBrush = Brush.horizontalGradient(
            listOf(InstagramPurple, InstagramPink, InstagramOrange)
        )
    )
}

@Composable
fun SocialButton(
    platform: SocialPlatform,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(platform.backgroundBrush)
            .clickable {
                if (onClick != null) {
                    onClick()
                } else {
                    openSocialUrl(context, platform.url)
                }
            }
            .testTag("social_btn_${platform.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = platform.name,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = platform.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private fun openSocialUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Opening $url", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CommunitySocialRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SocialButton(
            platform = SocialPlatform.WhatsApp,
            modifier = Modifier.weight(1f)
        )
        SocialButton(
            platform = SocialPlatform.Telegram,
            modifier = Modifier.weight(1f)
        )
        SocialButton(
            platform = SocialPlatform.Instagram,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "SocialButton Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SocialButtonDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        CommunitySocialRow(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "SocialButton Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun SocialButtonLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        CommunitySocialRow(modifier = Modifier.padding(16.dp))
    }
}
