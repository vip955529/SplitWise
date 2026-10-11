package com.techvipin130524.splitmate.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techvipin130524.splitmate.domain.model.Expense
import com.techvipin130524.splitmate.domain.model.Friend
import com.techvipin130524.splitmate.domain.repository.ExpenseRepository
import com.techvipin130524.splitmate.domain.repository.FriendRepository
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendDetailUiState(
    val currentUserId: String = "",
    val friend: Friend? = null,
    val sharedExpenses: List<Expense> = emptyList(),
    val friendNetBalancePaise: Long = 0L,
    val isLoading: Boolean = true
)

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendDetailUiState())
    val uiState: StateFlow<FriendDetailUiState> = _uiState.asStateFlow()

    fun loadFriendDetails(friendId: String) {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authUser ->
                if (authUser != null) {
                    val currentUid = authUser.uid
                    _uiState.value = _uiState.value.copy(currentUserId = currentUid)

                    friendRepository.observeFriends(currentUid).collectLatest { friends ->
                        val friendObj = friends.find { it.id == friendId || it.friendUserId == friendId }
                        _uiState.value = _uiState.value.copy(friend = friendObj)

                        if (friendObj != null) {
                            expenseRepository.observeAllExpenses().collectLatest { allExpenses ->
                                val shared = allExpenses.filter { exp ->
                                    !exp.isDeleted && exp.participants.any { it.userId == friendObj.friendUserId }
                                }

                                var netBal = 0L
                                shared.forEach { exp ->
                                    val isPayer = exp.createdBy == currentUid || exp.payers.any { it.userId == currentUid }
                                    val friendShare = exp.participants.find { it.userId == friendObj.friendUserId }?.amount ?: 0L
                                    val myShare = exp.participants.find { it.userId == currentUid }?.amount ?: 0L

                                    if (isPayer) {
                                        netBal += friendShare
                                    } else if (exp.createdBy == friendObj.friendUserId || exp.payers.any { it.userId == friendObj.friendUserId }) {
                                        netBal -= myShare
                                    }
                                }

                                _uiState.value = _uiState.value.copy(
                                    sharedExpenses = shared,
                                    friendNetBalancePaise = netBal,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
