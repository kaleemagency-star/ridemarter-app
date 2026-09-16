package com.ridemarter.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ridemarter.app.model.OrderAlertData
import com.ridemarter.app.ui.overlay.OrderAlertPopupContent

@Composable
fun OrderAlertPopupContent(
    data: OrderAlertData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.ridemarter.app.ui.overlay.OrderAlertPopupContent(
        data = data,
        onDismiss = onDismiss,
        modifier = modifier
    )
}

@Composable
fun OrderAlertPopup(
    data: OrderAlertData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.ridemarter.app.ui.overlay.OrderAlertPopup(
        data = data,
        onDismiss = onDismiss,
        modifier = modifier
    )
}
