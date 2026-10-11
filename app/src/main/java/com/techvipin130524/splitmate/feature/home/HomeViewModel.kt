package com.techvipin130524.splitmate.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techvipin130524.splitmate.domain.model.Expense
import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.repository.ExpenseRepository
import com.techvipin130524.splitmate.domain.repository.GroupRepository
import com.techvipin130524.splitmate.domain.repository.SettlementRepository
import com.techvipin130524.splitmate.domain.usecase.balance.DebtSimplifier
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.techvipin130524.splitmate.domain.model.ActivityLog
import com.techvipin130524.splitmate.domain.model.ExpenseCategory
import com.techvipin130524.splitmate.domain.repository.ActivityLogRepository
import com.techvipin130524.splitmate.domain.repository.UserRepository

data class HomeUiState(
    val currentUserId: String = "",
    val currentUserName: String = "",
    val youOwePaise: Long = 0L,
    val youAreOwedPaise: Long = 0L,
    val recentExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val searchQuery: String = "",
    val categorySpendMap: Map<ExpenseCategory, Long> = emptyMap(),
    val totalSpendPaise: Long = 0L,
    val monthlyBudgetPaise: Long = 0L,
    val activityLogs: List<ActivityLog> = emptyList(),
    val groups: List<Group> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val settlementRepository: SettlementRepository,
    private val groupRepository: GroupRepository,
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authUser ->
                if (authUser != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = authUser.uid,
                        currentUserName = authUser.name ?: "User"
                    )

                    val uid = authUser.uid

                    launch {
                        userRepository.observeUser(uid).collectLatest { user ->
                            if (user != null) {
                                _uiState.value = _uiState.value.copy(monthlyBudgetPaise = user.monthlyBudgetPaise)
                            }
                        }
                    }

                    launch {
                        expenseRepository.observeAllExpenses().collectLatest { expenses ->
                            _uiState.value = _uiState.value.copy(recentExpenses = expenses.take(10))
                            calculateOverallBalance(uid, expenses)
                            calculateCategoryAnalytics(expenses)
                            applyFilter()
                        }
                    }

                    launch {
                        groupRepository.observeGroups(uid).collectLatest { groups ->
                            _uiState.value = _uiState.value.copy(groups = groups, isLoading = false)
                        }
                    }

                    launch {
                        activityLogRepository.observeAllActivityLogs().collectLatest { logs ->
                            _uiState.value = _uiState.value.copy(activityLogs = logs)
                        }
                    }
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilter()
    }

    private fun applyFilter() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val all = _uiState.value.recentExpenses
        val filtered = if (query.isEmpty()) {
            all
        } else {
            all.filter {
                it.description.lowercase().contains(query) ||
                        it.category.displayName.lowercase().contains(query) ||
                        it.createdByName.lowercase().contains(query)
            }
        }
        _uiState.value = _uiState.value.copy(filteredExpenses = filtered)
    }

    private fun calculateCategoryAnalytics(expenses: List<Expense>) {
        val categoryMap = mutableMapOf<ExpenseCategory, Long>()
        var total = 0L

        for (expense in expenses) {
            if (expense.isDeleted) continue
            val current = categoryMap[expense.category] ?: 0L
            categoryMap[expense.category] = current + expense.totalAmount
            total += expense.totalAmount
        }

        _uiState.value = _uiState.value.copy(
            categorySpendMap = categoryMap,
            totalSpendPaise = total
        )
    }

    private fun calculateOverallBalance(uid: String, expenses: List<Expense>) {
        var oweSum = 0L
        var owedSum = 0L

        for (expense in expenses) {
            if (expense.isDeleted) continue

            val isPayer = expense.createdBy == uid || expense.payers.any { it.userId == uid }
            val myParticipant = expense.participants.find { it.userId == uid }
            val myShare = myParticipant?.amount ?: 0L

            if (isPayer) {
                // If I paid, total minus my share is owed to me
                val amountLent = expense.totalAmount - myShare
                if (amountLent > 0) owedSum += amountLent
            } else if (myParticipant != null) {
                // I am a participant, so I owe my share
                oweSum += myShare
            }
        }

        _uiState.value = _uiState.value.copy(
            youOwePaise = oweSum,
            youAreOwedPaise = owedSum
        )
    }
}
