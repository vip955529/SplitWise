package com.techvipin130524.splitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techvipin130524.splitmate.data.local.entity.ExpenseCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCommentDao {

    @Query("SELECT * FROM expense_comments WHERE expenseId = :expenseId ORDER BY timestamp ASC")
    fun observeCommentsForExpense(expenseId: String): Flow<List<ExpenseCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ExpenseCommentEntity)
}
