package vn.ivsjsc.nocnom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.ivsjsc.nocnom.data.UserStateRepository
import vn.ivsjsc.nocnom.data.auth.AuthRepository
import vn.ivsjsc.nocnom.data.auth.AuthState
import vn.ivsjsc.nocnom.domain.model.UserState

@HiltViewModel
class NocnomViewModel @Inject constructor(
    private val repository: UserStateRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val state: StateFlow<UserState> = repository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.state.value,
    )

    val authState: StateFlow<AuthState> = authRepository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = authRepository.state.value,
    )

    init {
        viewModelScope.launch {
            authRepository.state.collectLatest { auth ->
                if (auth.user != null) {
                    runCatching { repository.refresh() }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refresh() } }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch { runCatching { repository.updateDailyCalorieTarget(target) } }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch { authRepository.signInWithEmail(email, password) }
    }

    fun createAccount(email: String, password: String) {
        viewModelScope.launch { authRepository.createAccount(email, password) }
    }

    fun signInWithGoogleToken(idToken: String) {
        viewModelScope.launch { authRepository.signInWithGoogleIdToken(idToken) }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch { authRepository.sendPasswordReset(email) }
    }

    fun signOut() = authRepository.signOut()

    fun clearAuthMessage() = authRepository.clearMessage()
}
