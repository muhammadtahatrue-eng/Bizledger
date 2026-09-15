package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.entity.UserStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY createdAtMillis DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE status = :status ORDER BY createdAtMillis DESC")
    fun getUsersByStatus(status: UserStatus): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE status = 'PENDING'")
    fun getPendingUsersCount(): Flow<Int>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getTotalUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("UPDATE users SET status = :status, approvedAtMillis = :approvedAt, approvedBy = :approvedBy WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: UserStatus, approvedAt: Long?, approvedBy: String?)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: Long, role: UserRole)
}
