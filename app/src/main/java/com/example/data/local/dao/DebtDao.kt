package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, dueDateMillis ASC, issueDateMillis DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY dateMillis DESC")
    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: DebtPaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: DebtPaymentEntity)

    @Transaction
    suspend fun recordDebtPayment(debtId: Long, amount: Double, notes: String): Boolean {
        val debt = getDebtById(debtId) ?: return false
        val newPaid = debt.paidAmount + amount
        val isSettled = newPaid >= (debt.totalAmount - 0.001)
        updateDebt(debt.copy(paidAmount = newPaid, isSettled = isSettled))
        insertPayment(DebtPaymentEntity(debtId = debtId, amount = amount, notes = notes))
        return true
    }
}
