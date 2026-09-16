package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun PlanScreen(state: UserState, contentPadding: PaddingValues) {
    var skipLunch by remember { mutableStateOf(false) }
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
        item { Text("Kế hoạch ăn", style = MaterialTheme.typography.headlineSmall) }
        item { MealSlotCard("Bữa sáng", "08:00", state.dishes.firstOrNull()?.name ?: "Chưa chọn") }
        item {
            Card {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Bữa trưa", style = MaterialTheme.typography.titleMedium)
                        Text(if (skipLunch) "Không ăn buổi trưa" else "12:00 · Chưa chọn", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = skipLunch, onCheckedChange = { skipLunch = it })
                }
            }
        }
        item { MealSlotCard("Bữa tối", "18:00", "Chưa chọn") }
    }
}

@Composable
private fun MealSlotCard(title: String, time: String, dish: String) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text("$time · $dish", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
