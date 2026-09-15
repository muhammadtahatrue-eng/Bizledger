package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.UserStatus
import com.example.data.local.entity.WithdrawalEntity
import com.example.data.repository.BizLedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimePeriodFilter(val displayName: String) {
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_QUARTER("This Quarter"),
    THIS_YEAR("This Year"),
    LAST_YEAR("Last Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom Timeline")
}

data class CategoryExpenseStat(
    val category: String,
    val totalSpent: Double,
    val percentage: Float,
    val budget: Double = 0.0,
    val count: Int = 0
)

data class DashboardSummary(
    val totalExpenses: Double = 0.0,
    val totalWithdrawals: Double = 0.0,
    val totalReceivables: Double = 0.0,      // Owed to business
    val totalPayables: Double = 0.0,         // Business owes
    val totalOverallBudget: Double = 0.0,
    val totalSpentInBudget: Double = 0.0,
    val netCashOutflow: Double = 0.0,        // Expenses + Withdrawals
    val pendingDebtsCount: Int = 0,
    val overdueDebtsCount: Int = 0,
    val categoryBreakdown: List<CategoryExpenseStat> = emptyList()
)

data class BizLedgerUiState(
    val currentUser: UserEntity? = null,
    val isLoggedIn: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authError: String? = null,
    val authSuccessMessage: String? = null,
    val allUsers: List<UserEntity> = emptyList(),
    val pendingUsers: List<UserEntity> = emptyList(),
    val pendingUsersCount: Int = 0,
    val showAdminPanel: Boolean = false,
    val selectedPeriod: TimePeriodFilter = TimePeriodFilter.THIS_MONTH,
    val customDateRange: Pair<Long, Long> = Pair(System.currentTimeMillis() - 30L * 86400000L, System.currentTimeMillis()),
    val expenses: List<ExpenseEntity> = emptyList(),
    val filteredExpenses: List<ExpenseEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val withdrawals: List<WithdrawalEntity> = emptyList(),
    val filteredWithdrawals: List<WithdrawalEntity> = emptyList(),
    val debts: List<DebtEntity> = emptyList(),
    val filteredDebts: List<DebtEntity> = emptyList(),
    val summary: DashboardSummary = DashboardSummary(),
    val searchQuery: String = "",
    val selectedExpenseCategory: String = "All",
    val selectedDebtFilter: String = "All", // "All", "Receivable", "Payable", "Settled", "Overdue"
    val currencySymbol: String = "PKR"
)

private data class LedgerDataSources(
    val expenses: List<ExpenseEntity>,
    val budgets: List<BudgetEntity>,
    val withdrawals: List<WithdrawalEntity>,
    val debts: List<DebtEntity>
)

private data class UserSources(
    val allUsers: List<UserEntity>,
    val pendingUsers: List<UserEntity>,
    val pendingUsersCount: Int
)

private data class FilterParams(
    val period: TimePeriodFilter,
    val customRange: Pair<Long, Long>,
    val query: String,
    val expCat: String,
    val debtFilter: String,
    val currency: String
)

private data class AuthState(
    val currentUser: UserEntity?,
    val isLoggedIn: Boolean,
    val isAuthLoading: Boolean,
    val authError: String?,
    val authSuccessMessage: String?
)

class BizLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BizLedgerRepository =
        BizLedgerRepository(AppDatabase.getDatabase(application))

    private val prefs = application.getSharedPreferences("bizledger_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _isAuthLoading = MutableStateFlow(false)
    private val _authError = MutableStateFlow<String?>(null)
    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    private val _showAdminPanel = MutableStateFlow(false)

    private val _selectedPeriod = MutableStateFlow(TimePeriodFilter.THIS_MONTH)
    private val _customDateRange = MutableStateFlow(Pair(System.currentTimeMillis() - 30L * 86400000L, System.currentTimeMillis()))
    private val _searchQuery = MutableStateFlow("")
    private val _selectedExpenseCategory = MutableStateFlow("All")
    private val _selectedDebtFilter = MutableStateFlow("All")
    private val _currencySymbol = MutableStateFlow("PKR")

    private val authStateFlow = combine(
        _currentUser,
        _isLoggedIn,
        _isAuthLoading,
        _authError,
        _authSuccessMessage
    ) { current, loggedIn, loading, err, success ->
        AuthState(current, loggedIn, loading, err, success)
    }

    private val ledgerDataFlow = combine(
        repository.allExpenses,
        repository.allBudgets,
        repository.allWithdrawals,
        repository.allDebts
    ) { expenses, budgets, withdrawals, debts ->
        LedgerDataSources(expenses, budgets, withdrawals, debts)
    }

    private val userSourcesFlow = combine(
        repository.allUsers,
        repository.pendingUsers,
        repository.pendingUsersCount
    ) { allUsers, pendingUsers, pendingCount ->
        UserSources(allUsers, pendingUsers, pendingCount)
    }

    private val filterParamsFlow = combine(
        combine(_selectedPeriod, _customDateRange) { period, customRange -> Pair(period, customRange) },
        combine(_searchQuery, _selectedExpenseCategory) { query, expCat -> Pair(query, expCat) },
        combine(_selectedDebtFilter, _currencySymbol) { debtFilter, currency -> Pair(debtFilter, currency) }
    ) { (period, customRange), (query, expCat), (debtFilter, currency) ->
        FilterParams(period, customRange, query, expCat, debtFilter, currency)
    }

    val uiState: StateFlow<BizLedgerUiState> = combine(
        ledgerDataFlow,
        userSourcesFlow,
        filterParamsFlow,
        authStateFlow,
        _showAdminPanel
    ) { ledgerData, userData, filter, auth, showAdmin ->
        val (startTime, endTime) = if (filter.period == TimePeriodFilter.CUSTOM) {
            filter.customRange
        } else {
            getTimeRangeForPeriod(filter.period)
        }

        // Filter expenses by period, search & category
        val inPeriodExpenses = ledgerData.expenses.filter { it.dateMillis in startTime..endTime }
        val filteredExpenses = inPeriodExpenses.filter { expense ->
            val matchesQuery = filter.query.isBlank() ||
                    expense.title.contains(filter.query, ignoreCase = true) ||
                    expense.category.contains(filter.query, ignoreCase = true) ||
                    expense.notes.contains(filter.query, ignoreCase = true) ||
                    expense.receiptTag.contains(filter.query, ignoreCase = true)
            val matchesCat = filter.expCat == "All" || expense.category.equals(filter.expCat, ignoreCase = true)
            matchesQuery && matchesCat
        }

        // Filter withdrawals by period & query
        val inPeriodWithdrawals = ledgerData.withdrawals.filter { it.dateMillis in startTime..endTime }
        val filteredWithdrawals = inPeriodWithdrawals.filter { withdrawal ->
            filter.query.isBlank() ||
                    withdrawal.ownerName.contains(filter.query, ignoreCase = true) ||
                    withdrawal.reason.contains(filter.query, ignoreCase = true)
        }

        // Filter debts
        val filteredDebts = ledgerData.debts.filter { debt ->
            val matchesQuery = filter.query.isBlank() ||
                    debt.partyName.contains(filter.query, ignoreCase = true) ||
                    debt.notes.contains(filter.query, ignoreCase = true) ||
                    debt.contactInfo.contains(filter.query, ignoreCase = true)

            val matchesFilter = when (filter.debtFilter) {
                "Receivable" -> debt.type == DebtType.RECEIVABLE && !debt.isSettled
                "Payable" -> debt.type == DebtType.PAYABLE && !debt.isSettled
                "Settled" -> debt.isSettled
                "Overdue" -> debt.isOverdue
                else -> true
            }
            matchesQuery && matchesFilter
        }

        // Compute Dashboard Summary
        val totalExp = inPeriodExpenses.sumOf { it.amount }
        val totalWith = inPeriodWithdrawals.sumOf { it.amount }

        val totalReceivable = ledgerData.debts.filter { it.type == DebtType.RECEIVABLE && !it.isSettled }
            .sumOf { it.remainingAmount }
        val totalPayable = ledgerData.debts.filter { it.type == DebtType.PAYABLE && !it.isSettled }
            .sumOf { it.remainingAmount }

        val pendingDebts = ledgerData.debts.count { !it.isSettled }
        val overdueDebts = ledgerData.debts.count { it.isOverdue }

        val overallBudget = ledgerData.budgets.find { it.category == "All Expenses" || it.category == "Total Monthly" }?.monthlyLimit
            ?: ledgerData.budgets.sumOf { it.monthlyLimit }

        // Category breakdown calculation
        val categoryMap = inPeriodExpenses.groupBy { it.category }
        val categoryBreakdown = categoryMap.map { (cat, list) ->
            val spent = list.sumOf { it.amount }
            val pct = if (totalExp > 0) (spent / totalExp).toFloat() else 0f
            val catBudget = ledgerData.budgets.find { it.category.equals(cat, ignoreCase = true) }?.monthlyLimit ?: 0.0
            CategoryExpenseStat(
                category = cat,
                totalSpent = spent,
                percentage = pct,
                budget = catBudget,
                count = list.size
            )
        }.sortedByDescending { it.totalSpent }

        val summary = DashboardSummary(
            totalExpenses = totalExp,
            totalWithdrawals = totalWith,
            totalReceivables = totalReceivable,
            totalPayables = totalPayable,
            totalOverallBudget = overallBudget,
            totalSpentInBudget = totalExp,
            netCashOutflow = totalExp + totalWith,
            pendingDebtsCount = pendingDebts,
            overdueDebtsCount = overdueDebts,
            categoryBreakdown = categoryBreakdown
        )

        BizLedgerUiState(
            currentUser = auth.currentUser,
            isLoggedIn = auth.isLoggedIn,
            isAuthLoading = auth.isAuthLoading,
            authError = auth.authError,
            authSuccessMessage = auth.authSuccessMessage,
            allUsers = userData.allUsers,
            pendingUsers = userData.pendingUsers,
            pendingUsersCount = userData.pendingUsersCount,
            showAdminPanel = showAdmin,
            selectedPeriod = filter.period,
            customDateRange = filter.customRange,
            expenses = ledgerData.expenses,
            filteredExpenses = filteredExpenses,
            budgets = ledgerData.budgets,
            withdrawals = ledgerData.withdrawals,
            filteredWithdrawals = filteredWithdrawals,
            debts = ledgerData.debts,
            filteredDebts = filteredDebts,
            summary = summary,
            searchQuery = filter.query,
            selectedExpenseCategory = filter.expCat,
            selectedDebtFilter = filter.debtFilter,
            currencySymbol = filter.currency
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BizLedgerUiState()
    )

    init {
        viewModelScope.launch {
            // Ensure default root administrator exists (admin / admin123)
            repository.ensureDefaultAdminExists()

            // Restore saved session if user was previously approved and logged in
            val savedUserId = prefs.getLong("saved_user_id", -1L)
            if (savedUserId != -1L) {
                val user = repository.getUserById(savedUserId)
                if (user != null && user.status == UserStatus.APPROVED) {
                    _currentUser.value = user
                    _isLoggedIn.value = true
                } else {
                    prefs.edit().remove("saved_user_id").apply()
                }
            }
        }
    }

    // ==========================================
    // Authentication & User Management Actions
    // ==========================================

    fun login(usernameOrEmail: String, password: String) {
        val trimmedIdent = usernameOrEmail.trim()
        val trimmedPass = password.trim()

        if (trimmedIdent.isBlank() || trimmedPass.isBlank()) {
            _authError.value = "Please enter both username and password."
            return
        }

        _isAuthLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null

        viewModelScope.launch {
            try {
                val user = repository.getUserByUsername(trimmedIdent)
                    ?: repository.getUserByEmail(trimmedIdent)

                if (user == null) {
                    _authError.value = "Account not found. Please check your username or register."
                    _isAuthLoading.value = false
                    return@launch
                }

                if (user.password != trimmedPass) {
                    _authError.value = "Incorrect password. Please try again."
                    _isAuthLoading.value = false
                    return@launch
                }

                when (user.status) {
                    UserStatus.PENDING -> {
                        _authError.value = "Your registration is pending Admin approval. An administrator must approve your account before you can log in."
                        _isAuthLoading.value = false
                    }
                    UserStatus.REJECTED -> {
                        _authError.value = "Your registration request has been rejected by the administrator. Please contact system support."
                        _isAuthLoading.value = false
                    }
                    UserStatus.APPROVED -> {
                        _currentUser.value = user
                        _isLoggedIn.value = true
                        _isAuthLoading.value = false
                        _authError.value = null
                        prefs.edit().putLong("saved_user_id", user.id).apply()
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Login error: ${e.localizedMessage ?: "Unknown error"}"
                _isAuthLoading.value = false
            }
        }
    }

    fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole = UserRole.USER,
        notes: String = ""
    ) {
        val trimmedName = fullName.trim()
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedName.isBlank() || trimmedUsername.isBlank() || trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            _authError.value = "All fields are required for registration."
            return
        }

        if (trimmedPass.length < 4) {
            _authError.value = "Password must be at least 4 characters."
            return
        }

        if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
            _authError.value = "Please enter a valid email address."
            return
        }

        _isAuthLoading.value = true
        _authError.value = null
        _authSuccessMessage.value = null

        viewModelScope.launch {
            try {
                val newUser = UserEntity(
                    username = trimmedUsername,
                    fullName = trimmedName,
                    email = trimmedEmail,
                    password = trimmedPass,
                    role = role,
                    status = UserStatus.PENDING, // Strictly requires admin approval
                    notes = notes
                )

                val result = repository.registerUser(newUser)
                _isAuthLoading.value = false

                if (result.isSuccess) {
                    _authSuccessMessage.value = "Registration submitted successfully! Your account is currently PENDING admin approval. An administrator must approve your account before you can log in."
                } else {
                    _authError.value = result.exceptionOrNull()?.message ?: "Failed to register account."
                }
            } catch (e: Exception) {
                _authError.value = "Registration error: ${e.localizedMessage}"
                _isAuthLoading.value = false
            }
        }
    }

    fun adminCreateUser(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: UserRole,
        preApproved: Boolean,
        notes: String = ""
    ) {
        val trimmedName = fullName.trim()
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedName.isBlank() || trimmedUsername.isBlank() || trimmedEmail.isBlank() || trimmedPass.isBlank()) {
            return
        }

        viewModelScope.launch {
            val adminName = _currentUser.value?.fullName ?: "Admin"
            val newUser = UserEntity(
                username = trimmedUsername,
                fullName = trimmedName,
                email = trimmedEmail,
                password = trimmedPass,
                role = role,
                status = if (preApproved) UserStatus.APPROVED else UserStatus.PENDING,
                approvedAtMillis = if (preApproved) System.currentTimeMillis() else null,
                approvedBy = if (preApproved) adminName else null,
                notes = notes
            )
            repository.registerUser(newUser)
        }
    }

    fun approveUser(userId: Long) = viewModelScope.launch {
        val adminName = _currentUser.value?.username ?: "Admin"
        repository.approveUser(userId, adminName)
    }

    fun rejectUser(userId: Long) = viewModelScope.launch {
        val adminName = _currentUser.value?.username ?: "Admin"
        repository.rejectUser(userId, adminName)
    }

    fun updateUserRole(userId: Long, newRole: UserRole) = viewModelScope.launch {
        repository.updateUserRole(userId, newRole)
    }

    fun deleteUser(user: UserEntity) = viewModelScope.launch {
        repository.deleteUser(user)
    }

    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _showAdminPanel.value = false
        _authError.value = null
        _authSuccessMessage.value = null
        prefs.edit().remove("saved_user_id").apply()
    }

    fun toggleAdminPanel(show: Boolean) {
        _showAdminPanel.value = show
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authSuccessMessage.value = null
    }

    // ==========================================
    // Filter & Business Ledger Operations
    // ==========================================

    fun setTimePeriod(period: TimePeriodFilter) {
        _selectedPeriod.value = period
    }

    fun setCustomDateRange(startMillis: Long, endMillis: Long) {
        _customDateRange.value = Pair(startMillis, endMillis)
        _selectedPeriod.value = TimePeriodFilter.CUSTOM
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setExpenseCategoryFilter(category: String) {
        _selectedExpenseCategory.value = category
    }

    fun setDebtFilter(filter: String) {
        _selectedDebtFilter.value = filter
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    // Expense operations
    fun addExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    // Budget operations
    fun addBudget(budget: BudgetEntity) = viewModelScope.launch {
        repository.insertBudget(budget)
    }

    fun updateBudget(budget: BudgetEntity) = viewModelScope.launch {
        repository.updateBudget(budget)
    }

    fun deleteBudget(budget: BudgetEntity) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    // Withdrawal operations
    fun addWithdrawal(withdrawal: WithdrawalEntity) = viewModelScope.launch {
        repository.insertWithdrawal(withdrawal)
    }

    fun updateWithdrawal(withdrawal: WithdrawalEntity) = viewModelScope.launch {
        repository.updateWithdrawal(withdrawal)
    }

    fun deleteWithdrawal(withdrawal: WithdrawalEntity) = viewModelScope.launch {
        repository.deleteWithdrawal(withdrawal)
    }

    // Debt operations
    fun addDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.insertDebt(debt)
    }

    fun updateDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.updateDebt(debt)
    }

    fun deleteDebt(debt: DebtEntity) = viewModelScope.launch {
        repository.deleteDebt(debt)
    }

    fun recordDebtPayment(debtId: Long, amount: Double, notes: String) = viewModelScope.launch {
        repository.recordDebtPayment(debtId, amount, notes)
    }

    fun getPaymentsForDebt(debtId: Long): StateFlow<List<DebtPaymentEntity>> {
        return repository.getPaymentsForDebt(debtId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Data Backup & Restore Operations (Admin Device Storage)
    suspend fun createDeviceBackup(context: Context): com.example.util.BackupFileInfo {
        val currentState = uiState.value
        return com.example.util.DataBackupManager.createBackup(
            context = context,
            expenses = currentState.expenses,
            withdrawals = currentState.withdrawals,
            debts = currentState.debts,
            budgets = currentState.budgets,
            users = currentState.allUsers
        )
    }

    suspend fun restoreDeviceBackup(
        file: java.io.File,
        clearExistingFirst: Boolean
    ): com.example.util.BackupRestoreResult {
        return com.example.util.DataBackupManager.restoreBackup(
            jsonFile = file,
            repository = repository,
            clearExistingFirst = clearExistingFirst
        )
    }

    fun getDeviceBackups(context: Context): List<com.example.util.BackupFileInfo> {
        return com.example.util.DataBackupManager.listBackups(context)
    }

    fun deleteDeviceBackup(context: Context, backup: com.example.util.BackupFileInfo): Boolean {
        return com.example.util.DataBackupManager.deleteBackup(context, backup)
    }

    companion object {
        fun formatCurrency(amount: Double, symbol: String = "PKR"): String {
            val formatter = NumberFormat.getNumberInstance(Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            return "$symbol ${formatter.format(amount)}"
        }

        fun formatDate(millis: Long, pattern: String = "MMM dd, yyyy"): String {
            return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
        }

        fun getTimeRangeForPeriod(period: TimePeriodFilter): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            return when (period) {
                TimePeriodFilter.THIS_WEEK -> {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_WEEK, 7)
                    val end = cal.timeInMillis - 1
                    Pair(start, end)
                }
                TimePeriodFilter.THIS_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 1)
                    val end = cal.timeInMillis - 1
                    Pair(start, end)
                }
                TimePeriodFilter.LAST_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val end = cal.timeInMillis - 1
                    cal.add(Calendar.MONTH, -1)
                    val start = cal.timeInMillis
                    Pair(start, end)
                }
                TimePeriodFilter.THIS_QUARTER -> {
                    val currentMonth = cal.get(Calendar.MONTH)
                    val quarterStartMonth = (currentMonth / 3) * 3
                    cal.set(Calendar.MONTH, quarterStartMonth)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.MONTH, 3)
                    val end = cal.timeInMillis - 1
                    Pair(start, end)
                }
                TimePeriodFilter.THIS_YEAR -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.YEAR, 1)
                    val end = cal.timeInMillis - 1
                    Pair(start, end)
                }
                TimePeriodFilter.LAST_YEAR -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val end = cal.timeInMillis - 1
                    cal.add(Calendar.YEAR, -1)
                    val start = cal.timeInMillis
                    Pair(start, end)
                }
                TimePeriodFilter.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
                TimePeriodFilter.CUSTOM -> Pair(System.currentTimeMillis() - 30L * 86400000L, System.currentTimeMillis())
            }
        }
    }
}
