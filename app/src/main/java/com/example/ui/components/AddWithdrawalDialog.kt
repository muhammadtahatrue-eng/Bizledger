package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.WithdrawalEntity

val withdrawalPaymentMethods = listOf(
    "Bank Transfer",
    "Cash",
    "Check",
    "Digital Wallet"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWithdrawalDialog(
    withdrawalToEdit: WithdrawalEntity? = null,
    onDismiss: () -> Unit,
    onSave: (WithdrawalEntity) -> Unit
) {
    var ownerName by remember { mutableStateOf(withdrawalToEdit?.ownerName ?: "") }
    var amountText by remember { mutableStateOf(withdrawalToEdit?.amount?.toString() ?: "") }
    var paymentMethod by remember { mutableStateOf(withdrawalToEdit?.paymentMethod ?: withdrawalPaymentMethods[0]) }
    var reason by remember { mutableStateOf(withdrawalToEdit?.reason ?: "") }

    var methodExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (withdrawalToEdit == null) "Record Owner Withdrawal" else "Edit Withdrawal",
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
                // Owner / Partner Name
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = {
                        ownerName = it
                        isError = false
                    },
                    label = { Text("Owner / Partner Name *") },
                    placeholder = { Text("e.g. Alex (Owner Draw)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = isError && ownerName.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_name_input")
                )

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = { Text("Withdrawal Amount *") },
                    placeholder = { Text("1000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (amountText.toDoubleOrNull() == null || amountText.toDoubleOrNull()!! <= 0),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("withdrawal_amount_input")
                )

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payout Method") },
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        withdrawalPaymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Reason / Purpose
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Purpose / Reason (Optional)") },
                    placeholder = { Text("e.g. Monthly personal draw, profit distribution") },
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
                    if (ownerName.isNotBlank() && amt != null && amt > 0) {
                        onSave(
                            WithdrawalEntity(
                                id = withdrawalToEdit?.id ?: 0,
                                ownerName = ownerName.trim(),
                                amount = amt,
                                dateMillis = withdrawalToEdit?.dateMillis ?: System.currentTimeMillis(),
                                paymentMethod = paymentMethod,
                                reason = reason.trim()
                            )
                        )
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_withdrawal_button")
            ) {
                Text("Save Withdrawal")
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
