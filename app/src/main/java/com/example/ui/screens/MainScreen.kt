package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.WithdrawalEntity
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddDebtDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddWithdrawalDialog
import com.example.ui.components.AdminUsersDialog
import com.example.ui.components.ExcelReportDialog
import com.example.ui.components.RecordDebtPaymentDialog
import com.example.ui.viewmodel.BizLedgerViewModel

enum class NavTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Overview", Icons.Default.Dashboard),
    EXPENSES("Expenses", Icons.Default.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.PieChart),
    WITHDRAWALS("Withdrawals", Icons.Default.AccountBalanceWallet),
    DEBTS("Debts", Icons.Default.SwapHoriz)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BizLedgerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // If user is not logged in, show AuthScreen (Sign in / Register)
    if (!uiState.isLoggedIn) {
        AuthScreen(
            uiState = uiState,
            viewModel = viewModel,
            modifier = modifier
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog States
    var showAdminDialog by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }

    var showAddWithdrawalDialog by remember { mutableStateOf(false) }
    var withdrawalToEdit by remember { mutableStateOf<WithdrawalEntity?>(null) }

    var showAddDebtDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<DebtEntity?>(null) }

    var debtForPayment by remember { mutableStateOf<DebtEntity?>(null) }

    val isAdmin = uiState.currentUser?.role == UserRole.ADMIN
    val pendingApprovalsCount = uiState.pendingUsers.size

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("main_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BizLedger",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "ADMIN",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = uiState.currentUser?.fullName ?: "Logged In",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    // If Admin, show Quick Admin Approvals Button
                    if (isAdmin) {
                        BadgedBox(
                            badge = {
                                if (pendingApprovalsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text("$pendingApprovalsCount")
                                    }
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(
                                onClick = { showAdminDialog = true },
                                modifier = Modifier.testTag("top_bar_admin_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Approval Panel",
                                    tint = if (pendingApprovalsCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Currency Badge (Fixed to PKR)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "PKR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // User Profile Dropdown
                    Box {
                        IconButton(
                            onClick = { showUserMenu = true },
                            modifier = Modifier.testTag("user_profile_menu_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isAdmin) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.currentUser?.fullName?.take(1)?.uppercase() ?: "U",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                                Text(
                                    text = uiState.currentUser?.fullName ?: "User",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "@${uiState.currentUser?.username} • ${if (isAdmin) "Administrator" else "Approved User"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Divider()

                            if (isAdmin) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.AdminPanelSettings,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (pendingApprovalsCount > 0) "Approvals ($pendingApprovalsCount pending)" else "Admin Approvals Panel",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        showAdminDialog = true
                                    },
                                    modifier = Modifier.testTag("menu_admin_panel_item")
                                )
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Log Out / Switch Account",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    viewModel.logout()
                                },
                                modifier = Modifier.testTag("menu_logout_item")
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    uiState = uiState,
                    onPeriodSelect = { viewModel.setTimePeriod(it) },
                    onAddExpense = { showAddExpenseDialog = true },
                    onAddWithdrawal = { showAddWithdrawalDialog = true },
                    onAddBudget = { showAddBudgetDialog = true },
                    onAddDebt = { showAddDebtDialog = true },
                    onEditExpense = {
                        expenseToEdit = it
                        showAddExpenseDialog = true
                    },
                    onDeleteExpense = { viewModel.deleteExpense(it) },
                    onNavigateToExpenses = { selectedTab = 1 },
                    onNavigateToDebts = { selectedTab = 4 }
                )
                1 -> ExpensesScreen(
                    uiState = uiState,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setExpenseCategoryFilter(it) },
                    onAddExpense = {
                        expenseToEdit = null
                        showAddExpenseDialog = true
                    },
                    onEditExpense = {
                        expenseToEdit = it
                        showAddExpenseDialog = true
                    },
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                2 -> BudgetsScreen(
                    uiState = uiState,
                    onAddBudget = {
                        budgetToEdit = null
                        showAddBudgetDialog = true
                    },
                    onEditBudget = {
                        budgetToEdit = it
                        showAddBudgetDialog = true
                    },
                    onDeleteBudget = { viewModel.deleteBudget(it) }
                )
                3 -> WithdrawalsScreen(
                    uiState = uiState,
                    onAddWithdrawal = {
                        withdrawalToEdit = null
                        showAddWithdrawalDialog = true
                    },
                    onEditWithdrawal = {
                        withdrawalToEdit = it
                        showAddWithdrawalDialog = true
                    },
                    onDeleteWithdrawal = { viewModel.deleteWithdrawal(it) }
                )
                4 -> DebtsScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onFilterSelect = { viewModel.setDebtFilter(it) },
                    onAddDebt = {
                        debtToEdit = null
                        showAddDebtDialog = true
                    },
                    onRecordPayment = { debt ->
                        debtForPayment = debt
                    },
                    onEditDebt = {
                        debtToEdit = it
                        showAddDebtDialog = true
                    },
                    onDeleteDebt = { viewModel.deleteDebt(it) }
                )
            }
        }
    }

    // Admin Users Approval Dialog
    if (showAdminDialog && isAdmin) {
        AdminUsersDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showAdminDialog = false }
        )
    }

    // Modal Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            expenseToEdit = expenseToEdit,
            onDismiss = {
                showAddExpenseDialog = false
                expenseToEdit = null
            },
            onSave = { expense ->
                if (expenseToEdit != null) {
                    viewModel.updateExpense(expense)
                } else {
                    viewModel.addExpense(expense)
                }
                showAddExpenseDialog = false
                expenseToEdit = null
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            budgetToEdit = budgetToEdit,
            onDismiss = {
                showAddBudgetDialog = false
                budgetToEdit = null
            },
            onSave = { budget ->
                if (budgetToEdit != null) {
                    viewModel.updateBudget(budget)
                } else {
                    viewModel.addBudget(budget)
                }
                showAddBudgetDialog = false
                budgetToEdit = null
            }
        )
    }

    if (showAddWithdrawalDialog) {
        AddWithdrawalDialog(
            withdrawalToEdit = withdrawalToEdit,
            onDismiss = {
                showAddWithdrawalDialog = false
                withdrawalToEdit = null
            },
            onSave = { withdrawal ->
                if (withdrawalToEdit != null) {
                    viewModel.updateWithdrawal(withdrawal)
                } else {
                    viewModel.addWithdrawal(withdrawal)
                }
                showAddWithdrawalDialog = false
                withdrawalToEdit = null
            }
        )
    }

    if (showAddDebtDialog) {
        AddDebtDialog(
            debtToEdit = debtToEdit,
            onDismiss = {
                showAddDebtDialog = false
                debtToEdit = null
            },
            onSave = { debt ->
                if (debtToEdit != null) {
                    viewModel.updateDebt(debt)
                } else {
                    viewModel.addDebt(debt)
                }
                showAddDebtDialog = false
                debtToEdit = null
            }
        )
    }

    if (debtForPayment != null) {
        RecordDebtPaymentDialog(
            debt = debtForPayment!!,
            currencySymbol = uiState.currencySymbol,
            onDismiss = { debtForPayment = null },
            onConfirm = { amount, notes ->
                viewModel.recordDebtPayment(debtForPayment!!.id, amount, notes)
                debtForPayment = null
            }
        )
    }
}
