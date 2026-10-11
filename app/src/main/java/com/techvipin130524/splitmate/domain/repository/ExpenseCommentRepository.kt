package com.techvipin130524.splitmate.domain.repository

import com.techvipin130524.splitmate.domain.model.ExpenseComment
import kotlinx.coroutines.flow.Flow

interface ExpenseCommentRepository {
    fun observeCommentsForExpense(expenseId: String): Flow<List<ExpenseComment>>
    suspend fun addComment(comment: ExpenseComment): Result<Unit>
}
