package com.ridemarter.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridemarter.app.model.UserData
import com.ridemarter.app.ui.theme.BrandGreenPrimary
import com.ridemarter.app.ui.theme.BrandOrangeSecondary
import com.ridemarter.app.ui.theme.DarkBackground
import com.ridemarter.app.ui.theme.DarkCardBorder
import com.ridemarter.app.ui.theme.DarkSurface
import com.ridemarter.app.ui.theme.DarkSurfaceVariant
import com.ridemarter.app.ui.theme.DarkTextMuted
import com.ridemarter.app.ui.theme.DarkTextSecondary
import com.ridemarter.app.viewmodel.AuthViewModel

enum class ApprovalFilter {
    ALL, PENDING, APPROVED, REJECTED, DUPLICATES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverApprovalSheet(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var drivers by remember { mutableStateOf<List<UserData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(ApprovalFilter.ALL) }

    // Dialog state for rejecting a driver
    var driverToReject by remember { mutableStateOf<UserData?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    // Dialog state for deleting duplicate user
    var driverToDelete by remember { mutableStateOf<UserData?>(null) }

    fun refreshDrivers() {
        isLoading = true
        authViewModel.fetchAllDrivers(
            onSuccess = { list ->
                drivers = list
                isLoading = false
            },
            onError = { msg ->
                isLoading = false
                Toast.makeText(context, "Error loading drivers: $msg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshDrivers()
    }

    // Identify potential duplicate phone numbers or emails
    val phoneCounts = remember(drivers) {
        drivers.filter { it.mobile.isNotBlank() }
            .groupingBy { it.mobile.trim() }
            .eachCount()
    }
    val emailCounts = remember(drivers) {
        drivers.filter { it.email.isNotBlank() }
            .groupingBy { it.email.trim().lowercase() }
            .eachCount()
    }

    val filteredDrivers by remember(drivers, searchQuery, selectedFilter, phoneCounts, emailCounts) {
        derivedStateOf {
            drivers.filter { driver ->
                val matchesQuery = searchQuery.isBlank() ||
                        driver.name.contains(searchQuery, ignoreCase = true) ||
                        driver.mobile.contains(searchQuery, ignoreCase = true) ||
                        driver.email.contains(searchQuery, ignoreCase = true) ||
                        driver.userId.contains(searchQuery, ignoreCase = true)

                val isDuplicate = (phoneCounts[driver.mobile.trim()] ?: 0) > 1 ||
                        (emailCounts[driver.email.trim().lowercase()] ?: 0) > 1

                val matchesFilter = when (selectedFilter) {
                    ApprovalFilter.ALL -> true
                    ApprovalFilter.PENDING -> !driver.approved && driver.status != "rejected"
                    ApprovalFilter.APPROVED -> driver.approved || driver.status == "approved"
                    ApprovalFilter.REJECTED -> driver.status == "rejected"
                    ApprovalFilter.DUPLICATES -> isDuplicate
                }

                matchesQuery && matchesFilter
            }
        }
    }

    val totalCount = drivers.size
    val pendingCount = drivers.count { !it.approved && it.status != "rejected" }
    val approvedCount = drivers.count { it.approved || it.status == "approved" }
    val rejectedCount = drivers.count { it.status == "rejected" }
    val duplicateCount = drivers.count {
        (phoneCounts[it.mobile.trim()] ?: 0) > 1 ||
                (emailCounts[it.email.trim().lowercase()] ?: 0) > 1
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkCardBorder)
            )
        },
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .testTag("driver_approval_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrandGreenPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = BrandGreenPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Driver Approval Controls",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Manage approvals & prevent duplicate accounts",
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = { refreshDrivers() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_drivers_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_approval_sheet_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Total", count = totalCount, color = Color.White)
                StatDivider()
                StatItem(label = "Pending", count = pendingCount, color = BrandOrangeSecondary)
                StatDivider()
                StatItem(label = "Approved", count = approvedCount, color = BrandGreenPrimary)
                StatDivider()
                StatItem(label = "Rejected", count = rejectedCount, color = Color(0xFFEF5350))
                if (duplicateCount > 0) {
                    StatDivider()
                    StatItem(label = "Duplicates", count = duplicateCount, color = Color(0xFFFFB74D))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, phone, email, or ID...", color = DarkTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = DarkTextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = DarkTextMuted)
                        }
                    }
                },
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandGreenPrimary,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApprovalFilterChip(
                    label = "All ($totalCount)",
                    selected = selectedFilter == ApprovalFilter.ALL,
                    onClick = { selectedFilter = ApprovalFilter.ALL }
                )
                ApprovalFilterChip(
                    label = "Pending ($pendingCount)",
                    selected = selectedFilter == ApprovalFilter.PENDING,
                    onClick = { selectedFilter = ApprovalFilter.PENDING }
                )
                ApprovalFilterChip(
                    label = "Approved ($approvedCount)",
                    selected = selectedFilter == ApprovalFilter.APPROVED,
                    onClick = { selectedFilter = ApprovalFilter.APPROVED }
                )
                ApprovalFilterChip(
                    label = "Rejected ($rejectedCount)",
                    selected = selectedFilter == ApprovalFilter.REJECTED,
                    onClick = { selectedFilter = ApprovalFilter.REJECTED }
                )
                if (duplicateCount > 0) {
                    ApprovalFilterChip(
                        label = "Duplicates ($duplicateCount)",
                        selected = selectedFilter == ApprovalFilter.DUPLICATES,
                        onClick = { selectedFilter = ApprovalFilter.DUPLICATES }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Driver list
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandGreenPrimary)
                }
            } else if (filteredDrivers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No drivers found",
                            color = DarkTextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search query" else "New registrations will appear here",
                            color = DarkTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredDrivers, key = { it.uid.ifBlank { it.userId } }) { driver ->
                        val hasDuplicatePhone = (phoneCounts[driver.mobile.trim()] ?: 0) > 1
                        val hasDuplicateEmail = (emailCounts[driver.email.trim().lowercase()] ?: 0) > 1

                        DriverCard(
                            driver = driver,
                            hasDuplicatePhone = hasDuplicatePhone,
                            hasDuplicateEmail = hasDuplicateEmail,
                            onApprove = {
                                authViewModel.updateDriverApproval(
                                    driverUid = driver.uid,
                                    approved = true,
                                    status = "approved",
                                    onSuccess = {
                                        Toast.makeText(context, "Driver ${driver.name.ifBlank { driver.userId }} Approved!", Toast.LENGTH_SHORT).show()
                                        refreshDrivers()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onReject = {
                                driverToReject = driver
                                rejectionReasonInput = "Documentation unverified or incomplete"
                            },
                            onResetPending = {
                                authViewModel.updateDriverApproval(
                                    driverUid = driver.uid,
                                    approved = false,
                                    status = "pending",
                                    onSuccess = {
                                        Toast.makeText(context, "Reset to Pending", Toast.LENGTH_SHORT).show()
                                        refreshDrivers()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            onDelete = {
                                driverToDelete = driver
                            }
                        )
                    }
                }
            }
        }
    }

    // Rejection Dialog
    if (driverToReject != null) {
        val target = driverToReject!!
        AlertDialog(
            onDismissRequest = { driverToReject = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Reject Driver Application",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Specify a reason for rejecting ${target.name.ifBlank { target.userId }}. The driver will see this reason in their app.",
                        color = DarkTextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = rejectionReasonInput,
                        onValueChange = { rejectionReasonInput = it },
                        label = { Text("Rejection Reason", color = DarkTextMuted) },
                        placeholder = { Text("e.g. Duplicate account, Invalid phone number", color = DarkTextMuted) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOrangeSecondary,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = rejectionReasonInput.trim().ifEmpty { "Application not approved by administrator." }
                        authViewModel.updateDriverApproval(
                            driverUid = target.uid,
                            approved = false,
                            status = "rejected",
                            rejectionReason = reason,
                            onSuccess = {
                                Toast.makeText(context, "Driver application rejected", Toast.LENGTH_SHORT).show()
                                driverToReject = null
                                refreshDrivers()
                            },
                            onError = { msg ->
                                Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) {
                    Text("Reject Driver", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { driverToReject = null }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (driverToDelete != null) {
        val target = driverToDelete!!
        AlertDialog(
            onDismissRequest = { driverToDelete = null },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Delete Duplicate Record?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete the driver record for ${target.name.ifBlank { target.userId }} (${target.mobile})? This helps eliminate duplicate or test entries.",
                    color = DarkTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.deleteDriverRecord(
                            driverUid = target.uid,
                            onSuccess = {
                                Toast.makeText(context, "Driver record removed", Toast.LENGTH_SHORT).show()
                                driverToDelete = null
                                refreshDrivers()
                            },
                            onError = { msg ->
                                Toast.makeText(context, "Failed: $msg", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete Record", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { driverToDelete = null }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = DarkTextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(DarkCardBorder)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApprovalFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandGreenPrimary.copy(alpha = 0.2f),
            selectedLabelColor = BrandGreenPrimary,
            containerColor = DarkSurface,
            labelColor = DarkTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = DarkCardBorder,
            selectedBorderColor = BrandGreenPrimary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}

@Composable
private fun DriverCard(
    driver: UserData,
    hasDuplicatePhone: Boolean,
    hasDuplicateEmail: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onResetPending: () -> Unit,
    onDelete: () -> Unit
) {
    val isApproved = driver.approved || driver.status == "approved"
    val isRejected = driver.status == "rejected"
    val isPending = !isApproved && !isRejected

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(
            1.dp,
            when {
                hasDuplicatePhone || hasDuplicateEmail -> Color(0xFFFFB74D).copy(alpha = 0.6f)
                isApproved -> BrandGreenPrimary.copy(alpha = 0.3f)
                isRejected -> Color(0xFFEF5350).copy(alpha = 0.3f)
                else -> DarkCardBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_card_${driver.userId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Name + Vehicle + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = driver.name.ifBlank { "Driver (${driver.userId})" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = driver.userId,
                                color = BrandGreenPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${driver.vehicleType}",
                                color = DarkTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isApproved -> BrandGreenPrimary.copy(alpha = 0.15f)
                        isRejected -> Color(0xFFEF5350).copy(alpha = 0.15f)
                        else -> BrandOrangeSecondary.copy(alpha = 0.15f)
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            isApproved -> BrandGreenPrimary.copy(alpha = 0.5f)
                            isRejected -> Color(0xFFEF5350).copy(alpha = 0.5f)
                            else -> BrandOrangeSecondary.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isApproved -> Icons.Default.CheckCircle
                                isRejected -> Icons.Default.Close
                                else -> Icons.Default.HourglassTop
                            },
                            contentDescription = null,
                            tint = when {
                                isApproved -> BrandGreenPrimary
                                isRejected -> Color(0xFFEF5350)
                                else -> BrandOrangeSecondary
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when {
                                isApproved -> "APPROVED"
                                isRejected -> "REJECTED"
                                else -> "PENDING"
                            },
                            color = when {
                                isApproved -> BrandGreenPrimary
                                isRejected -> Color(0xFFEF5350)
                                else -> BrandOrangeSecondary
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details: Mobile & Email with Duplicate Alerts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = DarkTextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = driver.mobile.ifBlank { "No phone provided" },
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                    if (hasDuplicatePhone) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Duplicate Mobile", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (driver.email.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = driver.email,
                            color = DarkTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (hasDuplicateEmail) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Duplicate Email", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (driver.rejectionReason.isNotBlank() && isRejected) {
                    Text(
                        text = "Reason: ${driver.rejectionReason}",
                        color = Color(0xFFEF5350),
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isApproved) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreenPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("approve_driver_${driver.userId}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Approve", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (!isRejected) {
                    OutlinedButton(
                        onClick = onReject,
                        border = BorderStroke(1.dp, Color(0xFFEF5350)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("reject_driver_${driver.userId}")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reject", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (isApproved || isRejected) {
                    OutlinedButton(
                        onClick = onResetPending,
                        border = BorderStroke(1.dp, DarkCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTextSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("reset_driver_${driver.userId}")
                    ) {
                        Text(text = "Reset", fontSize = 12.sp)
                    }
                }

                // Delete Duplicate / Test Driver button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF262626))
                        .testTag("delete_driver_${driver.userId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Duplicate Record",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
