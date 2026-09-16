package vn.ivsjsc.nocnom.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import vn.ivsjsc.nocnom.domain.meal.DailySuggestions
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

@Singleton
class HybridUserStateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserStateRepository {
    private val mutableState = MutableStateFlow(UserState())
    override val state: StateFlow<UserState> = mutableState

    private var listenerUid: String? = null
    private val listeners = mutableListOf<ListenerRegistration>()

    override suspend fun refresh() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            stopRealtimeSync()
            mutableState.value = UserState(source = DataSource.DEMO)
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            stopRealtimeSync()
            mutableState.value = UserState(source = DataSource.DEMO)
            return
        }

        val db = FirebaseFirestore.getInstance()
        val profileSnap = db.document(FirestoreContract.profile(uid)).get().await()
        val timetableSnap = db.document(FirestoreContract.timetable(uid)).get().await()
        val dishesSnap = db.document(FirestoreContract.dishes(uid)).get().await()
        val categoriesSnap = db.document(FirestoreContract.categories(uid)).get().await()
        val logsSnap = db.document(FirestoreContract.logs(uid)).get().await()

        val loaded = UserState(
            profile = parseProfile(profileSnap.data) ?: UserProfile(),
            timetable = parseTimetable(timetableSnap.data?.get("value")),
            categories = parseCategories(categoriesSnap.data?.get("items")),
            dishes = parseDishes(dishesSnap.data?.get("items")),
            logs = parseLogs(logsSnap.data?.get("items")),
            source = DataSource.FIRESTORE,
        )

        val refreshed = DailySuggestions.refresh(loaded)
        mutableState.value = refreshed.state.copy(source = DataSource.FIRESTORE)
        if (refreshed.changed) {
            persistTimetable(uid, refreshed.state.timetable)
        }
        ensureRealtimeSync(uid)
    }

    override suspend fun updateDailyCalorieTarget(target: Int) {
        require(target in 800..6000) { "Mục tiêu kcal phải từ 800 đến 6.000." }
        mutableState.value = mutableState.value.copy(
            profile = mutableState.value.profile.copy(dailyCalorieTarget = target),
        )

        val uid = currentUid() ?: return
        FirebaseFirestore.getInstance()
            .document(FirestoreContract.profile(uid))
            .set(
                mapOf(
                    "dailyCalorieTarget" to target,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    override suspend fun updateMealSlot(
        mealKey: MealKey,
        dishId: String?,
        skipped: Boolean,
    ) {
        val uid = currentUid() ?: return
        val dayKey = VietnamTime.dayKey()
        val dateKey = VietnamTime.dateKey()
        val current = mutableState.value
        val menu = current.timetable[dayKey] ?: DayMenu(dayName = vietnameseDayName(dayKey))
        val slot = menu.options[mealKey] ?: MealSlot()
        val nextSlot = slot.copy(
            dishId = dishId ?: slot.dishId,
            skipped = skipped,
        )
        val nextMenu = menu.copy(
            options = menu.options + (mealKey to nextSlot),
            suggestionDate = dateKey,
            suggestionVersion = DailySuggestions.VERSION,
        )
        val nextTimetable = current.timetable + (dayKey to nextMenu)
        mutableState.value = current.copy(timetable = nextTimetable)
        persistTimetable(uid, nextTimetable)
    }

    override suspend fun markMealEaten(mealKey: MealKey) {
        val uid = currentUid() ?: return
        val current = mutableState.value
        val dayKey = VietnamTime.dayKey()
        val dateKey = VietnamTime.dateKey()
        val menu = current.timetable[dayKey] ?: return
        val slot = menu.options[mealKey] ?: return
        require(!slot.skipped) { "Bữa này đang được đánh dấu không ăn." }
        val dish = current.dishes.firstOrNull { it.id == slot.dishId }
            ?: throw IllegalStateException("Không tìm thấy món đã chọn.")
        val vendor = dish.vendors.minByOrNull { vendor ->
            vendor.price.takeIf { it > 0 } ?: Long.MAX_VALUE
        }
        val now = System.currentTimeMillis()
        val id = "android-$dateKey-${mealKey.name}"
        val entry = LogEntry(
            id = id,
            dishName = dish.name,
            vendorName = vendor?.name.orEmpty(),
            price = vendor?.price ?: 0,
            mealKey = mealKey,
            timestamp = now,
            nutrition = dish.nutrition,
        )
        val retained = current.logs.filterNot { log ->
            log.id == id ||
                (log.mealKey == mealKey && VietnamTime.dateKey(log.timestamp) == dateKey)
        }
        mutableState.value = current.copy(logs = retained + entry)
        persistLogWithoutLosingUnknownFields(uid, entry, dateKey, mealKey)
    }

    override suspend fun updateRecommendationPreferences(
        mode: String,
        mealBudgetVnd: Long?,
    ) {
        require(mode in setOf("balanced", "budget", "variety", "quick")) {
            "Chế độ gợi ý không hợp lệ."
        }
        require(mealBudgetVnd == null || mealBudgetVnd in 5_000L..2_000_000L) {
            "Ngân sách/bữa phải từ 5.000đ đến 2.000.000đ."
        }

        val nextProfile = mutableState.value.profile.copy(
            recommendationMode = mode,
            mealBudgetVnd = mealBudgetVnd,
        )
        mutableState.value = mutableState.value.copy(profile = nextProfile)

        val uid = currentUid() ?: return
        FirebaseFirestore.getInstance()
            .document(FirestoreContract.profile(uid))
            .set(
                mapOf(
                    "recommendationMode" to mode,
                    "mealBudgetVnd" to (mealBudgetVnd ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    override suspend fun updateProfile(profile: UserProfile) {
        val uid = currentUid() ?: return
        mutableState.value = mutableState.value.copy(profile = profile)

        FirebaseFirestore.getInstance()
            .document(FirestoreContract.profile(uid))
            .set(
                mapOf(
                    "fullName" to profile.fullName,
                    "dateOfBirth" to profile.dateOfBirth,
                    "school" to profile.school,
                    "faculty" to profile.faculty,
                    "studentId" to profile.studentId,
                    "phone" to profile.phone,
                    "photoUrl" to profile.photoUrl,
                    "gender" to profile.gender,
                    "heightCm" to (profile.heightCm ?: ""),
                    "weightKg" to (profile.weightKg ?: ""),
                    "activityLevel" to profile.activityLevel,
                    "healthGoal" to profile.healthGoal,
                    "dailyCalorieTarget" to profile.dailyCalorieTarget,
                    "macroTargetMode" to profile.macroTargetMode,
                    "macroProteinPct" to (profile.macroProteinPct ?: ""),
                    "macroCarbsPct" to (profile.macroCarbsPct ?: ""),
                    "macroFatPct" to (profile.macroFatPct ?: ""),
                    "macroProteinG" to (profile.macroProteinG ?: ""),
                    "macroCarbsG" to (profile.macroCarbsG ?: ""),
                    "macroFatG" to (profile.macroFatG ?: ""),
                    "recommendationMode" to profile.recommendationMode,
                    "mealBudgetVnd" to (profile.mealBudgetVnd ?: ""),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    private fun currentUid(): String? {
        if (FirebaseApp.getApps(context).isEmpty()) return null
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private suspend fun persistTimetable(
        uid: String,
        timetable: Map<String, DayMenu>,
    ) {
        FirebaseFirestore.getInstance()
            .document(FirestoreContract.timetable(uid))
            .set(
                mapOf(
                    "value" to serializeTimetable(timetable),
                    "schemaVersion" to FirestoreContract.USER_STATE_SCHEMA_VERSION,
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
            .await()
    }

    private suspend fun persistLogWithoutLosingUnknownFields(
        uid: String,
        entry: LogEntry,
        dateKey: String,
        mealKey: MealKey,
    ) {
        val ref = FirebaseFirestore.getInstance().document(FirestoreContract.logs(uid))
        val snapshot = ref.get().await()
        val existing = (snapshot.data?.get("items") as? List<*>)
            .orEmpty()
            .mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                LinkedHashMap<String, Any?>().apply {
                    map.forEach { (key, value) ->
                        if (key is String) put(key, value)
                    }
                }
            }
            .filterNot { raw ->
                val rawId = raw["id"] as? String
                val rawMeal = raw["mealKey"] as? String
                val rawTimestamp = timestampMillis(raw["timestamp"])
                rawId == entry.id ||
                    (
                        rawMeal == mealKey.name &&
                            rawTimestamp != null &&
                            VietnamTime.dateKey(rawTimestamp) == dateKey
                        )
            }

        ref.set(
            mapOf(
                "items" to (existing + serializeLog(entry)),
                "schemaVersion" to FirestoreContract.USER_STATE_SCHEMA_VERSION,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
            SetOptions.merge(),
        ).await()
    }

    private fun ensureRealtimeSync(uid: String) {
        if (listenerUid == uid && listeners.isNotEmpty()) return
        stopRealtimeSync()
        listenerUid = uid
        val db = FirebaseFirestore.getInstance()

        listeners += db.document(FirestoreContract.profile(uid))
            .addSnapshotListener { snapshot, _ ->
                if (listenerUid != uid) return@addSnapshotListener
                parseProfile(snapshot?.data)?.let { profile ->
                    mutableState.value = mutableState.value.copy(
                        profile = profile,
                        source = DataSource.FIRESTORE,
                    )
                }
            }

        listeners += db.document(FirestoreContract.timetable(uid))
            .addSnapshotListener { snapshot, _ ->
                if (listenerUid != uid) return@addSnapshotListener
                val value = snapshot?.data?.get("value") ?: return@addSnapshotListener
                mutableState.value = mutableState.value.copy(
                    timetable = parseTimetable(value),
                    source = DataSource.FIRESTORE,
                )
            }

        listeners += db.document(FirestoreContract.dishes(uid))
            .addSnapshotListener { snapshot, _ ->
                if (listenerUid != uid) return@addSnapshotListener
                val items = snapshot?.data?.get("items") ?: return@addSnapshotListener
                mutableState.value = mutableState.value.copy(
                    dishes = parseDishes(items),
                    source = DataSource.FIRESTORE,
                )
            }

        listeners += db.document(FirestoreContract.categories(uid))
            .addSnapshotListener { snapshot, _ ->
                if (listenerUid != uid) return@addSnapshotListener
                val items = snapshot?.data?.get("items") ?: return@addSnapshotListener
                mutableState.value = mutableState.value.copy(
                    categories = parseCategories(items),
                    source = DataSource.FIRESTORE,
                )
            }

        listeners += db.document(FirestoreContract.logs(uid))
            .addSnapshotListener { snapshot, _ ->
                if (listenerUid != uid) return@addSnapshotListener
                val items = snapshot?.data?.get("items") ?: return@addSnapshotListener
                mutableState.value = mutableState.value.copy(
                    logs = parseLogs(items),
                    source = DataSource.FIRESTORE,
                )
            }
    }

    private fun stopRealtimeSync() {
        listeners.forEach(ListenerRegistration::remove)
        listeners.clear()
        listenerUid = null
    }

    private fun parseProfile(data: Map<String, Any>?): UserProfile? {
        if (data == null) return null
        return UserProfile(
            fullName = data["fullName"] as? String ?: "",
            dateOfBirth = data["dateOfBirth"] as? String ?: "",
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
            macroProteinG = (data["macroProteinG"] as? Number)?.toDouble(),
            macroCarbsG = (data["macroCarbsG"] as? Number)?.toDouble(),
            macroFatG = (data["macroFatG"] as? Number)?.toDouble(),
            recommendationMode = data["recommendationMode"] as? String ?: "balanced",
            mealBudgetVnd = (data["mealBudgetVnd"] as? Number)?.toLong(),
        )
    }

    private fun parseTimetable(raw: Any?): Map<String, DayMenu> {
        val root = raw as? Map<*, *> ?: return emptyMap()
        return root.mapNotNull { (dayKeyRaw, valueRaw) ->
            val dayKey = dayKeyRaw as? String ?: return@mapNotNull null
            val map = valueRaw as? Map<*, *> ?: return@mapNotNull null
            val optionsRaw = map["options"] as? Map<*, *> ?: emptyMap<Any, Any>()
            val options = MealKey.entries.associateWith { mealKey ->
                val slot = optionsRaw[mealKey.name] as? Map<*, *>
                MealSlot(
                    dishId = slot?.get("dishId") as? String ?: "",
                    stock = (slot?.get("stock") as? Number)?.toInt() ?: 0,
                    skipped = slot?.get("skipped") as? Boolean ?: false,
                )
            }
            dayKey to DayMenu(
                dayName = map["dayName"] as? String ?: vietnameseDayName(dayKey),
                options = options,
                suggestionDate = map["suggestionDate"] as? String,
                suggestionVersion = (map["suggestionVersion"] as? Number)?.toInt(),
            )
        }.toMap()
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
                vendors = parseVendors(map["vendors"]),
                nutrition = parseNutrition(map),
            )
        }.orEmpty()

    private fun parseVendors(raw: Any?): List<Vendor> =
        (raw as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val id = map["id"] as? String ?: return@mapNotNull null
            Vendor(
                id = id,
                name = map["name"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                address = map["address"] as? String ?: "",
                price = (map["price"] as? Number)?.toLong() ?: 0,
                link = map["link"] as? String,
                extraInfo = (map["extraInfo"] as? List<*>)?.mapNotNull { extra ->
                    val extraMap = extra as? Map<*, *> ?: return@mapNotNull null
                    val extraId = extraMap["id"] as? String ?: return@mapNotNull null
                    VendorExtraInfo(
                        id = extraId,
                        label = extraMap["label"] as? String ?: "",
                        value = extraMap["value"] as? String ?: "",
                    )
                }.orEmpty(),
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
                mealKey = (map["mealKey"] as? String)?.let {
                    runCatching { MealKey.valueOf(it) }.getOrNull()
                },
                timestamp = timestampMillis(map["timestamp"]) ?: 0,
                nutrition = parseNutrition(map),
            )
        }.orEmpty()

    private fun parseNutrition(map: Map<*, *>): NutritionSnapshot =
        NutritionSnapshot(
            calories = (map["calories"] as? Number)?.toDouble(),
            proteinG = (map["proteinG"] as? Number)?.toDouble(),
            carbsG = (map["carbsG"] as? Number)?.toDouble(),
            fatG = (map["fatG"] as? Number)?.toDouble(),
            nutritionRecordId = map["nutritionRecordId"] as? String,
            nutritionCanonicalName = map["nutritionCanonicalName"] as? String,
            nutritionSource = map["nutritionSource"] as? String,
            nutritionSourceId = map["nutritionSourceId"] as? String,
            nutritionSourceUrl = map["nutritionSourceUrl"] as? String,
            nutritionReferenceOnly = map["nutritionReferenceOnly"] as? Boolean ?: false,
            macroEnergyKcal = (map["macroEnergyKcal"] as? Number)?.toDouble(),
            macroEnergyDeltaPct = (map["macroEnergyDeltaPct"] as? Number)?.toDouble(),
        )

    private fun serializeTimetable(
        timetable: Map<String, DayMenu>,
    ): Map<String, Any?> =
        timetable.mapValues { (_, menu) ->
            mapOf(
                "dayName" to menu.dayName,
                "options" to menu.options.mapKeys { it.key.name }.mapValues { (_, slot) ->
                    mapOf(
                        "dishId" to slot.dishId,
                        "stock" to slot.stock,
                        "skipped" to slot.skipped,
                    )
                },
                "suggestionDate" to menu.suggestionDate,
                "suggestionVersion" to menu.suggestionVersion,
            )
        }

    private fun serializeLog(entry: LogEntry): Map<String, Any?> =
        linkedMapOf(
            "id" to entry.id,
            "dishName" to entry.dishName,
            "vendorName" to entry.vendorName,
            "price" to entry.price,
            "mealKey" to entry.mealKey?.name,
            "timestamp" to entry.timestamp,
            "calories" to entry.nutrition.calories,
            "proteinG" to entry.nutrition.proteinG,
            "carbsG" to entry.nutrition.carbsG,
            "fatG" to entry.nutrition.fatG,
            "nutritionRecordId" to entry.nutrition.nutritionRecordId,
            "nutritionCanonicalName" to entry.nutrition.nutritionCanonicalName,
            "nutritionSource" to entry.nutrition.nutritionSource,
            "nutritionSourceId" to entry.nutrition.nutritionSourceId,
            "nutritionSourceUrl" to entry.nutrition.nutritionSourceUrl,
            "nutritionReferenceOnly" to entry.nutrition.nutritionReferenceOnly,
            "macroEnergyKcal" to entry.nutrition.macroEnergyKcal,
            "macroEnergyDeltaPct" to entry.nutrition.macroEnergyDeltaPct,
        )

    private fun timestampMillis(value: Any?): Long? = when (value) {
        is Number -> value.toLong()
        is Timestamp -> value.toDate().time
        else -> null
    }

    private fun vietnameseDayName(dayKey: String): String = when (dayKey) {
        "mon" -> "Thứ 2"
        "tue" -> "Thứ 3"
        "wed" -> "Thứ 4"
        "thu" -> "Thứ 5"
        "fri" -> "Thứ 6"
        "sat" -> "Thứ 7"
        "sun" -> "Chủ nhật"
        else -> ""
    }
}
