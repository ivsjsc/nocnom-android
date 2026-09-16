package vn.ivsjsc.nocnom.domain.meal

import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

class DailySuggestionsTest {
    @Test
    fun refresh_preservesSkippedMealAndWritesVietnamDateMarker() {
        val now = Instant.parse("2026-09-16T12:30:00Z").toEpochMilli()
        val dayKey = VietnamTime.dayKey(now)
        val state = UserState(
            categories = listOf(Category("main", "Cơm"), Category("aux", "Trái cây")),
            dishes = listOf(
                Dish("d1", "Cơm gà", "main", nutrition = NutritionSnapshot(calories = 600.0)),
                Dish("d2", "Phở bò", "main", nutrition = NutritionSnapshot(calories = 520.0)),
                Dish("d3", "Cơm cá", "main", nutrition = NutritionSnapshot(calories = 650.0)),
                Dish("d4", "Dưa hấu", "aux", nutrition = NutritionSnapshot(calories = 120.0)),
            ),
            timetable = mapOf(
                dayKey to DayMenu(
                    dayName = "Thứ 4",
                    options = mapOf(
                        MealKey.A to MealSlot("d1"),
                        MealKey.B to MealSlot("d2", skipped = true),
                        MealKey.C to MealSlot("d3"),
                    ),
                ),
            ),
        )

        val result = DailySuggestions.refresh(state, now)
        val menu = result.state.timetable.getValue(dayKey)

        assertThat(result.changed).isTrue()
        assertThat(menu.suggestionDate).isEqualTo("2026-09-16")
        assertThat(menu.options.getValue(MealKey.B).skipped).isTrue()
        assertThat(menu.options.values.map { it.dishId }).doesNotContain("d4")
    }

    @Test
    fun refresh_isIdempotentWithinSameVietnamDay() {
        val now = Instant.parse("2026-09-16T12:30:00Z").toEpochMilli()
        val dayKey = VietnamTime.dayKey(now)
        val state = UserState(
            dishes = listOf(Dish("d1", "Cơm gà", "c1")),
            timetable = mapOf(
                dayKey to DayMenu(
                    dayName = "Thứ 4",
                    options = mapOf(
                        MealKey.A to MealSlot("d1"),
                        MealKey.B to MealSlot("d1"),
                        MealKey.C to MealSlot("d1"),
                    ),
                    suggestionDate = "2026-09-16",
                    suggestionVersion = DailySuggestions.VERSION,
                ),
            ),
        )

        assertThat(DailySuggestions.refresh(state, now).changed).isFalse()
    }
}
