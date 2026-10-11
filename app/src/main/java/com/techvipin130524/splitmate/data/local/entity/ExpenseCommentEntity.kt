package com.techvipin130524.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_comments")
data class ExpenseCommentEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val userName: String,
    val commentText: String,
    val timestamp: Long
)
