package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
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
import com.example.data.local.entity.BudgetEntity

val budgetCategoryOptions = listOf(
    "All Expenses",
    "Inventory & Stock",
    "Payroll & Wages",
    "Rent & Office",
    "Marketing & Ads",
    "Utilities",
    "Supplies & Ops",
    "Equipment & Tech",
    "Logistics & Shipping",
    "Maintenance",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetDialog(
    budgetToEdit: BudgetEntity? = null,
    onDismiss: () -> Unit,
    onSave: (BudgetEntity) -> Unit
) {
    var category by remember { mutableStateOf(budgetToEdit?.category ?: budgetCategoryOptions[0]) }
    var limitText by remember { mutableStateOf(budgetToEdit?.monthlyLimit?.toString() ?: "") }
    var notes by remember { mutableStateOf(budgetToEdit?.notes ?: "") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (budgetToEdit == null) "Set Budget Limit" else "Edit Budget",
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
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Budget Category") },
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
                        budgetCategoryOptions.forEach { cat ->
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

                // Monthly Limit
                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        isError = false
                    },
                    label = { Text("Monthly Budget Limit *") },
                    placeholder = { Text("5000.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError && (limitText.toDoubleOrNull() == null || limitText.toDoubleOrNull()!! <= 0),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("budget_limit_input")
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Target cap for Q3 growth") },
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
                    val limit = limitText.toDoubleOrNull()
                    if (limit != null && limit > 0) {
                        onSave(
                            BudgetEntity(
                                id = budgetToEdit?.id ?: 0,
                                category = category,
                                monthlyLimit = limit,
                                notes = notes.trim()
                            )
                        )
                    } else {
                        isError = true
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save Budget")
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
