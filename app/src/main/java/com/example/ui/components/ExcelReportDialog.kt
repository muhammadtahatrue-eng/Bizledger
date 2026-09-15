package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.entity.DebtType
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportTimelineOption(val title: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    CUSTOM("Custom Timeline")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExcelReportDialog(
    uiState: BizLedgerUiState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTimeline by remember { mutableStateOf(ReportTimelineOption.MONTHLY) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Export Options, 1: CSV Preview

    // Date range states
    val now = System.currentTimeMillis()
    var startMillis by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        mutableLongStateOf(cal.timeInMillis)
    }
    var endMillis by remember { mutableLongStateOf(now) }
    var timelineLabel by remember { mutableStateOf("This Month") }

    // Date picker dialog states for custom timeline
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Function to calculate start/end for timeline option
    fun updateTimeline(option: ReportTimelineOption) {
        selectedTimeline = option
        val cal = Calendar.getInstance()
        when (option) {
            ReportTimelineOption.WEEKLY -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                startMillis = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 7)
                endMillis = cal.timeInMillis - 1
                val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
                timelineLabel = "Weekly (${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))})"
            }
            ReportTimelineOption.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                startMillis = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                endMillis = cal.timeInMillis - 1
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                timelineLabel = "Monthly (${sdf.format(Date(startMillis))})"
            }
            ReportTimelineOption.YEARLY -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                startMillis = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                endMillis = cal.timeInMillis - 1
                val sdf = SimpleDateFormat("yyyy", Locale.getDefault())
                timelineLabel = "Yearly Ledger (${sdf.format(Date(startMillis))})"
            }
            ReportTimelineOption.CUSTOM -> {
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                timelineLabel = "Custom (${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))})"
            }
        }
    }

    // Filter data for the exact chosen report range
    val rangeExpenses = uiState.expenses.filter { it.dateMillis in startMillis..endMillis }
    val rangeWithdrawals = uiState.withdrawals.filter { it.dateMillis in startMillis..endMillis }
    val totalRangeExpenses = rangeExpenses.sumOf { it.amount }
    val totalRangeWithdrawals = rangeWithdrawals.sumOf { it.amount }
    val totalRangeReceivables = uiState.debts.filter { it.type == DebtType.RECEIVABLE && !it.isSettled }.sumOf { it.remainingAmount }
    val totalRangePayables = uiState.debts.filter { it.type == DebtType.PAYABLE && !it.isSettled }.sumOf { it.remainingAmount }
    val netRangeOutflow = totalRangeExpenses + totalRangeWithdrawals

    // Build the CSV spreadsheet text
    val csvContent = remember(startMillis, endMillis, rangeExpenses, rangeWithdrawals, uiState.debts, uiState.budgets) {
        buildCsvReport(
            timelineTitle = timelineLabel,
            startMillis = startMillis,
            endMillis = endMillis,
            currency = uiState.currencySymbol,
            expenses = rangeExpenses,
            withdrawals = rangeWithdrawals,
            debts = uiState.debts,
            budgets = uiState.budgets,
            totalExpenses = totalRangeExpenses,
            totalWithdrawals = totalRangeWithdrawals,
            totalReceivables = totalRangeReceivables,
            totalPayables = totalRangePayables,
            netOutflow = netRangeOutflow
        )
    }

    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = startMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        startMillis = it
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        timelineLabel = "Custom (${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))})"
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    // End Date Picker Dialog
    if (showEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = endMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let {
                        endMillis = it
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        timelineLabel = "Custom (${sdf.format(Date(startMillis))} - ${sdf.format(Date(endMillis))})"
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = "Excel Spreadsheet",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Excel Spreadsheet Generator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Export financial ledger reports to Excel / Sheets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Timeline Option Chips
                Text(
                    text = "SELECT REPORT TIMELINE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReportTimelineOption.values().forEach { option ->
                        FilterChip(
                            selected = selectedTimeline == option,
                            onClick = { updateTimeline(option) },
                            label = { Text(option.title, fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (option) {
                                        ReportTimelineOption.WEEKLY -> Icons.Default.ViewWeek
                                        ReportTimelineOption.MONTHLY -> Icons.Default.CalendarMonth
                                        ReportTimelineOption.YEARLY -> Icons.Default.CalendarToday
                                        ReportTimelineOption.CUSTOM -> Icons.Default.DateRange
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Timeline Pickers if CUSTOM selected
                if (selectedTimeline == ReportTimelineOption.CUSTOM) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Custom Date Range",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showStartDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startMillis)),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showEndDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(endMillis)),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Quick Range Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickRangeChip("Last 30 Days") {
                                    endMillis = System.currentTimeMillis()
                                    startMillis = endMillis - (30L * 86400000L)
                                    updateTimeline(ReportTimelineOption.CUSTOM)
                                }
                                QuickRangeChip("Last 90 Days") {
                                    endMillis = System.currentTimeMillis()
                                    startMillis = endMillis - (90L * 86400000L)
                                    updateTimeline(ReportTimelineOption.CUSTOM)
                                }
                                QuickRangeChip("1 Year") {
                                    endMillis = System.currentTimeMillis()
                                    startMillis = endMillis - (365L * 86400000L)
                                    updateTimeline(ReportTimelineOption.CUSTOM)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Summary Badge of Selected Timeline Data
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timelineLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${rangeExpenses.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(BizLedgerViewModel.formatCurrency(totalRangeExpenses, uiState.currencySymbol), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text("Owner Drawings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(BizLedgerViewModel.formatCurrency(totalRangeWithdrawals, uiState.currencySymbol), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column {
                                Text("Net Outflow", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(BizLedgerViewModel.formatCurrency(netRangeOutflow, uiState.currencySymbol), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Summary vs Raw CSV Preview
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Report Highlights", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("CSV Table Preview", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Highlights Preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReportLine("📅 Date Range:", "${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(startMillis))} – ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(endMillis))}")
                        ReportLine("💳 Operating Expenses:", "${BizLedgerViewModel.formatCurrency(totalRangeExpenses, uiState.currencySymbol)} (${rangeExpenses.size} transactions)")
                        ReportLine("💼 Partner Withdrawals:", "${BizLedgerViewModel.formatCurrency(totalRangeWithdrawals, uiState.currencySymbol)} (${rangeWithdrawals.size} drawings)")
                        ReportLine("📥 Accounts Receivable:", BizLedgerViewModel.formatCurrency(totalRangeReceivables, uiState.currencySymbol))
                        ReportLine("📤 Accounts Payable:", BizLedgerViewModel.formatCurrency(totalRangePayables, uiState.currencySymbol))
                        ReportLine("📊 Net Cash Outflow:", BizLedgerViewModel.formatCurrency(netRangeOutflow, uiState.currencySymbol))
                        ReportLine("📑 Excel Format:", "RFC 4180 CSV (Compatible with Microsoft Excel, Google Sheets, LibreOffice)")
                    }
                } else {
                    // CSV Preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = csvContent.take(1200) + if (csvContent.length > 1200) "\n... [Full data included in Excel export]" else "",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val file = saveCsvToCache(context, csvContent, timelineLabel)
                    if (file != null) {
                        shareOrOpenExcelFile(context, file, "BizLedger_${selectedTimeline.name}_Report.csv")
                    } else {
                        Toast.makeText(context, "Could not generate file. Copying CSV text instead.", Toast.LENGTH_SHORT).show()
                        clipboardManager.setText(AnnotatedString(csvContent))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("export_excel_button")
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export to Excel")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(csvContent))
                        Toast.makeText(context, "CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy CSV")
                }
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun QuickRangeChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ReportLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

private fun buildCsvReport(
    timelineTitle: String,
    startMillis: Long,
    endMillis: Long,
    currency: String,
    expenses: List<com.example.data.local.entity.ExpenseEntity>,
    withdrawals: List<com.example.data.local.entity.WithdrawalEntity>,
    debts: List<com.example.data.local.entity.DebtEntity>,
    budgets: List<com.example.data.local.entity.BudgetEntity>,
    totalExpenses: Double,
    totalWithdrawals: Double,
    totalReceivables: Double,
    totalPayables: Double,
    netOutflow: Double
): String {
    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfNow = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun escapeCsv(s: String): String = "\"${s.replace("\"", "\"\"")}\""

    return buildString {
        // Document Header
        appendLine(escapeCsv("BIZLEDGER BUSINESS FINANCIAL LEDGER REPORT"))
        appendLine("${escapeCsv("Report Timeline")},${escapeCsv(timelineTitle)}")
        appendLine("${escapeCsv("Start Date")},${escapeCsv(sdfDate.format(Date(startMillis)))}")
        appendLine("${escapeCsv("End Date")},${escapeCsv(sdfDate.format(Date(endMillis)))}")
        appendLine("${escapeCsv("Generated Timestamp")},${escapeCsv(sdfNow.format(Date()))}")
        appendLine("${escapeCsv("Currency")},${escapeCsv(currency)}")
        appendLine()

        // 1. Executive Summary Table
        appendLine(escapeCsv("=== 1. EXECUTIVE SUMMARY ==="))
        appendLine("${escapeCsv("Financial Metric")},${escapeCsv("Amount ($currency)")},${escapeCsv("Status / Notes")}")
        appendLine("${escapeCsv("Total Operational Expenses")},${"%.2f".format(totalExpenses)},${escapeCsv("${expenses.size} entries")}")
        appendLine("${escapeCsv("Total Owner Drawings / Withdrawals")},${"%.2f".format(totalWithdrawals)},${escapeCsv("${withdrawals.size} drawings")}")
        appendLine("${escapeCsv("Total Cash Outflow")},${"%.2f".format(netOutflow)},${escapeCsv("Expenses + Withdrawals")}")
        appendLine("${escapeCsv("Total Accounts Receivable (Owed to Us)")},${"%.2f".format(totalReceivables)},${escapeCsv("Customer & Partner Receivables")}")
        appendLine("${escapeCsv("Total Accounts Payable (We Owe)")},${"%.2f".format(totalPayables)},${escapeCsv("Supplier & Lender Payables")}")
        appendLine()

        // 2. Operational Expenses Table
        appendLine(escapeCsv("=== 2. OPERATIONAL EXPENSES LEDGER ==="))
        appendLine("${escapeCsv("ID")},${escapeCsv("Date")},${escapeCsv("Expense Title")},${escapeCsv("Category")},${escapeCsv("Amount ($currency)")},${escapeCsv("Payment Method")},${escapeCsv("Receipt / Invoice Tag")},${escapeCsv("Notes")}")
        if (expenses.isEmpty()) {
            appendLine("${escapeCsv("None")},${escapeCsv("-")},${escapeCsv("No expenses recorded in this period")},${escapeCsv("-")},0.00,${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")}")
        } else {
            expenses.forEachIndexed { idx, exp ->
                appendLine("EXP-${idx + 1},${escapeCsv(sdfDate.format(Date(exp.dateMillis)))},${escapeCsv(exp.title)},${escapeCsv(exp.category)},${"%.2f".format(exp.amount)},${escapeCsv(exp.paymentMethod)},${escapeCsv(exp.receiptTag)},${escapeCsv(exp.notes)}")
            }
        }
        appendLine("${escapeCsv("TOTAL EXPENSES")},,,,,${"%.2f".format(totalExpenses)},,")
        appendLine()

        // 3. Owner Withdrawals Table
        appendLine(escapeCsv("=== 3. OWNER DRAWINGS & WITHDRAWALS ==="))
        appendLine("${escapeCsv("ID")},${escapeCsv("Date")},${escapeCsv("Owner / Partner Name")},${escapeCsv("Amount ($currency)")},${escapeCsv("Payment Method")},${escapeCsv("Purpose / Reason")}")
        if (withdrawals.isEmpty()) {
            appendLine("${escapeCsv("None")},${escapeCsv("-")},${escapeCsv("No withdrawals recorded in this period")},0.00,${escapeCsv("-")},${escapeCsv("-")}")
        } else {
            withdrawals.forEachIndexed { idx, wth ->
                appendLine("WTH-${idx + 1},${escapeCsv(sdfDate.format(Date(wth.dateMillis)))},${escapeCsv(wth.ownerName)},${"%.2f".format(wth.amount)},${escapeCsv(wth.paymentMethod)},${escapeCsv(wth.reason)}")
            }
        }
        appendLine("${escapeCsv("TOTAL WITHDRAWALS")},,,${"%.2f".format(totalWithdrawals)},,")
        appendLine()

        // 4. Accounts Receivable & Payable Table
        appendLine(escapeCsv("=== 4. DEBTS & RECEIVABLES REGISTRY ==="))
        appendLine("${escapeCsv("ID")},${escapeCsv("Party / Counterparty")},${escapeCsv("Type")},${escapeCsv("Total Amount ($currency)")},${escapeCsv("Paid Amount ($currency)")},${escapeCsv("Remaining Balance ($currency)")},${escapeCsv("Due Date")},${escapeCsv("Status")},${escapeCsv("Contact Info")},${escapeCsv("Notes")}")
        if (debts.isEmpty()) {
            appendLine("${escapeCsv("None")},${escapeCsv("No debt records")},${escapeCsv("-")},0.00,0.00,0.00,${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")},${escapeCsv("-")}")
        } else {
            debts.forEachIndexed { idx, debt ->
                val typeStr = if (debt.type == DebtType.RECEIVABLE) "RECEIVABLE (Owed to Us)" else "PAYABLE (We Owe)"
                val statusStr = if (debt.isSettled) "SETTLED" else if (debt.isOverdue) "OVERDUE" else "PENDING"
                val dueStr = debt.dueDateMillis?.let { sdfDate.format(Date(it)) } ?: "None"
                appendLine("DBT-${idx + 1},${escapeCsv(debt.partyName)},${escapeCsv(typeStr)},${"%.2f".format(debt.totalAmount)},${"%.2f".format(debt.paidAmount)},${"%.2f".format(debt.remainingAmount)},${escapeCsv(dueStr)},${escapeCsv(statusStr)},${escapeCsv(debt.contactInfo)},${escapeCsv(debt.notes)}")
            }
        }
        appendLine()

        // 5. Budgets & Allocation Table
        appendLine(escapeCsv("=== 5. BUDGET ALLOCATION & VARIANCE ==="))
        appendLine("${escapeCsv("Category")},${escapeCsv("Monthly Budget Limit ($currency)")},${escapeCsv("Period Spent ($currency)")},${escapeCsv("Variance ($currency)")},${escapeCsv("Status")}")
        if (budgets.isEmpty()) {
            appendLine("${escapeCsv("General")},0.00,${"%.2f".format(totalExpenses)},${"%.2f".format(-totalExpenses)},${escapeCsv("No formal budget defined")}")
        } else {
            budgets.forEach { b ->
                val spentInCat = expenses.filter { it.category.equals(b.category, ignoreCase = true) }.sumOf { it.amount }
                val variance = b.monthlyLimit - spentInCat
                val status = if (variance >= 0) "Within Budget" else "OVER BUDGET"
                appendLine("${escapeCsv(b.category)},${"%.2f".format(b.monthlyLimit)},${"%.2f".format(spentInCat)},${"%.2f".format(variance)},${escapeCsv(status)}")
            }
        }
        appendLine()
        appendLine(escapeCsv("Report exported from BizLedger Android Suite"))
    }
}

private fun saveCsvToCache(context: Context, content: String, title: String): File? {
    return try {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        val cleanTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        val file = File(reportsDir, "BizLedger_${cleanTitle}_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { it.write(content) }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun shareOrOpenExcelFile(context: Context, file: File, fileName: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "BizLedger Financial Spreadsheet Export - $fileName")
            putExtra(Intent.EXTRA_TEXT, "Attached is the generated BizLedger Excel/CSV financial report.")
            type = "text/csv"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Open or Share Excel Spreadsheet Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error opening file share: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
