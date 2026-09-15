package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WithdrawalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals ORDER BY dateMillis DESC")
    fun getAllWithdrawals(): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals WHERE dateMillis BETWEEN :startTime AND :endTime ORDER BY dateMillis DESC")
    fun getWithdrawalsInRange(startTime: Long, endTime: Long): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Update
    suspend fun updateWithdrawal(withdrawal: WithdrawalEntity)

    @Delete
    suspend fun deleteWithdrawal(withdrawal: WithdrawalEntity)

    @Query("DELETE FROM withdrawals WHERE id = :id")
    suspend fun deleteWithdrawalById(id: Long)

    @Query("SELECT SUM(amount) FROM withdrawals")
    fun getTotalWithdrawals(): Flow<Double?>
}
