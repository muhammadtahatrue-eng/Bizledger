package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BudgetEntity
import com.example.ui.components.BudgetItemCard
import com.example.ui.theme.BudgetBlue
import com.example.ui.theme.Emerald600
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.WithdrawAmber
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel

@Composable
fun BudgetsScreen(
    uiState: BizLedgerUiState,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val cur = uiState.currencySymbol
    val summary = uiState.summary
    val overallLimit = summary.totalOverallBudget
    val overallSpent = summary.totalSpentInBudget
    val overallPct = if (overallLimit > 0) (overallSpent / overallLimit).toFloat() else 0f
    val isOver = overallSpent > overallLimit && overallLimit > 0

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("budgets_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudget,
                containerColor = BudgetBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_budget")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
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
            // Overall Monthly Cap Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Monthly Financial Ceiling",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tracking operational expenses against set budgets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "Total Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BizLedgerViewModel.formatCurrency(overallSpent, cur),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) ExpenseRed else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Total Budget Cap",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BizLedgerViewModel.formatCurrency(overallLimit, cur),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { overallPct.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (isOver) ExpenseRed else if (overallPct >= 0.8f) WithdrawAmber else Emerald600,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isOver) "Exceeded by ${BizLedgerViewModel.formatCurrency(overallSpent - overallLimit, cur)}"
                                else "${BizLedgerViewModel.formatCurrency(overallLimit - overallSpent, cur)} remaining in budget",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOver) ExpenseRed else Emerald600
                            )

                            Text(
                                text = "${(overallPct * 100).toInt()}% allocated",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Text(
                    text = "Category Budgets (${uiState.budgets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Budget Cards List
            if (uiState.budgets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No category budgets configured",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to set a spending limit for any category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.budgets, key = { it.id }) { budget ->
                    // Calculate spent for this category
                    val catSpent = if (budget.category == "All Expenses" || budget.category == "Total Monthly") {
                        uiState.filteredExpenses.sumOf { it.amount }
                    } else {
                        uiState.filteredExpenses.filter { it.category.equals(budget.category, ignoreCase = true) }
                            .sumOf { it.amount }
                    }

                    BudgetItemCard(
                        budget = budget,
                        spentAmount = catSpent,
                        currencySymbol = cur,
                        onEdit = { onEditBudget(budget) },
                        onDelete = { onDeleteBudget(budget) }
                    )
                }
            }
        }
    }
}
