package com.techvipin130524.splitmate.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techvipin130524.splitmate.domain.model.User
import com.techvipin130524.splitmate.domain.repository.UserRepository
import com.techvipin130524.splitmate.feature.auth.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isUpdatingName: Boolean = false,
    val defaultCurrency: String = "INR",
    val notificationsEnabled: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.authState.collectLatest { authUser ->
                if (authUser != null) {
                    userRepository.observeUser(authUser.uid).collectLatest { user ->
                        if (user != null) {
                            _uiState.value = _uiState.value.copy(user = user, isLoading = false)
                        } else {
                            userRepository.getUserProfile(authUser.uid)
                                .onSuccess { fetched ->
                                    _uiState.value = _uiState.value.copy(user = fetched, isLoading = false)
                                }
                                .onFailure {
                                    _uiState.value = _uiState.value.copy(
                                        user = User(id = authUser.uid, name = authUser.name ?: "User", email = authUser.email ?: ""),
                                        isLoading = false
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    fun updateName(newName: String) {
        val currentUser = _uiState.value.user ?: return
        if (newName.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdatingName = true, error = null)
            val updatedUser = currentUser.copy(name = newName.trim(), updatedAt = System.currentTimeMillis())
            userRepository.createOrUpdateUser(updatedUser)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        isUpdatingName = false,
                        message = "Name updated successfully"
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isUpdatingName = false,
                        error = e.localizedMessage ?: "Failed to update name"
                    )
                }
        }
    }

    fun updateMonthlyBudget(budgetPaise: Long) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            val updatedUser = currentUser.copy(monthlyBudgetPaise = budgetPaise, updatedAt = System.currentTimeMillis())
            userRepository.createOrUpdateUser(updatedUser)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        message = "Monthly budget updated!"
                    )
                }
        }
    }

    fun updatePreferredUpiId(upiId: String) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            val updatedUser = currentUser.copy(preferredUpiId = upiId.trim(), updatedAt = System.currentTimeMillis())
            userRepository.createOrUpdateUser(updatedUser)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        user = updatedUser,
                        message = "Preferred UPI ID saved!"
                    )
                }
        }
    }

    fun updateDefaultCurrency(currency: String) {
        _uiState.value = _uiState.value.copy(defaultCurrency = currency, message = "Default currency set to $currency")
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}
