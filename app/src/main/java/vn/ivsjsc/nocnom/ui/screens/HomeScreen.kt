package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun HomeScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onOpenHistory: () -> Unit,
) {
    val consumed = state.logs.sumOf { it.nutrition.calories ?: 0.0 }.toInt()
    val target = state.profile.dailyCalorieTarget
    val progress = if (target <= 0) 0f else (consumed.toFloat() / target).coerceIn(0f, 1f)
    val protein = state.logs.sumOf { it.nutrition.proteinG ?: 0.0 }
    val carbs = state.logs.sumOf { it.nutrition.carbsG ?: 0.0 }
    val fat = state.logs.sumOf { it.nutrition.fatG ?: 0.0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("nOcnOm", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Ăn đủ, hiểu đúng, theo dõi vừa đủ.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Card {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric("Đã tiêu thụ", "$consumed kcal")
                        Metric("Mục tiêu ngày", "$target kcal")
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        item {
            Text("Macro hôm nay", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroCard("Protein", protein, Modifier.weight(1f))
                MacroCard("Carb", carbs, Modifier.weight(1f))
                MacroCard("Fat", fat, Modifier.weight(1f))
            }
            Text(
                "Chỉ cộng macro từ món có dữ liệu macro thực tế; không suy ngược macro từ kcal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Text("Xem lịch sử ăn")
            }
        }
        item {
            AssistChip(
                onClick = {},
                label = { Text(if (state.source.name == "FIRESTORE") "Đang đồng bộ Cloud" else "Chế độ demo — chưa kết nối Firebase Android") },
            )
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun MacroCard(label: String, grams: Double, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("%.1f g".format(grams), style = MaterialTheme.typography.titleMedium)
        }
    }
}
