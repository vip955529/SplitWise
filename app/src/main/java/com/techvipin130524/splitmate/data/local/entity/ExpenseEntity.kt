package com.techvipin130524.splitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val description: String,
    val totalAmount: Long,
    val currency: String,
    val category: String,
    val recurrence: String,
    val note: String,
    val receiptImageUrl: String?,
    val createdBy: String,
    val createdByName: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
)

@Entity(tableName = "expense_participants")
data class ExpenseParticipantEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val userName: String,
    val splitType: String,
    val amount: Long,
    val percentage: Double,
    val shares: Int
)

@Entity(tableName = "expense_payers")
data class ExpensePayerEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val userName: String,
    val amount: Long
)
