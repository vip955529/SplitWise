package com.techvipin130524.splitmate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.techvipin130524.splitmate.data.local.dao.ActivityLogDao
import com.techvipin130524.splitmate.data.local.dao.ExpenseDao
import com.techvipin130524.splitmate.data.local.dao.FriendDao
import com.techvipin130524.splitmate.data.local.dao.GroupDao
import com.techvipin130524.splitmate.data.local.dao.SettlementDao
import com.techvipin130524.splitmate.data.local.dao.UserDao
import com.techvipin130524.splitmate.data.local.entity.ActivityLogEntity
import com.techvipin130524.splitmate.data.local.entity.ExpenseEntity
import com.techvipin130524.splitmate.data.local.entity.ExpenseParticipantEntity
import com.techvipin130524.splitmate.data.local.entity.ExpensePayerEntity
import com.techvipin130524.splitmate.data.local.entity.FriendEntity
import com.techvipin130524.splitmate.data.local.entity.GroupEntity
import com.techvipin130524.splitmate.data.local.entity.GroupMemberEntity
import com.techvipin130524.splitmate.data.local.entity.SettlementEntity
import com.techvipin130524.splitmate.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        FriendEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpenseParticipantEntity::class,
        ExpensePayerEntity::class,
        SettlementEntity::class,
        ActivityLogEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class SplitMateDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settlementDao(): SettlementDao
    abstract fun activityLogDao(): ActivityLogDao
}
