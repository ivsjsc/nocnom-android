package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import vn.ivsjsc.nocnom.domain.meal.RecommendationEngine
import vn.ivsjsc.nocnom.domain.model.*
import vn.ivsjsc.nocnom.domain.time.VietnamTime

@Composable
fun PlanScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onUpdateMeal: (MealKey, String?, Boolean) -> Unit,
    onMarkMealEaten: (MealKey) -> Unit,
) {
    val today = VietnamTime.dateKey()
    val dayKey = VietnamTime.dayKey()
    val menu = state.timetable[dayKey]
    val eatenMeals = state.logs
        .filter { VietnamTime.dateKey(it.timestamp) == today }
        .mapNotNull { it.mealKey }
        .toSet()

    var selectingMeal by remember { mutableStateOf<MealKey?>(null) }
    var query by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Kế hoạch ăn hôm nay", style = MaterialTheme.typography.headlineSmall)
            Text(
                menu?.dayName.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (menu == null) {
            item {
                Card {
                    Text(
                        "Chưa có thực đơn đồng bộ cho hôm nay. Hãy dùng Đồng bộ lại trong Tài khoản.",
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                    )
                }
            }
        } else {
            listOf(MealKey.A, MealKey.B, MealKey.C).forEach { key ->
                item(key = key.name) {
                    val slot = menu.options[key] ?: MealSlot()
                    val dish = state.dishes.firstOrNull { it.id == slot.dishId }
                    MealSlotCard(
                        mealKey = key,
                        slot = slot,
                        dish = dish,
                        eaten = key in eatenMeals,
                        onChooseDish = {
                            query = ""
                            selectingMeal = key
                        },
                        onToggleSkip = {
                            onUpdateMeal(key, slot.dishId.takeIf(String::isNotBlank), !slot.skipped)
                        },
                        onMarkEaten = { onMarkMealEaten(key) },
                    )
                }
            }

            item {
                Text(
                    "Món được làm mới theo ngày Việt Nam. Lựa chọn “không ăn” và món bạn tự đổi được giữ nguyên trong ngày.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    selectingMeal?.let { mealKey ->
        val recommendations = RecommendationEngine.rank(state, mealKey)
        val visible = recommendations
            .filter { recommendation ->
                query.isBlank() ||
                    recommendation.dish.name.contains(query, ignoreCase = true)
            }
            .take(30)
        val money = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

        AlertDialog(
            onDismissRequest = {
                selectingMeal = null
                query = ""
            },
            title = {
                Text("Đổi ${mealLabel(mealKey).lowercase()}")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tìm món") },
                        singleLine = true,
                    )

                    if (recommendations.isNotEmpty()) {
                        Text(
                            "Gợi ý theo ${recommendationModeLabel(state.profile.recommendationMode)} · mục tiêu khoảng ${recommendations.first().targetCalories} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(visible, key = { it.dish.id }) { recommendation ->
                            OutlinedButton(
                                onClick = {
                                    onUpdateMeal(mealKey, recommendation.dish.id, false)
                                    selectingMeal = null
                                    query = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(
                                        recommendation.dish.name,
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    val kcal = recommendation.dish.nutrition.calories
                                        ?.toInt()
                                        ?.let { "$it kcal" }
                                        ?: "Chưa có kcal"
                                    val price = recommendation.minPrice
                                        ?.takeIf { it > 0 }
                                        ?.let { "${money.format(it)} đ" }
                                    Text(
                                        listOfNotNull(kcal, price).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectingMeal = null
                        query = ""
                    },
                ) {
                    Text("Đóng")
                }
            },
        )
    }
}

@Composable
private fun MealSlotCard(
    mealKey: MealKey,
    slot: MealSlot,
    dish: Dish?,
    eaten: Boolean,
    onChooseDish: () -> Unit,
    onToggleSkip: () -> Unit,
    onMarkEaten: () -> Unit,
) {
    Card {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(mealLabel(mealKey), style = MaterialTheme.typography.titleMedium)
                    Text(
                        mealTime(mealKey),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (eaten) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Đã ăn") },
                    )
                }
            }

            Text(
                when {
                    slot.skipped -> if (mealKey == MealKey.B) {
                        "Không ăn buổi trưa"
                    } else {
                        "Không ăn bữa này"
                    }
                    dish != null -> dish.name
                    else -> "Chưa chọn món"
                },
                style = MaterialTheme.typography.titleSmall,
            )

            if (!slot.skipped && dish != null) {
                Text(
                    dish.nutrition.calories?.let { "${it.toInt()} kcal" } ?: "Chưa có kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onChooseDish,
                    enabled = !eaten,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Đổi món")
                }
                OutlinedButton(
                    onClick = onToggleSkip,
                    enabled = !eaten,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (slot.skipped) "Ăn bữa này" else "Bỏ bữa")
                }
            }

            Button(
                onClick = onMarkEaten,
                enabled = !slot.skipped && dish != null && !eaten,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (eaten) "Đã ghi nhận" else "Đánh dấu đã ăn")
            }
        }
    }
}

private fun mealLabel(mealKey: MealKey): String = when (mealKey) {
    MealKey.A -> "Bữa sáng"
    MealKey.B -> "Bữa trưa"
    MealKey.C -> "Bữa tối"
}

private fun mealTime(mealKey: MealKey): String = when (mealKey) {
    MealKey.A -> "08:00"
    MealKey.B -> "12:00"
    MealKey.C -> "18:00"
}

private fun recommendationModeLabel(mode: String): String = when (mode) {
    "budget" -> "Tiết kiệm"
    "variety" -> "Đổi vị"
    "quick" -> "Nhanh"
    else -> "Cân bằng"
}
