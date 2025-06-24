package com.tech.matchmate.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.matchmate.data.repository.UserRepository
import com.tech.matchmate.domain.models.User
import com.tech.matchmate.domain.models.enums.MatchStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()


    val pendingUsers: StateFlow<List<User>> = _users.map { users ->
        users.filter { it.status == MatchStatus.PENDING }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val acceptedUsers: StateFlow<List<User>> = _users.map { users ->
        users.filter { it.status == MatchStatus.ACCEPTED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val declinedUsers: StateFlow<List<User>> = _users.map { users ->
        users.filter { it.status == MatchStatus.DECLINED }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {

                val result: Result<List<User>> = userRepository.fetchAndStoreUsers()

                if (result.isSuccess) {
                    val fetchedUsers: List<User> = result.getOrNull() ?: emptyList()
                    val usersWithScores = fetchedUsers.map { user ->
                        user.copy(matchScore = userRepository.calculateMatchScore(user))
                    }
                    _users.value = usersWithScores
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    val cachedUsers = userRepository.getCachedUsers()
                    if (cachedUsers.isNotEmpty()) {
                        _users.value = cachedUsers
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Using offline data - ${result.exceptionOrNull()?.message}"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "No cached data available - ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error loading users: ${e.message}"
                )
            }
        }
    }

    fun acceptUser(userId: String) {
        updateUserStatus(userId, MatchStatus.ACCEPTED)
    }

    fun declineUser(userId: String) {
        updateUserStatus(userId, MatchStatus.DECLINED)
    }

    fun undoAction(userId: String) {
        updateUserStatus(userId, MatchStatus.PENDING)
    }

    private fun updateUserStatus(userId: String, status: MatchStatus) {
        viewModelScope.launch {
            try {
                userRepository.updateUserStatus(userId, status)
                _users.value = _users.value.map { user ->
                    if (user.id == userId) user.copy(status = status) else user
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error updating user status: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retryLoading() {
        loadUsers()
    }
}


data class UiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
