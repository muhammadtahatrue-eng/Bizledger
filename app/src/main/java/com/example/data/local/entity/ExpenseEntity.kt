package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("dateMillis"),
        Index("category")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val dateMillis: Long,
    val paymentMethod: String,
    val notes: String = "",
    val receiptTag: String = ""
)
