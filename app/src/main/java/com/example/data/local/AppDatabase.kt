package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.DebtDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WithdrawalDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WithdrawalEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class,
        WithdrawalEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun debtDao(): DebtDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizledger_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
