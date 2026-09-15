package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Emerald600
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel
import com.example.util.BackupFileInfo
import com.example.util.BackupRestoreResult
import com.example.util.DataBackupManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBackupDialog(
    uiState: BizLedgerUiState,
    viewModel: BizLedgerViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var backupsList by remember { mutableStateOf<List<BackupFileInfo>>(emptyList()) }
    var isCreatingBackup by remember { mutableStateOf(false) }
    var backupSuccessMessage by remember { mutableStateOf<String?>(null) }
    var restoreDialogBackup by remember { mutableStateOf<BackupFileInfo?>(null) }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreResult by remember { mutableStateOf<BackupRestoreResult?>(null) }
    var backupToDelete by remember { mutableStateOf<BackupFileInfo?>(null) }

    fun refreshBackups() {
        backupsList = viewModel.getDeviceBackups(context)
    }

    LaunchedEffect(Unit) {
        refreshBackups()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("data_backup_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Admin Device Backup & Recovery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Excel CSV master archives & database snapshots",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_backup_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Callout: Create New Device Backup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Current Database State",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${uiState.expenses.size} Expenses • ${uiState.withdrawals.size} Withdrawals • ${uiState.debts.size} Debts • ${uiState.budgets.size} Budgets • ${uiState.allUsers.size} Users",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    isCreatingBackup = true
                                    coroutineScope.launch {
                                        try {
                                            val created = viewModel.createDeviceBackup(context)
                                            refreshBackups()
                                            backupSuccessMessage = "Backup created: ${created.csvFileName}"
                                            Toast.makeText(context, "Backup saved to device successfully!", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Failed to create backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isCreatingBackup = false
                                        }
                                    }
                                },
                                enabled = !isCreatingBackup,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("create_backup_btn")
                            ) {
                                if (isCreatingBackup) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Saving...", style = MaterialTheme.typography.labelMedium)
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create Device Backup", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        if (backupSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IncomeGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = backupSuccessMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Title: Device Archive History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Saved Device Backups (${backupsList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { refreshBackups() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Backups",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (backupsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Saved Backups Found on Device",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap 'Create Device Backup' above to create an Excel CSV and JSON snapshot safely on your device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
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
                        items(backupsList, key = { it.id }) { backup ->
                            BackupCardItem(
                                backup = backup,
                                onOpenInExcel = {
                                    if (backup.csvFile.exists()) {
                                        DataBackupManager.openInExcel(context, backup.csvFile)
                                    } else {
                                        Toast.makeText(context, "CSV file not found", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onShare = {
                                    val fileToShare = if (backup.csvFile.exists()) backup.csvFile else backup.jsonFile
                                    DataBackupManager.shareBackup(context, fileToShare, "BizLedger Master Backup ${backup.id}")
                                },
                                onRestore = {
                                    restoreDialogBackup = backup
                                },
                                onDelete = {
                                    backupToDelete = backup
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Information Notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Backups are stored permanently on your admin device storage and can be opened in Microsoft Excel, Google Sheets, or restored back to the app anytime.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Restore
    if (restoreDialogBackup != null) {
        val targetBackup = restoreDialogBackup!!
        AlertDialog(
            onDismissRequest = { if (!isRestoring) restoreDialogBackup = null },
            icon = { Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Restore From Backup Archive?") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to restore data from backup '${targetBackup.fileName}' created on ${targetBackup.formattedDate}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "This will restore all ${targetBackup.totalRecords} records (expenses, debts, budgets, users).",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isRestoring = true
                        coroutineScope.launch {
                            try {
                                val res = viewModel.restoreDeviceBackup(
                                    file = targetBackup.jsonFile,
                                    clearExistingFirst = false
                                )
                                restoreResult = res
                                restoreDialogBackup = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Restore failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            } finally {
                                isRestoring = false
                            }
                        }
                    },
                    enabled = !isRestoring
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restoring...")
                    } else {
                        Text("Restore Records")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { restoreDialogBackup = null },
                    enabled = !isRestoring
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Result Dialog
    if (restoreResult != null) {
        val result = restoreResult!!
        AlertDialog(
            onDismissRequest = { restoreResult = null },
            icon = {
                Icon(
                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.success) IncomeGreen else ExpenseRed
                )
            },
            title = { Text(if (result.success) "Restore Completed Successfully" else "Restore Failed") },
            text = {
                Column {
                    Text(text = result.message, style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                Button(onClick = { restoreResult = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Delete Backup Dialog
    if (backupToDelete != null) {
        val target = backupToDelete!!
        AlertDialog(
            onDismissRequest = { backupToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed) },
            title = { Text("Delete Backup Archive?") },
            text = {
                Text("Are you sure you want to delete '${target.fileName}' from device storage? This cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeviceBackup(context, target)
                        refreshBackups()
                        backupToDelete = null
                        Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { backupToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BackupCardItem(
    backup: BackupFileInfo,
    onOpenInExcel: () -> Unit,
    onShare: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = CardDefaults.outlinedCardBorder()
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald600.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = Emerald600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = backup.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${backup.formattedDate} • ${backup.fileSizeFormatted} • ${backup.totalRecords} Records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = ExpenseRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons for this backup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onOpenInExcel,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel CSV", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontSize = 11.sp)
                }

                Button(
                    onClick = onRestore,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore", fontSize = 11.sp)
                }
            }
        }
    }
}
