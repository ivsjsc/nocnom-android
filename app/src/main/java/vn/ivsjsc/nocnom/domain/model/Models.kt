package vn.ivsjsc.nocnom.domain.model

data class VendorExtraInfo(
    val id: String,
    val label: String,
    val value: String,
)

data class Vendor(
    val id: String,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val price: Long = 0,
    val link: String? = null,
    val extraInfo: List<VendorExtraInfo> = emptyList(),
)

data class Dish(
    val id: String,
    val name: String,
    val categoryId: String,
    val isFavorite: Boolean = false,
    val imageUrl: String? = null,
    val vendors: List<Vendor> = emptyList(),
    val nutrition: NutritionSnapshot = NutritionSnapshot(),
)

data class Category(
    val id: String,
    val name: String,
)

enum class MealKey { A, B, C }

data class LogEntry(
    val id: String,
    val dishName: String,
    val vendorName: String = "",
    val price: Long = 0,
    val mealKey: MealKey? = null,
    val timestamp: Long = 0,
    val nutrition: NutritionSnapshot = NutritionSnapshot(),
)

data class MealSlot(
    val dishId: String = "",
    val stock: Int = 0,
    val skipped: Boolean = false,
)

data class DayMenu(
    val dayName: String = "",
    val options: Map<MealKey, MealSlot> = mapOf(
        MealKey.A to MealSlot(),
        MealKey.B to MealSlot(),
        MealKey.C to MealSlot(),
    ),
    val suggestionDate: String? = null,
    val suggestionVersion: Int? = null,
)

data class UserProfile(
    val fullName: String = "",
    val dateOfBirth: String = "",
    val school: String = "",
    val faculty: String = "",
    val studentId: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val gender: String = "",
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val activityLevel: String = "",
    val healthGoal: String = "maintain",
    val dailyCalorieTarget: Int = 2000,
    val macroTargetMode: String = "auto",
    val macroProteinPct: Int? = null,
    val macroCarbsPct: Int? = null,
    val macroFatPct: Int? = null,
    val macroProteinG: Double? = null,
    val macroCarbsG: Double? = null,
    val macroFatG: Double? = null,
    val recommendationMode: String = "balanced",
    val mealBudgetVnd: Long? = null,
)

data class UserState(
    val categories: List<Category> = emptyList(),
    val dishes: List<Dish> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val timetable: Map<String, DayMenu> = emptyMap(),
    val profile: UserProfile = UserProfile(),
    val source: DataSource = DataSource.DEMO,
)

enum class DataSource { DEMO, FIRESTORE }
