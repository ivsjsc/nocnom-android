package vn.ivsjsc.nocnom.domain.meal

import java.text.Normalizer
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

object DailySuggestions {
    const val VERSION = 1
    private const val RECENT_LOG_DAYS = 4L
    private const val RECENT_SUGGESTION_DAYS = 3L

    private val mealKeys = listOf(MealKey.A, MealKey.B, MealKey.C)
    private val mealTargets = mapOf(
        MealKey.A to 450.0,
        MealKey.B to 650.0,
        MealKey.C to 650.0,
    )

    data class Result(val state: UserState, val changed: Boolean)

    fun refresh(state: UserState, now: Long = System.currentTimeMillis()): Result {
        val dateKey = VietnamTime.dateKey(now)
        val dayKey = VietnamTime.dayKey(now)
        val currentMenu = state.timetable[dayKey] ?: return Result(state, false)
        if (state.dishes.isEmpty()) return Result(state, false)
        if (currentMenu.suggestionDate == dateKey && currentMenu.suggestionVersion == VERSION) {
            return Result(state, false)
        }

        val categoryLookup = state.categories.associate { it.id to it.name }
        val mainDishes = state.dishes.filter { dish ->
            isLikelyMainDish(dish, categoryLookup[dish.categoryId].orEmpty())
        }
        val candidates = if (mainDishes.size >= mealKeys.size) mainDishes else state.dishes
        val recentDishIds = recentSuggestedDishIds(state.timetable, dateKey)
        val recentDishNames = recentConsumedDishNames(state.logs, dateKey)
        val consumedToday = state.logs
            .filter { it.mealKey != null && VietnamTime.dateKey(it.timestamp) == dateKey }
            .associateBy { it.mealKey!! }
        val usedDishIds = mutableSetOf<String>()
        val nextOptions = currentMenu.options.toMutableMap()

        mealKeys.forEach { mealKey ->
            val currentSlot = currentMenu.options[mealKey] ?: MealSlot()
            val eaten = consumedToday[mealKey]
            if (eaten != null) {
                val eatenName = normalize(eaten.dishName)
                val eatenDish = state.dishes.firstOrNull { normalize(it.name) == eatenName }
                val dishId = eatenDish?.id ?: currentSlot.dishId
                if (dishId.isNotBlank()) usedDishIds += dishId
                nextOptions[mealKey] = currentSlot.copy(dishId = dishId, skipped = false)
                return@forEach
            }

            if (currentSlot.skipped) {
                nextOptions[mealKey] = currentSlot.copy(skipped = true)
                return@forEach
            }

            val selected = chooseDish(
                mealKey = mealKey,
                dateKey = dateKey,
                candidates = candidates,
                categoryLookup = categoryLookup,
                usedDishIds = usedDishIds,
                recentDishIds = recentDishIds,
                recentDishNames = recentDishNames,
            ) ?: return@forEach

            usedDishIds += selected.id
            nextOptions[mealKey] = currentSlot.copy(dishId = selected.id, skipped = false)
        }

        val nextMenu = currentMenu.copy(
            options = nextOptions,
            suggestionDate = dateKey,
            suggestionVersion = VERSION,
        )
        return Result(
            state.copy(timetable = state.timetable + (dayKey to nextMenu)),
            true,
        )
    }

    private fun chooseDish(
        mealKey: MealKey,
        dateKey: String,
        candidates: List<Dish>,
        categoryLookup: Map<String, String>,
        usedDishIds: Set<String>,
        recentDishIds: Set<String>,
        recentDishNames: Set<String>,
    ): Dish? {
        val available = candidates.filterNot { it.id in usedDishIds }
        val pool = available.ifEmpty { candidates }
        val target = mealTargets.getValue(mealKey)

        return pool.minByOrNull { dish ->
            val calories = dishCalories(dish, categoryLookup[dish.categoryId].orEmpty())
            val calorieDistance = kotlin.math.abs(calories - target) / target
            val name = normalize(dish.name)
            val categoryName = categoryLookup[dish.categoryId].orEmpty()
            val mainPenalty = if (isLikelyMainDish(dish, categoryName)) 0.0 else 3.0
            val recentLogPenalty = if (name in recentDishNames) 2.5 else 0.0
            val recentSuggestionPenalty = if (dish.id in recentDishIds) 1.25 else 0.0
            val favoriteBonus = if (dish.isFavorite) -0.12 else 0.0
            val slotAffinity = if (mealKey == MealKey.A) breakfastPenalty(dish) else 0.0
            val variety = seededNoise("$dateKey|${mealKey.name}|${dish.id}") * 0.2
            calorieDistance * 1.5 +
                mainPenalty +
                recentLogPenalty +
                recentSuggestionPenalty +
                favoriteBonus +
                slotAffinity +
                variety
        }
    }

