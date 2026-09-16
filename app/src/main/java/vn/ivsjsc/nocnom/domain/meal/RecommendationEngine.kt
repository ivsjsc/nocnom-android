package vn.ivsjsc.nocnom.domain.meal

import java.text.Normalizer
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

object RecommendationEngine {
    data class Recommendation(
        val dish: Dish,
        val score: Double,
        val targetCalories: Int,
        val minPrice: Long?,
    )

    private data class Weights(
        val kcal: Double,
        val preference: Double,
        val budget: Double,
        val convenience: Double,
        val variety: Double,
    )

    private val weights = mapOf(
        "balanced" to Weights(0.35, 0.25, 0.20, 0.10, 0.10),
        "budget" to Weights(0.25, 0.15, 0.45, 0.10, 0.05),
        "variety" to Weights(0.20, 0.15, 0.10, 0.05, 0.50),
        "quick" to Weights(0.25, 0.15, 0.10, 0.45, 0.05),
    )

    private val baseShare = mapOf(
        MealKey.A to 0.25,
        MealKey.B to 0.375,
        MealKey.C to 0.375,
    )

    private val fallbackTarget = mapOf(
        MealKey.A to 450,
        MealKey.B to 650,
        MealKey.C to 650,
    )

    fun rank(
        state: UserState,
        mealKey: MealKey,
        now: Long = System.currentTimeMillis(),
    ): List<Recommendation> {
        val today = VietnamTime.dateKey(now)
        val todayMenu = state.timetable[VietnamTime.dayKey(now)]
        val consumedToday = state.logs
            .filter { VietnamTime.dateKey(it.timestamp) == today }
            .sumOf { it.nutrition.calories ?: 0.0 }
        val eatenMeals = state.logs
            .filter { VietnamTime.dateKey(it.timestamp) == today }
            .mapNotNull { it.mealKey }
            .toSet()
        val remaining = listOf(MealKey.A, MealKey.B, MealKey.C).filter { key ->
            key !in eatenMeals && todayMenu?.options?.get(key)?.skipped != true
        }
        val target = adaptiveTarget(
            mealKey = mealKey,
            dailyTarget = state.profile.dailyCalorieTarget,
            consumed = consumedToday,
            remaining = remaining,
        )

        val categories = state.categories.associate { it.id to it.name }
        val mode = state.profile.recommendationMode.takeIf(weights::containsKey) ?: "balanced"
        val w = weights.getValue(mode)
        val budget = state.profile.mealBudgetVnd?.takeIf { it > 0 }
        val history = HistorySignals.build(state, today)

        val candidates = state.dishes.filter { dish ->
            isLikelyMainDish(dish, categories[dish.categoryId].orEmpty())
        }.ifEmpty { state.dishes }

        return candidates.map { dish ->
            val calories = dish.nutrition.calories?.takeIf { it > 0 } ?: categoryFallbackCalories(
                categories[dish.categoryId].orEmpty(),
            )
            val kcalScore = clamp01(1.0 - abs(calories - target) / target.coerceAtLeast(1))
            val preference = preferenceScore(dish, history)
            val variety = varietyScore(dish, history)
            val convenience = convenienceScore(dish)
            val minPrice = dish.vendors.map { it.price }.filter { it > 0 }.minOrNull()
            val budgetScore = budgetScore(minPrice, budget)
            val breakfastBonus = breakfastAffinity(mealKey, dish)
            val score =
                w.kcal * kcalScore +
                w.preference * preference +
                w.budget * budgetScore +
                w.convenience * convenience +
                w.variety * variety +
                breakfastBonus +
                deterministicTieBreaker("$today|${mealKey.name}|${dish.id}")

            Recommendation(
                dish = dish,
                score = score,
                targetCalories = target,
                minPrice = minPrice,
            )
        }.sortedByDescending { it.score }
    }

    private fun adaptiveTarget(
        mealKey: MealKey,
        dailyTarget: Int,
        consumed: Double,
        remaining: List<MealKey>,
    ): Int {
        if (dailyTarget <= 0) return fallbackTarget.getValue(mealKey)
        val active = remaining.ifEmpty { listOf(MealKey.A, MealKey.B, MealKey.C) }
        val keys = if (mealKey in active) active else active + mealKey
        val shareTotal = keys.sumOf { baseShare.getValue(it) }
        val remainingEnergy = (dailyTarget - consumed).coerceAtLeast(0.0)
        val raw = if (shareTotal > 0) {
            remainingEnergy * (baseShare.getValue(mealKey) / shareTotal)
        } else {
            fallbackTarget.getValue(mealKey).toDouble()
        }
        val bounds = when (mealKey) {
            MealKey.A -> 250..700
            MealKey.B, MealKey.C -> 350..950
        }
        return raw.toInt().coerceIn(bounds)
    }

