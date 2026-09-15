package com.example.data.repository

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
import kotlinx.coroutines.flow.Flow

class BizLedgerRepository(private val database: AppDatabase) {

    private val expenseDao = database.expenseDao()
    private val budgetDao = database.budgetDao()
    private val withdrawalDao = database.withdrawalDao()
    private val debtDao = database.debtDao()
    private val userDao = database.userDao()

    // Users & Authentication
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val pendingUsers: Flow<List<UserEntity>> = userDao.getUsersByStatus(UserStatus.PENDING)
    val pendingUsersCount: Flow<Int> = userDao.getPendingUsersCount()

    suspend fun getUserByUsername(username: String): UserEntity? =
        userDao.getUserByUsername(username.trim())

    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getUserByEmail(email.trim())

    suspend fun getUserById(id: Long): UserEntity? =
        userDao.getUserById(id)

    suspend fun registerUser(user: UserEntity): Result<Long> {
        return try {
            val existingUser = userDao.getUserByUsername(user.username.trim())
            if (existingUser != null) {
                return Result.failure(Exception("Username '${user.username}' is already taken."))
            }
            val existingEmail = userDao.getUserByEmail(user.email.trim())
            if (existingEmail != null) {
                return Result.failure(Exception("Email '${user.email}' is already registered."))
            }
            val id = userDao.insertUser(user)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: UserEntity) =
        userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) =
        userDao.deleteUser(user)

    suspend fun approveUser(userId: Long, adminName: String) =
        userDao.updateUserStatus(userId, UserStatus.APPROVED, System.currentTimeMillis(), adminName)

    suspend fun rejectUser(userId: Long, adminName: String) =
        userDao.updateUserStatus(userId, UserStatus.REJECTED, System.currentTimeMillis(), adminName)

    suspend fun updateUserRole(userId: Long, role: UserRole) =
        userDao.updateUserRole(userId, role)

    suspend fun ensureDefaultAdminExists() {
        val adminCount = userDao.getAdminCount()
        if (adminCount == 0) {
            val defaultAdmin = UserEntity(
                username = "admin",
                fullName = "Master Administrator",
                email = "admin@bizledger.local",
                password = "admin123",
                role = UserRole.ADMIN,
                status = UserStatus.APPROVED,
                createdAtMillis = System.currentTimeMillis(),
                approvedAtMillis = System.currentTimeMillis(),
                approvedBy = "System",
                notes = "Pre-configured system root administrator"
            )
            userDao.insertUser(defaultAdmin)
        }
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenses()

    fun getExpensesInRange(start: Long, end: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesInRange(start, end)

    suspend fun insertExpense(expense: ExpenseEntity): Long =
        expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: ExpenseEntity) =
        expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) =
        expenseDao.deleteExpenseById(id)

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun insertBudget(budget: BudgetEntity): Long =
        budgetDao.insertBudget(budget)

    suspend fun updateBudget(budget: BudgetEntity) =
        budgetDao.updateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        budgetDao.deleteBudget(budget)

    suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteBudgetById(id)

    // Withdrawals
    val allWithdrawals: Flow<List<WithdrawalEntity>> = withdrawalDao.getAllWithdrawals()
    val totalWithdrawals: Flow<Double?> = withdrawalDao.getTotalWithdrawals()

    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long =
        withdrawalDao.insertWithdrawal(withdrawal)

    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity) =
        withdrawalDao.updateWithdrawal(withdrawal)

    suspend fun deleteWithdrawal(withdrawal: WithdrawalEntity) =
        withdrawalDao.deleteWithdrawal(withdrawal)

    suspend fun deleteWithdrawalById(id: Long) =
        withdrawalDao.deleteWithdrawalById(id)

    // Debts
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity): Long =
        debtDao.insertDebt(debt)

    suspend fun updateDebt(debt: DebtEntity) =
        debtDao.updateDebt(debt)

    suspend fun deleteDebt(debt: DebtEntity) =
        debtDao.deleteDebt(debt)

    suspend fun deleteDebtById(id: Long) =
        debtDao.deleteDebtById(id)

    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>> =
        debtDao.getPaymentsForDebt(debtId)

    suspend fun recordDebtPayment(debtId: Long, amount: Double, notes: String): Boolean =
        debtDao.recordDebtPayment(debtId, amount, notes)

    suspend fun clearAllLedgerData() {
        database.runInTransaction {
            // Clears ledger tables while preserving user accounts during backup restoration
            database.openHelper.writableDatabase.execSQL("DELETE FROM expenses")
            database.openHelper.writableDatabase.execSQL("DELETE FROM withdrawals")
            database.openHelper.writableDatabase.execSQL("DELETE FROM debt_payments")
            database.openHelper.writableDatabase.execSQL("DELETE FROM debts")
            database.openHelper.writableDatabase.execSQL("DELETE FROM budgets")
        }
    }
}