    private fun recentSuggestedDishIds(
        timetable: Map<String, DayMenu>,
        today: String,
    ): Set<String> {
        val recent = mutableSetOf<String>()
        timetable.values.forEach { menu ->
            val suggestionDate = menu.suggestionDate ?: return@forEach
            val age = dayDistance(today, suggestionDate) ?: return@forEach
            if (age !in 0..RECENT_SUGGESTION_DAYS) return@forEach
            mealKeys.forEach { key ->
                menu.options[key]?.dishId?.takeIf(String::isNotBlank)?.let(recent::add)
            }
        }
        return recent
    }

    private fun recentConsumedDishNames(logs: List<LogEntry>, today: String): Set<String> =
        logs.mapNotNull { log ->
            val age = dayDistance(today, VietnamTime.dateKey(log.timestamp)) ?: return@mapNotNull null
            if (age !in 0..RECENT_LOG_DAYS) return@mapNotNull null
            normalize(log.dishName).takeIf(String::isNotBlank)
        }.toSet()

    private fun dayDistance(newer: String, older: String): Long? = runCatching {
        ChronoUnit.DAYS.between(LocalDate.parse(older), LocalDate.parse(newer))
    }.getOrNull()

    private fun dishCalories(dish: Dish, categoryName: String): Double =
        dish.nutrition.calories?.takeIf { it > 0.0 } ?: categoryFallbackCalories(categoryName)

    private fun categoryFallbackCalories(categoryName: String): Double {
        val category = normalize(categoryName)
        if (
            "trai cay" in category ||
            "do uong" in category ||
            "thuc uong" in category ||
            "trang mieng" in category
        ) return 250.0
        if (
            "pho" in category ||
            "bun" in category ||
            "mi" in category ||
            "hu tieu" in category ||
            "mon nuoc" in category
        ) return 550.0
        if (
            "com" in category ||
            "mon man" in category ||
            "combo" in category ||
            "phan an" in category
        ) return 650.0
        return 500.0
    }

    private fun isLikelyMainDish(dish: Dish, categoryName: String): Boolean {
        val category = normalize(categoryName)
        if (
            "trai cay" in category ||
            "do uong" in category ||
            "thuc uong" in category ||
            "an vat" in category ||
            "trang mieng" in category ||
            "mon ngot" in category
        ) return false

        val name = normalize(dish.name)
        val signals = listOf(
            "com", "banh mi", "sandwich", "wrap", "salad", "bun", "pho",
            "mi", "mien", "chao", "xoi", "hu tieu", "thit", "ga", "ca",
            "bo", "hai san", "trung", "lau",
        )
        return signals.any { signal -> hasPhrase(name, signal) } || category.isNotBlank()
    }

    private fun breakfastPenalty(dish: Dish): Double {
        val name = normalize(dish.name)
        val preferred = listOf(
            "banh mi", "sandwich", "wrap", "pho", "bun", "mi", "mien",
            "chao", "xoi", "hu tieu", "trung", "sua chua",
        )
        if (preferred.any { hasPhrase(name, it) }) return -0.35
        if (hasPhrase(name, "lau")) return 0.9
        return 0.0
    }

    private fun hasPhrase(text: String, phrase: String): Boolean =
        " $text ".contains(" $phrase ")

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()

    private fun seededNoise(seed: String): Double {
        var hash = 2166136261L
        seed.forEach { ch ->
            hash = (hash xor ch.code.toLong()) * 16777619L
            hash = hash and 0xffffffffL
        }
        return hash.toDouble() / 4294967295.0
    }
}
