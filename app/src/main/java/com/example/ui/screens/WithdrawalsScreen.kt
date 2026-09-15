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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WithdrawalEntity
import com.example.ui.components.WithdrawalItemCard
import com.example.ui.theme.WithdrawAmber
import com.example.ui.viewmodel.BizLedgerUiState
import com.example.ui.viewmodel.BizLedgerViewModel

@Composable
fun WithdrawalsScreen(
    uiState: BizLedgerUiState,
    onAddWithdrawal: () -> Unit,
    onEditWithdrawal: (WithdrawalEntity) -> Unit,
    onDeleteWithdrawal: (WithdrawalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val cur = uiState.currencySymbol
    val totalWithdrawalSum = uiState.filteredWithdrawals.sumOf { it.amount }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("withdrawals_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWithdrawal,
                containerColor = WithdrawAmber,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_withdrawal")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Withdrawal")
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
            // Hero Summary Card
            item {
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
                        Text(
                            text = "Owner Drawings & Withdrawals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Track payouts, partner profit distributions, and capital cash-outs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Withdrawn (${uiState.selectedPeriod.displayName})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = BizLedgerViewModel.formatCurrency(totalWithdrawalSum, cur),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = WithdrawAmber
                                )
                            }

                            Text(
                                text = "${uiState.filteredWithdrawals.size} transactions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Withdrawals List
            if (uiState.filteredWithdrawals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No withdrawals recorded",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to record an owner drawing or profit payout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredWithdrawals, key = { it.id }) { withdrawal ->
                    WithdrawalItemCard(
                        withdrawal = withdrawal,
                        currencySymbol = cur,
                        onEdit = { onEditWithdrawal(withdrawal) },
                        onDelete = { onDeleteWithdrawal(withdrawal) }
                    )
                }
            }
        }
    }
}
