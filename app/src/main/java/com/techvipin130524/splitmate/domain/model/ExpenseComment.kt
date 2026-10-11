package com.techvipin130524.splitmate.domain.model

data class ExpenseComment(
    val id: String = "",
    val expenseId: String = "",
    val userId: String = "",
    val userName: String = "",
    val commentText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
