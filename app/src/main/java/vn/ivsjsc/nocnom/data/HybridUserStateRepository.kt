package vn.ivsjsc.nocnom.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import vn.ivsjsc.nocnom.domain.model.*

@Singleton
class HybridUserStateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserStateRepository {
    private val mutableState = MutableStateFlow(SampleData.state)
    override val state: StateFlow<UserState> = mutableState

    override suspend fun refresh() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            mutableState.value = mutableState.value.copy(source = DataSource.DEMO)
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            mutableState.value = mutableState.value.copy(source = DataSource.DEMO)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val profileSnap = db.document(FirestoreContract.profile(uid)).get().await()
        val dishesSnap = db.document(FirestoreContract.dishes(uid)).get().await()
        val categoriesSnap = db.document(FirestoreContract.categories(uid)).get().await()
        val logsSnap = db.document(FirestoreContract.logs(uid)).get().await()

        val profile = parseProfile(profileSnap.data)
        val categories = parseCategories(categoriesSnap.data?.get("items"))
        val dishes = parseDishes(dishesSnap.data?.get("items"))
        val logs = parseLogs(logsSnap.data?.get("items"))

        mutableState.value = mutableState.value.copy(
            profile = profile ?: mutableState.value.profile,
            categories = if (categories.isEmpty()) mutableState.value.categories else categories,
            dishes = if (dishes.isEmpty()) mutableState.value.dishes else dishes,
            logs = logs,
            source = DataSource.FIRESTORE,
        )
    }

    override suspend fun updateDailyCalorieTarget(target: Int) {
        require(target in 800..6000) { "Mục tiêu kcal phải từ 800 đến 6.000." }
        mutableState.value = mutableState.value.copy(
            profile = mutableState.value.profile.copy(dailyCalorieTarget = target),
        )

        if (FirebaseApp.getApps(context).isEmpty()) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .document(FirestoreContract.profile(uid))
            .set(mapOf("dailyCalorieTarget" to target), SetOptions.merge())
            .await()
    }

    private fun parseProfile(data: Map<String, Any>?): UserProfile? {
        if (data == null) return null
        return UserProfile(
            fullName = data["fullName"] as? String ?: "",
            school = data["school"] as? String ?: "",
            faculty = data["faculty"] as? String ?: "",
            studentId = data["studentId"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            photoUrl = data["photoUrl"] as? String ?: "",
            gender = data["gender"] as? String ?: "",
            heightCm = (data["heightCm"] as? Number)?.toDouble(),
            weightKg = (data["weightKg"] as? Number)?.toDouble(),
            activityLevel = data["activityLevel"] as? String ?: "",
            healthGoal = data["healthGoal"] as? String ?: "maintain",
            dailyCalorieTarget = (data["dailyCalorieTarget"] as? Number)?.toInt() ?: 2000,
            macroTargetMode = data["macroTargetMode"] as? String ?: "auto",
            macroProteinPct = (data["macroProteinPct"] as? Number)?.toInt(),
            macroCarbsPct = (data["macroCarbsPct"] as? Number)?.toInt(),
            macroFatPct = (data["macroFatPct"] as? Number)?.toInt(),
        )
    }

    private fun parseCategories(raw: Any?): List<Category> =
        (raw as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val id = map["id"] as? String ?: return@mapNotNull null
            val name = map["name"] as? String ?: return@mapNotNull null
            Category(id, name)
        }.orEmpty()

    private fun parseDishes(raw: Any?): List<Dish> =
        (raw as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val id = map["id"] as? String ?: return@mapNotNull null
            val name = map["name"] as? String ?: return@mapNotNull null
            Dish(
                id = id,
                name = name,
                categoryId = map["categoryId"] as? String ?: "",
                isFavorite = map["isFavorite"] as? Boolean ?: false,
                imageUrl = map["imageUrl"] as? String,
                nutrition = NutritionSnapshot(
                    calories = (map["calories"] as? Number)?.toDouble(),
                    proteinG = (map["proteinG"] as? Number)?.toDouble(),
                    carbsG = (map["carbsG"] as? Number)?.toDouble(),
                    fatG = (map["fatG"] as? Number)?.toDouble(),
                    nutritionRecordId = map["nutritionRecordId"] as? String,
                    nutritionCanonicalName = map["nutritionCanonicalName"] as? String,
                    nutritionSource = map["nutritionSource"] as? String,
                    nutritionSourceUrl = map["nutritionSourceUrl"] as? String,
                    nutritionReferenceOnly = map["nutritionReferenceOnly"] as? Boolean ?: false,
                ),
            )
        }.orEmpty()

    private fun parseLogs(raw: Any?): List<LogEntry> =
        (raw as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val id = map["id"] as? String ?: return@mapNotNull null
            val dishName = map["dishName"] as? String ?: return@mapNotNull null
            LogEntry(
                id = id,
                dishName = dishName,
                vendorName = map["vendorName"] as? String ?: "",
                price = (map["price"] as? Number)?.toLong() ?: 0,
                mealKey = (map["mealKey"] as? String)?.let { runCatching { MealKey.valueOf(it) }.getOrNull() },
                timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0,
                nutrition = NutritionSnapshot(
                    calories = (map["calories"] as? Number)?.toDouble(),
                    proteinG = (map["proteinG"] as? Number)?.toDouble(),
                    carbsG = (map["carbsG"] as? Number)?.toDouble(),
                    fatG = (map["fatG"] as? Number)?.toDouble(),
                    nutritionRecordId = map["nutritionRecordId"] as? String,
                    nutritionCanonicalName = map["nutritionCanonicalName"] as? String,
                ),
            )
        }.orEmpty()
}
