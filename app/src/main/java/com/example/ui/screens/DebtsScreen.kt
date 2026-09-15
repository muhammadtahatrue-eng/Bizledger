package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.ui.components.DebtItemCard
import com.example.ui.components.StatCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel

val debtFilterOptions = listOf(
    "All" to "All",
    "Receivable" to "Owed to Me",
    "Payable" to "I Owe",
    "Overdue" to "Overdue",
    "Settled" to "Settled"
)

@Composable
fun DebtsScreen(
    uiState: BizLedgerUiState,
    viewModel: BizLedgerViewModel,
    onFilterSelect: (String) -> Unit,
    onAddDebt: () -> Unit,
    onRecordPayment: (DebtEntity) -> Unit,
    onEditDebt: (DebtEntity) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val cur = uiState.currencySymbol
    val summary = uiState.summary

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("debts_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDebt,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_debt")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Debt / Loan")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics Row: Total Receivables vs Payables
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Owed to Me",
                        value = BizLedgerViewModel.formatCurrency(summary.totalReceivables, cur),
                        subtitle = "Pending customer inflow",
                        icon = Icons.Default.ArrowDownward,
                        accentColor = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "I Owe (Payables)",
                        value = BizLedgerViewModel.formatCurrency(summary.totalPayables, cur),
                        subtitle = "Pending vendor outflow",
                        icon = Icons.Default.ArrowUpward,
                        accentColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    debtFilterOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = uiState.selectedDebtFilter == key,
                            onClick = { onFilterSelect(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                    }
                }
            }

            // Debts List
            if (uiState.filteredDebts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No debts matching filter",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to record an invoice receivable or supplier payable",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredDebts, key = { it.id }) { debt ->
                    val paymentsState = viewModel.getPaymentsForDebt(debt.id).collectAsState()

                    DebtItemCard(
                        debt = debt,
                        payments = paymentsState.value,
                        currencySymbol = cur,
                        onRecordPayment = { onRecordPayment(debt) },
                        onEdit = { onEditDebt(debt) },
                        onDelete = { onDeleteDebt(debt) }
                    )
                }
            }
        }
    }
}
