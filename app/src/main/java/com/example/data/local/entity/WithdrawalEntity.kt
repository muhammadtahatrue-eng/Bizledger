package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "withdrawals",
    indices = [
        Index("dateMillis"),
        Index("ownerName")
    ]
)
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerName: String,
    val amount: Double,
    val dateMillis: Long,
    val paymentMethod: String,
    val reason: String = ""
)
