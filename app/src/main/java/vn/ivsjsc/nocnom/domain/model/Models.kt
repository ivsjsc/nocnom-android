package vn.ivsjsc.nocnom.domain.model

import java.time.LocalDate

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
    val key: MealKey,
    val dishId: String? = null,
    val skipped: Boolean = false,
)

data class TimetableDay(
    val date: LocalDate,
    val meals: List<MealSlot> = listOf(
        MealSlot(MealKey.A),
        MealSlot(MealKey.B),
        MealSlot(MealKey.C),
    ),
)

data class UserProfile(
    val fullName: String = "",
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
)

data class UserState(
    val categories: List<Category> = emptyList(),
    val dishes: List<Dish> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
    val timetable: List<TimetableDay> = emptyList(),
    val profile: UserProfile = UserProfile(),
    val source: DataSource = DataSource.DEMO,
)

enum class DataSource { DEMO, FIRESTORE }
