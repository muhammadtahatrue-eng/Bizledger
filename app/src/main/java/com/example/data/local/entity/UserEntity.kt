package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    USER
}

enum class UserStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index("status"),
        Index("email")
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.PENDING,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val approvedAtMillis: Long? = null,
    val approvedBy: String? = null,
    val notes: String = ""
)
