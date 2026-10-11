package com.techvipin130524.splitmate.feature.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techvipin130524.splitmate.core.utils.CurrencyUtils
import com.techvipin130524.splitmate.domain.model.Expense
import com.techvipin130524.splitmate.domain.model.ExpenseParticipant
import com.techvipin130524.splitmate.domain.model.ExpensePayer
import com.techvipin130524.splitmate.domain.model.Group
import com.techvipin130524.splitmate.domain.model.SplitType
import com.techvipin130524.splitmate.domain.repository.ExpenseRepository
import com.techvipin130524.splitmate.domain.repository.GroupRepository
import com.techvipin130524.splitmate.domain.usecase.split.SplitCalculator
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.techvipin130524.splitmate.domain.model.ActivityLog
import com.techvipin130524.splitmate.domain.model.ActivityType
import com.techvipin130524.splitmate.domain.model.ExpenseCategory
import com.techvipin130524.splitmate.domain.model.ExpenseComment
import com.techvipin130524.splitmate.domain.repository.ActivityLogRepository
import com.techvipin130524.splitmate.domain.repository.ExpenseCommentRepository

data class ExpensesUiState(
    val currentUserId: String = "",
    val currentUserName: String = "",
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val description: String = "",
    val totalAmountString: String = "",
    val note: String = "",
    val receiptImageUrl: String = "",
    val selectedPayerId: String = "",
    val isMultiplePayers: Boolean = false,
    val payerAmounts: Map<String, String> = emptyMap(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val splitType: SplitType = SplitType.EQUAL,
    val selectedParticipantIds: Set<String> = emptySet(),
    val exactAmounts: Map<String, String> = emptyMap(),
    val percentages: Map<String, String> = emptyMap(),
    val shares: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val selectedExpense: Expense? = null,
    val comments: List<ExpenseComment> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val activityLogRepository: ActivityLogRepository,
    private val expenseCommentRepository: ExpenseCommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authUser ->
                if (authUser != null) {
                    _uiState.value = _uiState.value.copy(
                        currentUserId = authUser.uid,
                        currentUserName = authUser.name ?: "User",
                        selectedPayerId = authUser.uid
                    )

                    groupRepository.observeGroups(authUser.uid).collectLatest { groups ->
                        _uiState.value = _uiState.value.copy(groups = groups)
                        if (groups.isNotEmpty() && _uiState.value.selectedGroup == null) {
                            selectGroup(groups.first().id)
                        }
                    }
                }
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            groupRepository.getGroupDetails(groupId).onSuccess { group ->
                val allMemberIds = group.members.map { it.userId }.toSet()
                _uiState.value = _uiState.value.copy(
                    selectedGroup = group,
                    selectedParticipantIds = allMemberIds
                )
            }
        }
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateTotalAmount(value: String) {
        _uiState.value = _uiState.value.copy(totalAmountString = value)
    }

    fun updatePayer(userId: String) {
        _uiState.value = _uiState.value.copy(selectedPayerId = userId)
    }

    fun toggleMultiplePayers(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isMultiplePayers = enabled)
    }

    fun updatePayerAmount(userId: String, amountString: String) {
        val updated = _uiState.value.payerAmounts.toMutableMap()
        updated[userId] = amountString
        _uiState.value = _uiState.value.copy(payerAmounts = updated)
    }

    fun updateCategory(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateReceiptImageUrl(url: String) {
        _uiState.value = _uiState.value.copy(receiptImageUrl = url)
    }

    fun updateSplitType(type: SplitType) {
        _uiState.value = _uiState.value.copy(splitType = type)
    }

    fun toggleParticipant(userId: String) {
        val current = _uiState.value.selectedParticipantIds.toMutableSet()
        if (current.contains(userId)) current.remove(userId) else current.add(userId)
        _uiState.value = _uiState.value.copy(selectedParticipantIds = current)
    }

    fun updateExactAmount(userId: String, amountString: String) {
        val updated = _uiState.value.exactAmounts.toMutableMap()
        updated[userId] = amountString
        _uiState.value = _uiState.value.copy(exactAmounts = updated)
    }

    fun updatePercentage(userId: String, pctString: String) {
        val updated = _uiState.value.percentages.toMutableMap()
        updated[userId] = pctString
        _uiState.value = _uiState.value.copy(percentages = updated)
    }

    fun updateShares(userId: String, sharesString: String) {
        val updated = _uiState.value.shares.toMutableMap()
        updated[userId] = sharesString
        _uiState.value = _uiState.value.copy(shares = updated)
    }

    fun saveExpense(onSaved: () -> Unit) {
        val state = _uiState.value
        val group = state.selectedGroup ?: run {
            _uiState.value = state.copy(error = "Please select a group")
            return
        }

        val totalPaise = CurrencyUtils.parseToPaise(state.totalAmountString)
        if (totalPaise == null || totalPaise <= 0) {
            _uiState.value = state.copy(error = "Please enter a valid amount")
            return
        }

        if (state.description.isBlank()) {
            _uiState.value = state.copy(error = "Please enter expense description")
            return
        }

        val participantsInfo = group.members
            .filter { it.userId in state.selectedParticipantIds }
            .map { it.userId to it.userName }

        if (participantsInfo.isEmpty()) {
            _uiState.value = state.copy(error = "Please select at least one participant")
            return
        }

        val splitResult: Result<List<ExpenseParticipant>> = when (state.splitType) {
            SplitType.EQUAL -> {
                Result.success(SplitCalculator.calculateEqualSplit(totalPaise, participantsInfo))
            }
            SplitType.EXACT -> {
                val exactMap = participantsInfo.associate { (id, _) ->
                    id to (CurrencyUtils.parseToPaise(state.exactAmounts[id] ?: "0") ?: 0L)
                }
                SplitCalculator.calculateExactSplit(totalPaise, participantsInfo, exactMap)
            }
            SplitType.PERCENTAGE -> {
                val pctMap = participantsInfo.associate { (id, _) ->
                    id to (state.percentages[id]?.toDoubleOrNull() ?: 0.0)
                }
                SplitCalculator.calculatePercentageSplit(totalPaise, participantsInfo, pctMap)
            }
            SplitType.SHARES -> {
                val sharesMap = participantsInfo.associate { (id, _) ->
                    id to (state.shares[id]?.toIntOrNull() ?: 1)
                }
                SplitCalculator.calculateSharesSplit(totalPaise, participantsInfo, sharesMap)
            }
        }

        splitResult.onSuccess { participants ->
            val payersList = if (state.isMultiplePayers) {
                val list = group.members.mapNotNull { m ->
                    val amtPaise = CurrencyUtils.parseToPaise(state.payerAmounts[m.userId] ?: "0") ?: 0L
                    if (amtPaise > 0) ExpensePayer(userId = m.userId, userName = m.userName, amount = amtPaise) else null
                }
                val payersSum = list.sumOf { it.amount }
                if (payersSum != totalPaise) {
                    _uiState.value = state.copy(error = "Sum of payer amounts (${CurrencyUtils.formatPaise(payersSum)}) must equal total amount (${CurrencyUtils.formatPaise(totalPaise)})")
                    return
                }
                list
            } else {
                val singlePayerName = group.members.find { it.userId == state.selectedPayerId }?.userName ?: state.currentUserName
                listOf(ExpensePayer(userId = state.selectedPayerId, userName = singlePayerName, amount = totalPaise))
            }

            val primaryPayerName = payersList.firstOrNull()?.userName ?: state.currentUserName

            val expense = Expense(
                groupId = group.id,
                description = state.description.trim(),
                totalAmount = totalPaise,
                category = state.category,
                note = state.note.trim(),
                receiptImageUrl = state.receiptImageUrl.trim().ifEmpty { null },
                createdBy = state.currentUserId,
                createdByName = primaryPayerName,
                createdAt = System.currentTimeMillis(),
                participants = participants,
                payers = payersList
            )

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                expenseRepository.addExpense(expense).onSuccess {
                    activityLogRepository.logActivity(
                        ActivityLog(
                            groupId = group.id,
                            groupName = group.name,
                            title = "Added '${expense.description}'",
                            description = "Total ${CurrencyUtils.formatPaise(totalPaise)} paid by $primaryPayerName",
                            type = ActivityType.EXPENSE_ADDED
                        )
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, message = "Expense saved!")
                    onSaved()
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage ?: "Failed to save expense")
                }
            }
        }.onFailure { e ->
            _uiState.value = state.copy(error = e.message ?: "Invalid split configuration")
        }
    }

    fun loadExpenseDetails(expenseId: String) {
        viewModelScope.launch {
            expenseRepository.getExpenseDetails(expenseId).onSuccess { expense ->
                _uiState.value = _uiState.value.copy(selectedExpense = expense)
            }
            expenseCommentRepository.observeCommentsForExpense(expenseId).collectLatest { commentList ->
                _uiState.value = _uiState.value.copy(comments = commentList)
            }
        }
    }

    fun addComment(expenseId: String, text: String) {
        val state = _uiState.value
        if (text.isBlank()) return
        viewModelScope.launch {
            val comment = ExpenseComment(
                expenseId = expenseId,
                userId = state.currentUserId,
                userName = state.currentUserName,
                commentText = text.trim()
            )
            expenseCommentRepository.addComment(comment)
        }
    }

    fun deleteExpense(expenseId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
            expenseRepository.deleteExpense(expenseId).onSuccess {
                _uiState.value = _uiState.value.copy(isSaving = false, message = "Expense deleted")
                onDeleted()
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.localizedMessage ?: "Failed to delete expense")
            }
        }
    }

    fun convertCurrencyToRupees(amount: Double, currencyCode: String): Double {
        val rate = when (currencyCode.uppercase()) {
            "USD" -> 83.0
            "EUR" -> 90.0
            "GBP" -> 105.0
            "CAD" -> 61.0
            "AED" -> 22.6
            "JPY" -> 0.55
            else -> 1.0
        }
        return amount * rate
    }

    fun scanReceiptSimulated(onScanned: (String, String, ExpenseCategory) -> Unit) {
        val sampleReceipts = listOf(
            Triple("Starbucks Coffee & Snacks", "450.00", ExpenseCategory.FOOD),
            Triple("Uber Cab Ride", "380.00", ExpenseCategory.TRANSPORT),
            Triple("Supermarket Groceries", "1250.00", ExpenseCategory.GROCERIES),
            Triple("Hotel Dinner Buffet", "2400.00", ExpenseCategory.HOTEL)
        )
        val sampled = sampleReceipts.random()
        _uiState.value = _uiState.value.copy(
            description = sampled.first,
            totalAmountString = sampled.second,
            category = sampled.third,
            note = "Auto-scanned via Receipt AI"
        )
        onScanned(sampled.first, sampled.second, sampled.third)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
