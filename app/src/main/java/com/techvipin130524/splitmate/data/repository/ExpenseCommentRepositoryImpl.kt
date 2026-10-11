package com.techvipin130524.splitmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.techvipin130524.splitmate.data.local.dao.ExpenseCommentDao
import com.techvipin130524.splitmate.data.mapper.toDomain
import com.techvipin130524.splitmate.data.mapper.toEntity
import com.techvipin130524.splitmate.domain.model.ExpenseComment
import com.techvipin130524.splitmate.domain.repository.ExpenseCommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class ExpenseCommentRepositoryImpl @Inject constructor(
    private val expenseCommentDao: ExpenseCommentDao,
    private val firestore: FirebaseFirestore
) : ExpenseCommentRepository {

    override fun observeCommentsForExpense(expenseId: String): Flow<List<ExpenseComment>> {
        return expenseCommentDao.observeCommentsForExpense(expenseId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addComment(comment: ExpenseComment): Result<Unit> {
        return try {
            val id = if (comment.id.isEmpty()) UUID.randomUUID().toString() else comment.id
            val finalComment = comment.copy(id = id)

            expenseCommentDao.insertComment(finalComment.toEntity())
            firestore.collection("expense_comments").document(id).set(finalComment).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
