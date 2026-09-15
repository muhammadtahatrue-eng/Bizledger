package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.UserStatus
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersDialog(
    uiState: BizLedgerUiState,
    viewModel: BizLedgerViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending, 1: Approved, 2: All Users
    var showDirectAddUserDialog by remember { mutableStateOf(false) }
    var showExcelDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("admin_users_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "User Approval & Admin Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Manage registrations, access roles & authorizations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_admin_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val pendingCount = uiState.allUsers.count { it.status == UserStatus.PENDING }
                    val approvedCount = uiState.allUsers.count { it.status == UserStatus.APPROVED }
                    val totalCount = uiState.allUsers.size

                    AdminStatChip(
                        title = "Pending Approvals",
                        count = pendingCount,
                        color = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatChip(
                        title = "Approved Members",
                        count = approvedCount,
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatChip(
                        title = "Total Registered",
                        count = totalCount,
                        color = MaterialTheme.colorScheme.secondary,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs for filtering
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    val pendingCount = uiState.allUsers.count { it.status == UserStatus.PENDING }
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Pending Requests", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$pendingCount",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("admin_tab_pending")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text("Approved Users", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        },
                        modifier = Modifier.testTag("admin_tab_approved")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text("All Accounts", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                        },
                        modifier = Modifier.testTag("admin_tab_all")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Row (Excel Reports, Device Backup, and Add User Directly buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showExcelDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_excel_reports_btn")
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel Reports", style = MaterialTheme.typography.labelMedium)
                    }

                    FilledTonalButton(
                        onClick = { showBackupDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_device_backup_btn")
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Data Backup", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showDirectAddUserDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("admin_add_user_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add User", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filtered List
                val displayedUsers = when (selectedTab) {
                    0 -> uiState.allUsers.filter { it.status == UserStatus.PENDING }
                    1 -> uiState.allUsers.filter { it.status == UserStatus.APPROVED }
                    else -> uiState.allUsers
                }

                if (displayedUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.CheckCircle else Icons.Default.SupervisorAccount,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (selectedTab) {
                                    0 -> "No pending approval requests!"
                                    1 -> "No approved users found."
                                    else -> "No accounts registered yet."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (selectedTab) {
                                    0 -> "New user registrations will appear here for your review."
                                    else -> "Users can register on the login screen."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(displayedUsers, key = { it.id }) { user ->
                            AdminUserCard(
                                user = user,
                                isCurrentUser = user.id == uiState.currentUser?.id,
                                onApprove = { viewModel.approveUser(user.id) },
                                onReject = { viewModel.rejectUser(user.id) },
                                onToggleRole = {
                                    val newRole = if (user.role == UserRole.ADMIN) UserRole.USER else UserRole.ADMIN
                                    viewModel.updateUserRole(user.id, newRole)
                                },
                                onDelete = { userToDelete = user }
                            )
                        }
                    }
                }
            }
        }
    }

    // Direct Add User Dialog
    if (showDirectAddUserDialog) {
        DirectAddUserModal(
            onDismiss = { showDirectAddUserDialog = false },
            onAdd = { fullName, username, email, password, role, preApproved, notes ->
                viewModel.adminCreateUser(fullName, username, email, password, role, preApproved, notes)
                showDirectAddUserDialog = false
            }
        )
    }

    // Excel Reports Dialog (Admin Exclusive)
    if (showExcelDialog) {
        ExcelReportDialog(
            uiState = uiState,
            onDismiss = { showExcelDialog = false }
        )
    }

    // Device Data Backup & Recovery Dialog (Admin Exclusive)
    if (showBackupDialog) {
        DataBackupDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showBackupDialog = false }
        )
    }

    // Delete User Confirmation Dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete User Account?") },
            text = { Text("Are you sure you want to permanently delete the account '${userToDelete?.username}' (${userToDelete?.fullName})?") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminStatChip(
    title: String,
    count: Int,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AdminUserCard(
    user: UserEntity,
    isCurrentUser: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onToggleRole: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_user_card_${user.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (user.role == UserRole.ADMIN) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(You)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "@${user.username} • ${user.email}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge
                when (user.status) {
                    UserStatus.PENDING -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "PENDING APPROVAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    UserStatus.APPROVED -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "APPROVED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    UserStatus.REJECTED -> {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceDim
                        ) {
                            Text(
                                text = "REJECTED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (user.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${user.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Role indicator and toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (user.role == UserRole.ADMIN) Icons.Default.Security else Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Role: ${if (user.role == UserRole.ADMIN) "Admin" else "Standard User"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (user.status == UserStatus.PENDING) {
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("approve_btn_${user.id}")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("reject_btn_${user.id}")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        // For non-pending users
                        if (!isCurrentUser) {
                            TextButton(onClick = onToggleRole) {
                                Text(
                                    text = if (user.role == UserRole.ADMIN) "Make User" else "Make Admin",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (user.status == UserStatus.APPROVED) {
                                TextButton(
                                    onClick = onReject,
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Revoke", style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                TextButton(onClick = onApprove) {
                                    Text("Re-Approve", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DirectAddUserModal(
    onDismiss: () -> Unit,
    onAdd: (fullName: String, username: String, email: String, password: String, role: UserRole, preApproved: Boolean, notes: String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.USER) }
    var preApproved by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = role == UserRole.USER,
                        onClick = { role = UserRole.USER },
                        label = { Text("Standard User") }
                    )
                    FilterChip(
                        selected = role == UserRole.ADMIN,
                        onClick = { role = UserRole.ADMIN },
                        label = { Text("Administrator") }
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Department / Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(fullName, username, email, password, role, preApproved, notes)
                },
                enabled = fullName.isNotBlank() && username.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Create User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
