package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DebtType {
    PAYABLE,    // Business owes to someone (supplier, lender, etc.)
    RECEIVABLE  // Someone owes to the business (customer, client, partner)
}

@Entity(
    tableName = "debts",
    indices = [
        Index("type"),
        Index("isSettled"),
        Index("dueDateMillis"),
        Index("partyName")
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partyName: String,
    val type: DebtType,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDateMillis: Long? = null,
    val issueDateMillis: Long = System.currentTimeMillis(),
    val contactInfo: String = "",
    val notes: String = "",
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)

    val isOverdue: Boolean
        get() = !isSettled && dueDateMillis != null && dueDateMillis < System.currentTimeMillis()
}