    private data class HistorySignals(
        val nameCount: Map<String, Int>,
        val categoryCount: Map<String, Int>,
        val lastNameDays: Map<String, Long>,
        val lastCategoryDays: Map<String, Long>,
    ) {
        companion object {
            fun build(state: UserState, today: String): HistorySignals {
                val nameCount = mutableMapOf<String, Int>()
                val categoryCount = mutableMapOf<String, Int>()
                val lastNameDays = mutableMapOf<String, Long>()
                val lastCategoryDays = mutableMapOf<String, Long>()
                val dishesByName = state.dishes.associateBy { normalize(it.name) }

                state.logs.forEach { log ->
                    val age = dayDistance(today, VietnamTime.dateKey(log.timestamp)) ?: return@forEach
                    if (age !in 0..30) return@forEach
                    val name = normalize(log.dishName)
                    if (name.isBlank()) return@forEach
                    nameCount[name] = (nameCount[name] ?: 0) + 1
                    lastNameDays[name] = minOf(lastNameDays[name] ?: age, age)
                    val categoryId = dishesByName[name]?.categoryId ?: return@forEach
                    categoryCount[categoryId] = (categoryCount[categoryId] ?: 0) + 1
                    lastCategoryDays[categoryId] =
                        minOf(lastCategoryDays[categoryId] ?: age, age)
                }
                return HistorySignals(
                    nameCount = nameCount,
                    categoryCount = categoryCount,
                    lastNameDays = lastNameDays,
                    lastCategoryDays = lastCategoryDays,
                )
            }
        }
    }

    private fun preferenceScore(dish: Dish, history: HistorySignals): Double {
        val name = normalize(dish.name)
        val exact = history.nameCount[name] ?: 0
        val category = history.categoryCount[dish.categoryId] ?: 0
        return clamp01(
            (if (dish.isFavorite) 0.45 else 0.0) +
                minOf(exact / 4.0, 1.0) * 0.30 +
                minOf(category / 6.0, 1.0) * 0.15,
        )
    }

    private fun varietyScore(dish: Dish, history: HistorySignals): Double {
        val age = history.lastNameDays[normalize(dish.name)]
        val dishScore = when {
            age == null || age >= 7 -> 1.0
            age >= 4 -> 0.8
            age >= 2 -> 0.55
            age == 1L -> 0.2
            else -> 0.05
        }
        val categoryAge = history.lastCategoryDays[dish.categoryId]
        val categoryFactor = when {
            categoryAge == null || categoryAge >= 3 -> 1.0
            categoryAge == 2L -> 0.9
            categoryAge == 1L -> 0.75
            else -> 0.65
        }
        return clamp01(dishScore * categoryFactor)
    }

    private fun convenienceScore(dish: Dish): Double {
        if (dish.vendors.isEmpty()) return 0.0
        if (dish.vendors.any { !it.link.isNullOrBlank() }) return 1.0
        if (dish.vendors.any { it.phone.isNotBlank() || it.address.isNotBlank() }) return 0.55
        return 0.35
    }

    private fun budgetScore(price: Long?, budget: Long?): Double {
        if (price == null) return 0.35
        if (budget == null) return 0.60
        return if (price <= budget) {
            clamp01(0.60 + 0.40 * (1.0 - price.toDouble() / budget))
        } else {
            clamp01(0.60 - ((price - budget).toDouble() / budget) * 1.20)
        }
    }

    private fun breakfastAffinity(mealKey: MealKey, dish: Dish): Double {
        if (mealKey != MealKey.A) return 0.0
        val name = normalize(dish.name)
        val preferred = listOf(
            "banh mi", "sandwich", "wrap", "pho", "bun", "mi", "mien",
            "chao", "xoi", "hu tieu", "trung",
        )
        if (preferred.any { hasPhrase(name, it) }) return 0.08
        if (hasPhrase(name, "lau")) return -0.12
        return 0.0
    }

    private fun categoryFallbackCalories(categoryName: String): Double {
        val category = normalize(categoryName)
        if (
            "trai cay" in category ||
            "do uong" in category ||
            "thuc uong" in category ||
            "trang mieng" in category ||
            "an vat" in category
        ) return 250.0
        if (
            "pho" in category || "bun" in category || "mi" in category ||
            "hu tieu" in category || "mon nuoc" in category
        ) return 550.0
        if (
            "com" in category || "mon man" in category ||
            "combo" in category || "phan an" in category
        ) return 650.0
        return 500.0
    }

    private fun isLikelyMainDish(dish: Dish, categoryName: String): Boolean {
        val category = normalize(categoryName)
        val auxiliary =
            "trai cay" in category ||
                "do uong" in category ||
                "thuc uong" in category ||
                "an vat" in category ||
                "trang mieng" in category ||
                "mon ngot" in category
        if (auxiliary) return false
        return dish.name.isNotBlank()
    }

    private fun deterministicTieBreaker(seed: String): Double =
        ((seed.hashCode().toLong() and 0xffffffffL) / 4294967295.0) * 0.0001

    private fun clamp01(value: Double) = value.coerceIn(0.0, 1.0)

    private fun hasPhrase(text: String, phrase: String): Boolean =
        " $text ".contains(" $phrase ")

    private fun dayDistance(newer: String, older: String): Long? = runCatching {
        ChronoUnit.DAYS.between(LocalDate.parse(older), LocalDate.parse(newer))
    }.getOrNull()

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
}
