package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.local.entity.ExpenseEntity

val defaultExpenseCategories = listOf(
    "Inventory & Stock",
    "Payroll & Wages",
    "Rent & Office",
    "Marketing & Ads",
    "Utilities",
    "Supplies & Ops",
    "Equipment & Tech",
    "Logistics & Shipping",
    "Tax & Accounting",
    "Maintenance",
    "Miscellaneous"
)

val defaultPaymentMethods = listOf(
    "Bank Transfer",
    "Credit Card",
    "Cash",
    "Debit Card",
    "UPI/Digital",
    "Check"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    expenseToEdit: ExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit
) {
    var title by remember { mutableStateOf(expenseToEdit?.title ?: "") }
    var amountText by remember { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(expenseToEdit?.category ?: defaultExpenseCategories[0]) }
    var paymentMethod by remember { mutableStateOf(expenseToEdit?.paymentMethod ?: defaultPaymentMethods[0]) }
    var receiptTag by remember { mutableStateOf(expenseToEdit?.receiptTag ?: "") }
    var notes by remember { mutableStateOf(expenseToEdit?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (expenseToEdit == null) "Record New Expense" else "Edit Expense",
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
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        isError = false
                    },
                    label = { Text("Expense Title / Item *") },
                    placeholder = { Text("e.g. Office Electricity Bill") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    isError = isError && title.isBlank(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = { Text("Amount *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (amountText.toDoubleOrNull() == null || amountText.toDoubleOrNull()!! <= 0),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        defaultExpenseCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
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
                        defaultPaymentMethods.forEach { method ->
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

                // Receipt Tag / Invoice #
                OutlinedTextField(
                    value = receiptTag,
                    onValueChange = { receiptTag = it },
                    label = { Text("Invoice / Receipt # (Optional)") },
                    placeholder = { Text("e.g. INV-2026-088") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description (Optional)") },
                    placeholder = { Text("Add details or vendor info") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) {
                        onSave(
                            ExpenseEntity(
                                id = expenseToEdit?.id ?: 0,
                                title = title.trim(),
                                amount = amt,
                                category = category,
                                dateMillis = expenseToEdit?.dateMillis ?: System.currentTimeMillis(),
                                paymentMethod = paymentMethod,
                                receiptTag = receiptTag.trim(),
                                notes = notes.trim()
                            )
                        )
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_expense_button")
            ) {
                Text("Save Expense")
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
