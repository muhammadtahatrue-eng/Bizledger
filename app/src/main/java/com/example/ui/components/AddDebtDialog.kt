package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.BizLedgerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtDialog(
    debtToEdit: DebtEntity? = null,
    onDismiss: () -> Unit,
    onSave: (DebtEntity) -> Unit
) {
    var partyName by remember { mutableStateOf(debtToEdit?.partyName ?: "") }
    var debtType by remember { mutableStateOf(debtToEdit?.type ?: DebtType.RECEIVABLE) }
    var totalAmountText by remember { mutableStateOf(debtToEdit?.totalAmount?.toString() ?: "") }
    var contactInfo by remember { mutableStateOf(debtToEdit?.contactInfo ?: "") }
    var notes by remember { mutableStateOf(debtToEdit?.notes ?: "") }
    var dueDateMillis by remember { mutableStateOf<Long?>(debtToEdit?.dueDateMillis) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateMillis ?: (System.currentTimeMillis() + 86400000L * 14)
    )

    var isError by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (debtToEdit == null) "Record Debt / Loan" else "Edit Debt",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector (Owed to Me vs I Owe)
                Text(
                    text = "Debt Direction",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { debtType = DebtType.RECEIVABLE },
                        shape = RoundedCornerShape(12.dp),
                        color = if (debtType == DebtType.RECEIVABLE) IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.5.dp,
                            if (debtType == DebtType.RECEIVABLE) IncomeGreen else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Owed to Me",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (debtType == DebtType.RECEIVABLE) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = { debtType = DebtType.PAYABLE },
                        shape = RoundedCornerShape(12.dp),
                        color = if (debtType == DebtType.PAYABLE) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.5.dp,
                            if (debtType == DebtType.PAYABLE) ExpenseRed else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "I Owe (Payable)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (debtType == DebtType.PAYABLE) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Party Name
                OutlinedTextField(
                    value = partyName,
                    onValueChange = {
                        partyName = it
                        isError = false
                    },
                    label = { Text(if (debtType == DebtType.RECEIVABLE) "Customer / Debtor Name *" else "Vendor / Lender Name *") },
                    placeholder = { Text("e.g. Acme Corp / John Doe") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = isError && partyName.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("debt_party_input")
                )

                // Total Amount
                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = {
                        totalAmountText = it
                        isError = false
                    },
                    label = { Text("Total Amount *") },
                    placeholder = { Text("2500.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (totalAmountText.toDoubleOrNull() == null || totalAmountText.toDoubleOrNull()!! <= 0),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("debt_amount_input")
                )

                // Contact Info
                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Phone / Email / Address (Optional)") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Due Date picker button
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (dueDateMillis != null) "Due Date: ${BizLedgerViewModel.formatDate(dueDateMillis!!)}" else "Set Due Date (Optional)",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Invoice Ref (Optional)") },
                    placeholder = { Text("e.g. Net 30 terms, invoice #902") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = totalAmountText.toDoubleOrNull()
                    if (partyName.isNotBlank() && amt != null && amt > 0) {
                        onSave(
                            DebtEntity(
                                id = debtToEdit?.id ?: 0,
                                partyName = partyName.trim(),
                                type = debtType,
                                totalAmount = amt,
                                paidAmount = debtToEdit?.paidAmount ?: 0.0,
                                dueDateMillis = dueDateMillis,
                                issueDateMillis = debtToEdit?.issueDateMillis ?: System.currentTimeMillis(),
                                contactInfo = contactInfo.trim(),
                                notes = notes.trim(),
                                isSettled = debtToEdit?.isSettled ?: false
                            )
                        )
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_debt_button")
            ) {
                Text("Save Debt")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
