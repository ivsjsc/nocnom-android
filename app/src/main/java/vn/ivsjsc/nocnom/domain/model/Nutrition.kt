package vn.ivsjsc.nocnom.domain.model

enum class CalorieSource { MANUAL, NUTRITION_DB, CATEGORY_FALLBACK, LEGACY }
enum class CalorieBasis { PORTION, GRAMS, CATEGORY, SERVING, PER_100G }
enum class ServingUnit { G, ML, PORTION }
enum class MacroSource { NUTRITION_DB, MANUAL, RECIPE }
enum class NutritionConfidence { HIGH, MEDIUM, LOW, REFERENCE, UNKNOWN, VERIFIED, ESTIMATED }
enum class MacroEnergyConsistency { NOT_APPLICABLE, CONSISTENT, REVIEW, INCONSISTENT }

data class NutritionSnapshot(
    val calories: Double? = null,
    val calorieSource: CalorieSource? = null,
    val calorieBasis: CalorieBasis? = null,
    val portionSize: String? = null,
    val portionGrams: Double? = null,
    val servingAmount: Double? = null,
    val servingUnit: ServingUnit? = null,
    val kcalMin: Double? = null,
    val kcalMax: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val macroSource: MacroSource? = null,
    val nutritionRecordId: String? = null,
    val nutritionCanonicalName: String? = null,
    val nutritionConfidence: NutritionConfidence? = null,
    val nutritionSource: String? = null,
    val nutritionSourceId: String? = null,
    val nutritionSourceUrl: String? = null,
    val nutritionReferenceOnly: Boolean = false,
    val macroEnergyKcal: Double? = null,
    val macroEnergyDeltaPct: Double? = null,
    val macroEnergyConsistency: MacroEnergyConsistency = MacroEnergyConsistency.NOT_APPLICABLE,
)
