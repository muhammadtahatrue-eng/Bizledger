package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.ExpenseEntity
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.ExpenseItemCard
import com.example.ui.components.StatCard
import com.example.ui.theme.BudgetBlue
import com.example.ui.theme.Emerald600
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WithdrawAmber
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel
import com.example.ui.viewmodel.TimePeriodFilter

@Composable
fun DashboardScreen(
    uiState: BizLedgerUiState,
    onPeriodSelect: (TimePeriodFilter) -> Unit,
    onAddExpense: () -> Unit,
    onAddWithdrawal: () -> Unit,
    onAddBudget: () -> Unit,
    onAddDebt: () -> Unit,
    onEditExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToDebts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary
    val cur = uiState.currencySymbol

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Period Selector Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimePeriodFilter.values().forEach { period ->
                    FilterChip(
                        selected = uiState.selectedPeriod == period,
                        onClick = { onPeriodSelect(period) },
                        label = { Text(period.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("period_chip_${period.name}")
                    )
                }
            }
        }

        // 2. Net Outflow Hero Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net Cash Outflow (${uiState.selectedPeriod.displayName})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Expenses + Draws",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = BizLedgerViewModel.formatCurrency(summary.netCashOutflow, cur),
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Operating Expenses",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = BizLedgerViewModel.formatCurrency(summary.totalExpenses, cur),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Owner Withdrawals",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = BizLedgerViewModel.formatCurrency(summary.totalWithdrawals, cur),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Action Buttons Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Add Expense
                Button(
                    onClick = onAddExpense,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("quick_add_expense")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Expense", fontWeight = FontWeight.Bold)
                }

                // Add Withdrawal
                Button(
                    onClick = onAddWithdrawal,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WithdrawAmber),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("quick_add_withdrawal")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdrawal", fontWeight = FontWeight.Bold)
                }

                // Add Debt
                Button(
                    onClick = onAddDebt,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("quick_add_debt")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Debt / Loan", fontWeight = FontWeight.Bold)
                }

                // Add Budget
                OutlinedButton(
                    onClick = onAddBudget,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("quick_add_budget")
                ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Budget")
                }
            }
        }

        // 5. Financial Health Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Receivables (Owed to Us)
                StatCard(
                    title = "Receivables",
                    value = BizLedgerViewModel.formatCurrency(summary.totalReceivables, cur),
                    subtitle = "Owed by clients",
                    icon = Icons.Default.ArrowDownward,
                    accentColor = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )

                // Payables (We Owe)
                StatCard(
                    title = "Payables",
                    value = BizLedgerViewModel.formatCurrency(summary.totalPayables, cur),
                    subtitle = "Owed to vendors",
                    icon = Icons.Default.ArrowUpward,
                    accentColor = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 6. Overall Budget Health Progress Card
        item {
            val budgetLimit = summary.totalOverallBudget
            val budgetSpent = summary.totalSpentInBudget
            val budgetPct = if (budgetLimit > 0) (budgetSpent / budgetLimit).toFloat() else 0f
            val isOver = budgetSpent > budgetLimit && budgetLimit > 0
            val isCaution = !isOver && budgetPct >= 0.8f

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Budget Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${BizLedgerViewModel.formatCurrency(budgetSpent, cur)} of ${BizLedgerViewModel.formatCurrency(budgetLimit, cur)} cap",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${(budgetPct * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isOver) ExpenseRed else if (isCaution) WithdrawAmber else Emerald600
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { budgetPct.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isOver) ExpenseRed else if (isCaution) WithdrawAmber else Emerald600,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )

                    if (isOver || isCaution) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isOver) ExpenseRed else WithdrawAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOver) "Budget exceeded by ${BizLedgerViewModel.formatCurrency(budgetSpent - budgetLimit, cur)}" else "Nearing monthly budget limit",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (isOver) ExpenseRed else WithdrawAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 7. Donut / Breakdown Chart
        item {
            CategoryDonutChart(
                stats = summary.categoryBreakdown,
                totalAmount = summary.totalExpenses,
                currencySymbol = cur
            )
        }

        // 8. Overdue / Pending Debts Quick Alert (if any)
        if (summary.overdueDebtsCount > 0 || summary.pendingDebtsCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDebts() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (summary.overdueDebtsCount > 0) ExpenseRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (summary.overdueDebtsCount > 0) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = if (summary.overdueDebtsCount > 0) ExpenseRed else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (summary.overdueDebtsCount > 0) "${summary.overdueDebtsCount} Overdue Debts" else "${summary.pendingDebtsCount} Active Debts",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (summary.overdueDebtsCount > 0) ExpenseRed else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Receivable: ${BizLedgerViewModel.formatCurrency(summary.totalReceivables, cur)} • Payable: ${BizLedgerViewModel.formatCurrency(summary.totalPayables, cur)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "View",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 9. Recent Expenses Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View All (${uiState.filteredExpenses.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToExpenses() }
                )
            }
        }

        // Recent Expenses (top 4)
        if (uiState.filteredExpenses.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expenses recorded for this period yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(uiState.filteredExpenses.take(4), key = { it.id }) { expense ->
                ExpenseItemCard(
                    expense = expense,
                    currencySymbol = cur,
                    onEdit = { onEditExpense(expense) },
                    onDelete = { onDeleteExpense(expense) }
                )
            }
        }
    }
}
