package com.smartdrivo.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartdrivo.app.model.AreaGroup
import com.smartdrivo.app.ui.theme.SmartDrivoTheme
import com.smartdrivo.app.viewmodel.AreaGroupsViewModel
import com.smartdrivo.app.viewmodel.ThemeMode
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AreaGroupsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AreaGroupsViewModel = viewModel()
) {
    val gotoGroups by viewModel.gotoGroups.collectAsStateWithLifecycle()
    val noGoGroups by viewModel.noGoGroups.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val globalMinPickup by viewModel.globalMinPickup.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddGroupDialog.collectAsStateWithLifecycle()
    val newGroupName by viewModel.newGroupName.collectAsStateWithLifecycle()
    val newGroupType by viewModel.newGroupType.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var expandedGroupId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<AreaGroup?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("area_groups_screen")
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onNavigateBack != null) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("area_groups_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart Area Groups",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Control where you accept rides",
                    color = Color(0xFFAAAAAA),
                    fontSize = 12.sp
                )
            }
        }

        // CUSTOM STYLED TAB ROW
        val indicatorColor = if (selectedTab == 0) Color(0xFF00C853) else Color(0xFFD50000)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0D0D0D),
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp)
                            .background(indicatorColor, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    )
                }
            },
            divider = {
                HorizontalDivider(color = Color(0xFF222222), thickness = 1.dp)
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "GO TO Areas",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Color(0xFF00C853) else Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "NO GO Areas",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Color(0xFFD50000) else Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
            )
        }

        // TAB CONTENT
        when (selectedTab) {
            0 -> {
                // === TAB 0: GO TO AREAS ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("goto_areas_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ITEM 1 - Global Pickup Distance Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("global_min_pickup_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Global Min Pickup",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Applies to all GO TO groups",
                                            color = Color(0xFFAAAAAA),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = "${String.format(Locale.ROOT, "%.1f", globalMinPickup)}km",
                                        color = Color(0xFF00C853),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Slider(
                                    value = globalMinPickup,
                                    onValueChange = { viewModel.saveGlobalMinPickup(it) },
                                    valueRange = 0.5f..10.0f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF00C853),
                                        activeTrackColor = Color(0xFF00C853),
                                        inactiveTrackColor = Color(0xFF333333)
                                    )
                                )
                            }
                        }
                    }

                    // ITEM 2 - Section header Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your GO TO Areas",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = { viewModel.showAddDialog("GOTO") },
                                shape = RoundedCornerShape(20.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00C853))
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_goto_group_button")
                            ) {
                                Text(
                                    text = "+ Add Group",
                                    color = Color(0xFF00C853),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ITEMS 3+ - Group Cards or Empty State
                    if (gotoGroups.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No GO TO Areas",
                                subtitle = "Add areas where you want to accept rides",
                                buttonText = "+ Create First Group",
                                accentColor = Color(0xFF00C853),
                                onAddClick = { viewModel.showAddDialog("GOTO") }
                            )
                        }
                    } else {
                        items(gotoGroups, key = { it.id }) { group ->
                            AreaGroupCard(
                                group = group,
                                accentColor = Color(0xFF00C853),
                                isExpanded = expandedGroupId == group.id,
                                onToggleExpand = {
                                    expandedGroupId = if (expandedGroupId == group.id) null else group.id
                                },
                                onToggleEnable = { viewModel.toggleGroup(group) },
                                onAddKeyword = { kw -> viewModel.addKeyword(group, kw) },
                                onRemoveKeyword = { kw -> viewModel.removeKeyword(group, kw) },
                                onDelete = { showDeleteDialog = group },
                                onUpdateDistances = { min, max ->
                                    viewModel.updateGroup(group.copy(minPickupKm = min, maxDropKm = max))
                                },
                                onUpdateNoGoAction = { action ->
                                    viewModel.updateGroup(group.copy(noGoAction = action))
                                },
                                showNoGoAction = false
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            1 -> {
                // === TAB 1: NO GO AREAS ===
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("nogo_areas_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section header Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NO GO / Unsafe Areas",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Automated reject or ignore for risky zones",
                                    color = Color(0xFFAAAAAA),
                                    fontSize = 11.sp
                                )
                            }
                            OutlinedButton(
                                onClick = { viewModel.showAddDialog("NOGO") },
                                shape = RoundedCornerShape(20.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD50000))
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_nogo_group_button")
                            ) {
                                Text(
                                    text = "+ Add NO GO Group",
                                    color = Color(0xFFD50000),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Group Cards or Empty State
                    if (noGoGroups.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No NO GO Areas",
                                subtitle = "Add areas you want to avoid",
                                buttonText = "+ Create First Group",
                                accentColor = Color(0xFFD50000),
                                onAddClick = { viewModel.showAddDialog("NOGO") }
                            )
                        }
                    } else {
                        items(noGoGroups, key = { it.id }) { group ->
                            AreaGroupCard(
                                group = group,
                                accentColor = Color(0xFFD50000),
                                isExpanded = expandedGroupId == group.id,
                                onToggleExpand = {
                                    expandedGroupId = if (expandedGroupId == group.id) null else group.id
                                },
                                onToggleEnable = { viewModel.toggleGroup(group) },
                                onAddKeyword = { kw -> viewModel.addKeyword(group, kw) },
                                onRemoveKeyword = { kw -> viewModel.removeKeyword(group, kw) },
                                onDelete = { showDeleteDialog = group },
                                onUpdateDistances = { min, max ->
                                    viewModel.updateGroup(group.copy(minPickupKm = min, maxDropKm = max))
                                },
                                onUpdateNoGoAction = { action ->
                                    viewModel.updateGroup(group.copy(noGoAction = action))
                                },
                                showNoGoAction = true
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // DIALOG: ADD GROUP
    if (showAddDialog) {
        val groupTypeLabel = if (newGroupType == "GOTO") "GO TO" else "NO GO"
        AlertDialog(
            onDismissRequest = { viewModel.hideAddDialog() },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "Add $groupTypeLabel Area Group",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a descriptive name for this group:",
                        fontSize = 13.sp,
                        color = Color(0xFFAAAAAA)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { viewModel.onNewGroupNameChange(it) },
                        placeholder = { Text("e.g. City Center or Far Suburbs", color = Color(0xFF666666)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = if (newGroupType == "GOTO") Color(0xFF00C853) else Color(0xFFD50000),
                            unfocusedBorderColor = Color(0xFF444444)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_group_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addGroup(newGroupName, newGroupType) },
                    enabled = newGroupName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newGroupType == "GOTO") Color(0xFF00C853) else Color(0xFFD50000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_group_confirm_button")
                ) {
                    Text("Add Group", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.hideAddDialog() }
                ) {
                    Text("Cancel", color = Color(0xFFAAAAAA))
                }
            }
        )
    }

    // DIALOG: CONFIRM DELETE
    if (showDeleteDialog != null) {
        val groupToDelete = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.White,
            title = {
                Text(
                    text = "Delete Area Group",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${groupToDelete.name}'? This cannot be undone.",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGroup(groupToDelete.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD50000),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("delete_group_confirm_button")
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = Color(0xFFAAAAAA))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AreaGroupCard(
    group: AreaGroup,
    accentColor: Color,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleEnable: () -> Unit,
    onAddKeyword: (String) -> Unit,
    onRemoveKeyword: (String) -> Unit,
    onDelete: () -> Unit,
    onUpdateDistances: (Float, Float) -> Unit,
    onUpdateNoGoAction: (String) -> Unit,
    showNoGoAction: Boolean
) {
    var localKeyword by remember { mutableStateOf("") }
    var localMinPickup by remember(group.minPickupKm) { mutableFloatStateOf(group.minPickupKm) }
    var localMaxDrop by remember(group.maxDropKm) { mutableFloatStateOf(group.maxDropKm) }
    var selectedNoGoAction by remember(group.noGoAction) { mutableStateOf(group.noGoAction) }

    val leftBorderColor = if (group.isEnabled) accentColor else Color(0xFF444444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("area_group_card_${group.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left border accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isExpanded) 340.dp else 70.dp)
                    .background(leftBorderColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // COLLAPSED VIEW (always shown)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleExpand() }
                    ) {
                        Text(
                            text = group.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${group.keywords.size} areas • ${if (group.isEnabled) "Active" else "Disabled"}",
                            color = Color(0xFFAAAAAA),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = group.isEnabled,
                        onCheckedChange = { onToggleEnable() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = accentColor,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF333333)
                        ),
                        modifier = Modifier.testTag("group_switch_${group.id}")
                    )

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.testTag("group_expand_${group.id}")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.Gray
                        )
                    }
                }

                // EXPANDED VIEW
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // MIN PICKUP DISTANCE SLIDER
                        Text("Min Pickup Distance", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = localMinPickup,
                                onValueChange = {
                                    val rounded = (it * 10f).roundToInt() / 10f
                                    localMinPickup = rounded
                                },
                                onValueChangeFinished = {
                                    onUpdateDistances(localMinPickup, localMaxDrop)
                                },
                                valueRange = 0.5f..10.0f,
                                steps = 18,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = Color(0xFF333333)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format(Locale.ROOT, "%.1f", localMinPickup)}km",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // MAX DROP DISTANCE SLIDER
                        Text("Max Drop Distance", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = localMaxDrop,
                                onValueChange = {
                                    val rounded = (it * 10f).roundToInt() / 10f
                                    localMaxDrop = rounded
                                },
                                onValueChangeFinished = {
                                    onUpdateDistances(localMinPickup, localMaxDrop)
                                },
                                valueRange = 0.5f..30.0f,
                                steps = 58,
                                colors = SliderDefaults.colors(
                                    thumbColor = accentColor,
                                    activeTrackColor = accentColor,
                                    inactiveTrackColor = Color(0xFF333333)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${String.format(Locale.ROOT, "%.1f", localMaxDrop)}km",
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.End
                            )
                        }

                        // NO GO ACTION (when showNoGoAction = true)
                        if (showNoGoAction) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Action for this zone", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("REJECT", "IGNORE").forEach { action ->
                                    val isSelected = selectedNoGoAction == action
                                    val btnBg = when {
                                        isSelected && action == "REJECT" -> Color(0xFFD50000)
                                        isSelected && action == "IGNORE" -> Color(0xFFFF6D00)
                                        else -> Color.Transparent
                                    }
                                    Button(
                                        onClick = {
                                            selectedNoGoAction = action
                                            onUpdateNoGoAction(action)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = btnBg,
                                            contentColor = if (isSelected) Color.White else Color(0xFFAAAAAA)
                                        ),
                                        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF444444))
                                        ) else null,
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(action, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // KEYWORDS SECTION
                        Text("Area Names / Keywords", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (group.keywords.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                group.keywords.forEach { keyword ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF282828),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = keyword,
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove $keyword",
                                                tint = Color(0xFFAAAAAA),
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { onRemoveKeyword(keyword) }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Add keyword input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = localKeyword,
                                onValueChange = { localKeyword = it },
                                placeholder = { Text("e.g. Indiranagar", color = Color(0xFF666666), fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (localKeyword.isNotBlank()) {
                                        onAddKeyword(localKeyword.trim())
                                        localKeyword = ""
                                    }
                                },
                                enabled = localKeyword.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add keyword",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // DELETE GROUP BUTTON
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onDelete,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFD50000))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    title: String,
    subtitle: String,
    buttonText: String,
    accentColor: Color,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            color = Color(0xFFAAAAAA),
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = buttonText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(name = "AreaGroupsScreen Dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AreaGroupsScreenDarkPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.DARK) {
        AreaGroupsScreen()
    }
}

@Preview(name = "AreaGroupsScreen Light", showBackground = true, backgroundColor = 0xFFF8F9FA)
@Composable
fun AreaGroupsScreenLightPreview() {
    SmartDrivoTheme(themeMode = ThemeMode.LIGHT) {
        AreaGroupsScreen()
    }
}
