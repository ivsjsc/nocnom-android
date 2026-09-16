package vn.ivsjsc.nocnom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.ivsjsc.nocnom.data.UserStateRepository
import vn.ivsjsc.nocnom.domain.model.UserState

@HiltViewModel
class NocnomViewModel @Inject constructor(
    private val repository: UserStateRepository,
) : ViewModel() {
    val state: StateFlow<UserState> = repository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = repository.state.value,
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { runCatching { repository.refresh() } }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch { runCatching { repository.updateDailyCalorieTarget(target) } }
    }
}
