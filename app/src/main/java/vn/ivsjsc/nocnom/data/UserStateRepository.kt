package vn.ivsjsc.nocnom.data

import kotlinx.coroutines.flow.StateFlow
import vn.ivsjsc.nocnom.domain.model.MealKey
import vn.ivsjsc.nocnom.domain.model.UserProfile
import vn.ivsjsc.nocnom.domain.model.UserState

interface UserStateRepository {
    val state: StateFlow<UserState>

    suspend fun refresh()
    suspend fun updateDailyCalorieTarget(target: Int)
    suspend fun updateMealSlot(mealKey: MealKey, dishId: String?, skipped: Boolean)
    suspend fun markMealEaten(mealKey: MealKey)
    suspend fun updateRecommendationPreferences(mode: String, mealBudgetVnd: Long?)
    suspend fun updateProfile(profile: UserProfile)
}
