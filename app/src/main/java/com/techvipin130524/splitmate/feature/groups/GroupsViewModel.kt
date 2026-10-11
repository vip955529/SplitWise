package com.techvipin130524.splitmate.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techvipin130524.splitmate.domain.model.ActivityLog
import com.techvipin130524.splitmate.domain.model.Expense
import com.techvipin130524.splitmate.domain.repository.ActivityLogRepository
import com.techvipin130524.splitmate.domain.model.Friend
import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.model.GroupMember
import com.techvipin130524.splitmate.domain.model.Settlement
import com.techvipin130524.splitmate.domain.repository.ExpenseRepository
import com.techvipin130524.splitmate.domain.repository.FriendRepository
import com.techvipin130524.splitmate.domain.repository.GroupRepository
import com.techvipin130524.splitmate.domain.repository.SettlementRepository
import com.techvipin130524.splitmate.domain.usecase.balance.DebtSimplifier
import com.techvipin130524.splitmate.domain.usecase.balance.DebtTransaction
import com.techvipin130524.splitmate.domain.usecase.balance.UserNetBalance
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupsUiState(
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserEmail: String = "",
    val groups: List<Group> = emptyList(),
    val friends: List<Friend> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingGroup: Boolean = false,
    val selectedGroup: Group? = null,
    val groupExpenses: List<Expense> = emptyList(),
    val groupSettlements: List<Settlement> = emptyList(),
    val groupActivityLogs: List<ActivityLog> = emptyList(),
    val groupNetBalances: List<UserNetBalance> = emptyList(),
    val groupSimplifiedDebts: List<DebtTransaction> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val friendRepository: FriendRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authUser ->
                if (authUser != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = authUser.uid,
                        currentUserName = authUser.name ?: "User",
                        currentUserEmail = authUser.email ?: ""
                    )

                    launch {
                        groupRepository.observeGroups(authUser.uid).collectLatest { groups ->
                            _uiState.value = _uiState.value.copy(groups = groups, isLoading = false)
                        }
                    }

                    launch {
                        friendRepository.observeFriends(authUser.uid).collectLatest { friends ->
                            _uiState.value = _uiState.value.copy(friends = friends)
                        }
                    }
                }
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            groupRepository.getGroupDetails(groupId).onSuccess { group ->
                _uiState.value = _uiState.value.copy(selectedGroup = group)

                val userNames = group.members.associate { it.userId to it.userName }

                launch {
                    expenseRepository.observeExpensesForGroup(groupId).collectLatest { expenses ->
                        _uiState.value = _uiState.value.copy(groupExpenses = expenses)
                        recalculateBalances(userNames)
                    }
                }

                launch {
                    settlementRepository.observeSettlementsForGroup(groupId).collectLatest { settlements ->
                        _uiState.value = _uiState.value.copy(groupSettlements = settlements)
                        recalculateBalances(userNames)
                    }
                }

                launch {
                    activityLogRepository.observeActivityLogsForGroup(groupId).collectLatest { logs ->
                        _uiState.value = _uiState.value.copy(groupActivityLogs = logs)
                    }
                }
            }
        }
    }

    private fun recalculateBalances(userNames: Map<String, String>) {
        val state = _uiState.value
        val netBalances = DebtSimplifier.calculateNetBalances(userNames, state.groupExpenses, state.groupSettlements)
        val simplifiedDebts = DebtSimplifier.simplifyDebts(netBalances)
        _uiState.value = _uiState.value.copy(
            groupNetBalances = netBalances,
            groupSimplifiedDebts = simplifiedDebts
        )
    }

    fun createGroup(name: String, description: String, selectedFriendIds: List<String>, onCreated: (String) -> Unit) {
        val state = _uiState.value
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGroup = true, error = null)

            val currentUserMember = GroupMember(
                userId = state.currentUserId,
                userName = state.currentUserName,
                userEmail = state.currentUserEmail,
                role = "ADMIN"
            )

            val selectedFriendMembers = state.friends
                .filter { it.friendUserId in selectedFriendIds }
                .map {
                    GroupMember(
                        userId = it.friendUserId,
                        userName = it.friendName,
                        userEmail = it.friendEmail,
                        role = "MEMBER"
                    )
                }

            val allMembers = listOf(currentUserMember) + selectedFriendMembers

            groupRepository.createGroup(
                name = name.trim(),
                description = description.trim(),
                createdByUserId = state.currentUserId,
                initialMembers = allMembers
            ).onSuccess { group ->
                _uiState.value = _uiState.value.copy(isCreatingGroup = false, message = "Group created!")
                onCreated(group.id)
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isCreatingGroup = false,
                    error = e.localizedMessage ?: "Failed to create group"
                )
            }
        }
    }

    fun joinGroupByCode(code: String) {
        val state = _uiState.value
        if (code.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            groupRepository.joinGroupByCode(
                currentUserId = state.currentUserId,
                currentUserName = state.currentUserName,
                currentUserEmail = state.currentUserEmail,
                inviteCode = code
            ).onSuccess { group ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = "Joined group '${group.name}'!"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Failed to join group"
                )
            }
        }
    }

    fun updateMemberRole(groupId: String, userId: String, newRole: String) {
        viewModelScope.launch {
            groupRepository.updateMemberRole(groupId, userId, newRole).onSuccess {
                selectGroup(groupId)
            }
        }
    }

    fun removeMember(groupId: String, userId: String) {
        viewModelScope.launch {
            groupRepository.removeGroupMember(groupId, userId).onSuccess {
                selectGroup(groupId)
            }
        }
    }

    fun leaveGroup(groupId: String, onLeft: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            groupRepository.removeGroupMember(groupId, state.currentUserId).onSuccess {
                onLeft()
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
