package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [
        Index("category")
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // e.g. "Total Monthly" or "Inventory", "Rent", etc.
    val monthlyLimit: Double,
    val monthYear: String = "ALL_MONTHS", // e.g. "2026-08" or "ALL_MONTHS"
    val notes: String = ""
)
