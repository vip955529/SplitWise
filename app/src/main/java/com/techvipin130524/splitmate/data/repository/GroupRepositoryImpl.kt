package com.techvipin130524.splitmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.techvipin130524.splitmate.data.local.dao.GroupDao
import com.techvipin130524.splitmate.data.mapper.toDomain
import com.techvipin130524.splitmate.data.mapper.toEntity
import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.model.GroupMember
import com.techvipin130524.splitmate.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val firestore: FirebaseFirestore
) : GroupRepository {

    override fun observeGroups(userId: String): Flow<List<Group>> {
        return groupDao.observeGroups().map { list ->
            list.map { entity ->
                val memberEntities = groupDao.getGroupMembers(entity.id)
                entity.toDomain(memberEntities.map { it.toDomain() })
            }
        }
    }

    override fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> {
        return groupDao.observeGroupMembers(groupId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getGroupDetails(groupId: String): Result<Group> {
        return try {
            val entity = groupDao.getGroupById(groupId)
            if (entity != null) {
                val members = groupDao.getGroupMembers(groupId).map { it.toDomain() }
                Result.success(entity.toDomain(members))
            } else {
                val snapshot = firestore.collection("groups").document(groupId).get().await()
                val group = snapshot.toObject(Group::class.java)
                if (group != null) {
                    groupDao.insertGroup(group.toEntity())
                    groupDao.insertGroupMembers(group.members.map { it.toEntity(groupId) })
                    Result.success(group)
                } else {
                    Result.failure(NoSuchElementException("Group not found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createGroup(
        name: String,
        description: String,
        createdByUserId: String,
        initialMembers: List<GroupMember>
    ): Result<Group> {
        return try {
            val groupId = UUID.randomUUID().toString()
            val inviteCode = "SPLIT-" + (1000..9999).random()
            val group = Group(
                id = groupId,
                name = name,
                description = description,
                inviteCode = inviteCode,
                createdBy = createdByUserId,
                members = initialMembers.map { it.copy(groupId = groupId) }
            )

            groupDao.insertGroup(group.toEntity())
            groupDao.insertGroupMembers(group.members.map { it.toEntity(groupId) })

            firestore.collection("groups").document(groupId).set(group).await()

            Result.success(group)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinGroupByCode(
        currentUserId: String,
        currentUserName: String,
        currentUserEmail: String,
        inviteCode: String
    ): Result<Group> {
        return try {
            val query = firestore.collection("groups")
                .whereEqualTo("inviteCode", inviteCode.trim().uppercase())
                .get()
                .await()

            val groupDoc = query.documents.firstOrNull()
                ?: return Result.failure(IllegalArgumentException("Group with invite code '$inviteCode' not found"))

            val group = groupDoc.toObject(Group::class.java)
                ?: return Result.failure(IllegalStateException("Failed to parse group data"))

            val newMember = GroupMember(
                groupId = group.id,
                userId = currentUserId,
                userName = currentUserName,
                userEmail = currentUserEmail,
                role = "MEMBER"
            )

            val updatedMembers: List<GroupMember> = if (group.members.none { it.userId == currentUserId }) {
                group.members + newMember
            } else {
                group.members
            }

            val updatedGroup = group.copy(members = updatedMembers)

            groupDao.insertGroup(updatedGroup.toEntity())
            groupDao.insertGroupMembers(updatedMembers.map { it.toEntity(group.id) })

            firestore.collection("groups").document(group.id).set(updatedGroup).await()

            Result.success(updatedGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addGroupMember(groupId: String, member: GroupMember): Result<Unit> {
        return try {
            val updatedMember = member.copy(groupId = groupId)
            groupDao.insertGroupMembers(listOf(updatedMember.toEntity(groupId)))

            val groupResult = getGroupDetails(groupId)
            if (groupResult.isSuccess) {
                val existingGroup = groupResult.getOrThrow()
                val updatedMembers = existingGroup.members + updatedMember
                val updatedGroup = existingGroup.copy(members = updatedMembers)
                firestore.collection("groups").document(groupId).set(updatedGroup).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMemberRole(groupId: String, userId: String, newRole: String): Result<Unit> {
        return try {
            val groupResult = getGroupDetails(groupId)
            if (groupResult.isSuccess) {
                val existingGroup = groupResult.getOrThrow()
                val updatedMembers = existingGroup.members.map { m ->
                    if (m.userId == userId) m.copy(role = newRole) else m
                }
                val updatedGroup = existingGroup.copy(members = updatedMembers)
                groupDao.insertGroupMembers(updatedMembers.map { it.toEntity(groupId) })
                firestore.collection("groups").document(groupId).set(updatedGroup).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeGroupMember(groupId: String, userId: String): Result<Unit> {
        return try {
            val groupResult = getGroupDetails(groupId)
            if (groupResult.isSuccess) {
                val existingGroup = groupResult.getOrThrow()
                val updatedMembers = existingGroup.members.filter { it.userId != userId }
                val updatedGroup = existingGroup.copy(members = updatedMembers)
                groupDao.insertGroupMembers(updatedMembers.map { it.toEntity(groupId) })
                firestore.collection("groups").document(groupId).set(updatedGroup).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
