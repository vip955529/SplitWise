package com.techvipin130524.splitmate.data.mapper

import com.techvipin130524.splitmate.data.local.entity.ActivityLogEntity
import com.techvipin130524.splitmate.data.local.entity.ExpenseEntity
import com.techvipin130524.splitmate.data.local.entity.ExpenseParticipantEntity
import com.techvipin130524.splitmate.data.local.entity.ExpensePayerEntity
import com.techvipin130524.splitmate.data.local.entity.FriendEntity
import com.techvipin130524.splitmate.data.local.entity.GroupEntity
import com.techvipin130524.splitmate.data.local.entity.GroupMemberEntity
import com.techvipin130524.splitmate.data.local.entity.SettlementEntity
import com.techvipin130524.splitmate.data.local.entity.UserEntity
import com.techvipin130524.splitmate.domain.model.ActivityLog
import com.techvipin130524.splitmate.domain.model.ActivityType
import com.techvipin130524.splitmate.domain.model.Expense
import com.techvipin130524.splitmate.domain.model.ExpenseCategory
import com.techvipin130524.splitmate.domain.model.ExpenseParticipant
import com.techvipin130524.splitmate.domain.model.ExpensePayer
import com.techvipin130524.splitmate.domain.model.Friend
import com.techvipin130524.splitmate.domain.model.FriendStatus
import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.model.GroupMember
import com.techvipin130524.splitmate.domain.model.Settlement
import com.techvipin130524.splitmate.domain.model.SplitType
import com.techvipin130524.splitmate.domain.model.User

import com.techvipin130524.splitmate.domain.model.RecurrenceFrequency

// User Mappers
fun User.toEntity() = UserEntity(id, name, email, profileImageUrl, createdAt, updatedAt)
fun UserEntity.toDomain() = User(id, name, email, profileImageUrl, createdAt, updatedAt)

// Friend Mappers
fun Friend.toEntity() = FriendEntity(id, userId, friendUserId, friendName, friendEmail, friendProfileImageUrl, status.name, createdAt)
fun FriendEntity.toDomain() = Friend(id, userId, friendUserId, friendName, friendEmail, friendProfileImageUrl, try { FriendStatus.valueOf(status) } catch (e: Exception) { FriendStatus.ACCEPTED }, createdAt)

// Group Mappers
fun Group.toEntity() = GroupEntity(id, name, description, imageUrl, inviteCode, createdBy, createdAt, updatedAt)
fun GroupEntity.toDomain(members: List<GroupMember> = emptyList()) = Group(id, name, description, imageUrl, inviteCode, createdBy, createdAt, updatedAt, members)

fun GroupMember.toEntity(groupId: String) = GroupMemberEntity(groupId, userId, userName, userEmail, role, joinedAt)
fun GroupMemberEntity.toDomain() = GroupMember(groupId, userId, userName, userEmail, role, joinedAt)

// Expense Mappers
fun Expense.toEntity() = ExpenseEntity(id, groupId, description, totalAmount, currency, category.name, recurrence.name, note, receiptImageUrl, createdBy, createdByName, createdAt, updatedAt, isDeleted)
fun ExpenseEntity.toDomain(participants: List<ExpenseParticipant> = emptyList(), payers: List<ExpensePayer> = emptyList()) = Expense(id, groupId, description, totalAmount, currency, try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.OTHER }, try { RecurrenceFrequency.valueOf(recurrence) } catch (e: Exception) { RecurrenceFrequency.NONE }, note, receiptImageUrl, createdBy, createdByName, createdAt, updatedAt, isDeleted, participants, payers)

fun ExpenseParticipant.toEntity(expenseId: String) = ExpenseParticipantEntity(if (id.isEmpty()) "${expenseId}_$userId" else id, expenseId, userId, userName, splitType.name, amount, percentage, shares)
fun ExpenseParticipantEntity.toDomain() = ExpenseParticipant(id, expenseId, userId, userName, try { SplitType.valueOf(splitType) } catch (e: Exception) { SplitType.EQUAL }, amount, percentage, shares)

fun ExpensePayer.toEntity(expenseId: String) = ExpensePayerEntity(if (id.isEmpty()) "${expenseId}_$userId" else id, expenseId, userId, userName, amount)
fun ExpensePayerEntity.toDomain() = ExpensePayer(id, expenseId, userId, userName, amount)

// Settlement Mappers
fun Settlement.toEntity() = SettlementEntity(id, groupId, fromUserId, fromUserName, toUserId, toUserName, amount, currency, note, createdAt)
fun SettlementEntity.toDomain() = Settlement(id, groupId, fromUserId, fromUserName, toUserId, toUserName, amount, currency, note, createdAt)

// ActivityLog Mappers
fun ActivityLog.toEntity() = ActivityLogEntity(id, groupId, groupName, title, description, type.name, timestamp)
fun ActivityLogEntity.toDomain() = ActivityLog(id, groupId, groupName, title, description, try { ActivityType.valueOf(type) } catch (e: Exception) { ActivityType.EXPENSE_ADDED }, timestamp)
