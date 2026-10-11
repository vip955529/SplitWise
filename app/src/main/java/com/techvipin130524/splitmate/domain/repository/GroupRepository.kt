package com.techvipin130524.splitmate.domain.repository

import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.model.GroupMember
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    fun observeGroups(userId: String): Flow<List<Group>>
    fun observeGroupMembers(groupId: String): Flow<List<GroupMember>>
    suspend fun getGroupDetails(groupId: String): Result<Group>
    suspend fun createGroup(name: String, description: String, createdByUserId: String, initialMembers: List<GroupMember>): Result<Group>
    suspend fun addGroupMember(groupId: String, member: GroupMember): Result<Unit>
    suspend fun joinGroupByCode(currentUserId: String, currentUserName: String, currentUserEmail: String, inviteCode: String): Result<Group>
    suspend fun updateMemberRole(groupId: String, userId: String, newRole: String): Result<Unit>
    suspend fun removeGroupMember(groupId: String, userId: String): Result<Unit>
}
