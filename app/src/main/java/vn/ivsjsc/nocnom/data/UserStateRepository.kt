package vn.ivsjsc.nocnom.data

import kotlinx.coroutines.flow.StateFlow
import vn.ivsjsc.nocnom.domain.model.UserState

interface UserStateRepository {
    val state: StateFlow<UserState>
    suspend fun refresh()
    suspend fun updateDailyCalorieTarget(target: Int)
}
