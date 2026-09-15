package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.ui.viewmodel.BizLedgerViewModel

@Composable
fun RecordDebtPaymentDialog(
    debt: DebtEntity,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.remainingAmount.toString()) }
    var notes by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val isReceivable = debt.type == DebtType.RECEIVABLE
    val actionTitle = if (isReceivable) "Record Incoming Payment" else "Record Outgoing Payment"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = actionTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "${debt.partyName} • Remaining: ${BizLedgerViewModel.formatCurrency(debt.remainingAmount, currencySymbol)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Full Amount Shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { amountText = debt.remainingAmount.toString() }
                    ) {
                        Text("Pay Full Balance (${BizLedgerViewModel.formatCurrency(debt.remainingAmount, currencySymbol)})")
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = { Text("Payment Amount *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (amountText.toDoubleOrNull() == null || amountText.toDoubleOrNull()!! <= 0),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payment Note / Reference (Optional)") },
                    placeholder = { Text("e.g. Bank wire #TRX-9182") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onConfirm(amt, notes.trim())
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Confirm Payment")
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
