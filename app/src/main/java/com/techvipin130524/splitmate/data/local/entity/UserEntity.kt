package com.techvipin130524.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val profileImageUrl: String?,
    val monthlyBudgetPaise: Long,
    val createdAt: Long,
    val updatedAt: Long
)
