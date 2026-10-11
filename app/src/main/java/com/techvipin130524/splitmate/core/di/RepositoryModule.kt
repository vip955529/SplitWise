package com.techvipin130524.splitmate.core.di

import com.techvipin130524.splitmate.data.repository.ActivityLogRepositoryImpl
import com.techvipin130524.splitmate.data.repository.ExpenseCommentRepositoryImpl
import com.techvipin130524.splitmate.data.repository.ExpenseRepositoryImpl
import com.techvipin130524.splitmate.data.repository.FriendRepositoryImpl
import com.techvipin130524.splitmate.data.repository.GroupRepositoryImpl
import com.techvipin130524.splitmate.data.repository.SettlementRepositoryImpl
import com.techvipin130524.splitmate.data.repository.UserRepositoryImpl
import com.techvipin130524.splitmate.domain.repository.ActivityLogRepository
import com.techvipin130524.splitmate.domain.repository.ExpenseCommentRepository
import com.techvipin130524.splitmate.domain.repository.ExpenseRepository
import com.techvipin130524.splitmate.domain.repository.FriendRepository
import com.techvipin130524.splitmate.domain.repository.GroupRepository
import com.techvipin130524.splitmate.domain.repository.SettlementRepository
import com.techvipin130524.splitmate.domain.repository.UserRepository
import com.techvipin130524.splitmate.feature.auth.data.FirebaseAuthRepository
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl,
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindFriendRepository(
        friendRepositoryImpl: FriendRepositoryImpl,
    ): FriendRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl,
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl,
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindSettlementRepository(
        settlementRepositoryImpl: SettlementRepositoryImpl,
    ): SettlementRepository

    @Binds
    @Singleton
    abstract fun bindActivityLogRepository(
        activityLogRepositoryImpl: ActivityLogRepositoryImpl,
    ): ActivityLogRepository

    @Binds
    @Singleton
    abstract fun bindExpenseCommentRepository(
        expenseCommentRepositoryImpl: ExpenseCommentRepositoryImpl,
    ): ExpenseCommentRepository
}
