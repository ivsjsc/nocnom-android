package vn.ivsjsc.nocnom.domain.meal

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

class RecommendationEngineTest {
    @Test
    fun budgetMode_prefersLowerPriceWhenNutritionIsComparable() {
        val now = Instant.parse("2026-09-16T05:00:00Z").toEpochMilli()
        val dayKey = VietnamTime.dayKey(now)
        val cheap = Dish(
            id = "cheap",
            name = "Cơm gà A",
            categoryId = "main",
            vendors = listOf(Vendor("v1", "Quán A", price = 25_000)),
            nutrition = NutritionSnapshot(calories = 650.0),
        )
        val expensive = Dish(
            id = "expensive",
            name = "Cơm gà B",
            categoryId = "main",
            vendors = listOf(Vendor("v2", "Quán B", price = 65_000)),
            nutrition = NutritionSnapshot(calories = 650.0),
        )
        val state = UserState(
            categories = listOf(Category("main", "Cơm")),
            dishes = listOf(expensive, cheap),
            timetable = mapOf(
                dayKey to DayMenu(
                    dayName = "Thứ 4",
                    options = mapOf(
                        MealKey.A to MealSlot(),
                        MealKey.B to MealSlot(),
                        MealKey.C to MealSlot(),
                    ),
                ),
            ),
            profile = UserProfile(
                dailyCalorieTarget = 2000,
                recommendationMode = "budget",
                mealBudgetVnd = 35_000,
            ),
        )

        val ranked = RecommendationEngine.rank(state, MealKey.B, now)

        assertThat(ranked.first().dish.id).isEqualTo("cheap")
    }
}
