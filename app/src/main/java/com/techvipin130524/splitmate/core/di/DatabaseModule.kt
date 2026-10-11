package com.techvipin130524.splitmate.core.di

import android.content.Context
import androidx.room.Room
import com.techvipin130524.splitmate.data.local.SplitMateDatabase
import com.techvipin130524.splitmate.data.local.dao.ActivityLogDao
import com.techvipin130524.splitmate.data.local.dao.ExpenseCommentDao
import com.techvipin130524.splitmate.data.local.dao.ExpenseDao
import com.techvipin130524.splitmate.data.local.dao.FriendDao
import com.techvipin130524.splitmate.data.local.dao.GroupDao
import com.techvipin130524.splitmate.data.local.dao.SettlementDao
import com.techvipin130524.splitmate.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SplitMateDatabase {
        return Room.databaseBuilder(
            context,
            SplitMateDatabase::class.java,
            "splitmate.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: SplitMateDatabase): UserDao = db.userDao()

    @Provides
    fun provideFriendDao(db: SplitMateDatabase): FriendDao = db.friendDao()

    @Provides
    fun provideGroupDao(db: SplitMateDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideExpenseDao(db: SplitMateDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideSettlementDao(db: SplitMateDatabase): SettlementDao = db.settlementDao()

    @Provides
    fun provideActivityLogDao(db: SplitMateDatabase): ActivityLogDao = db.activityLogDao()

    @Provides
    fun provideExpenseCommentDao(db: SplitMateDatabase): ExpenseCommentDao = db.expenseCommentDao()
}
