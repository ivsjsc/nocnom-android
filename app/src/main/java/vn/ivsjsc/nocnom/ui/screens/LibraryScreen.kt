package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun LibraryScreen(state: UserState, contentPadding: PaddingValues) {
    var query by remember { mutableStateOf("") }
    val visible = state.dishes.filter { it.name.contains(query, ignoreCase = true) }

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
        item { Text("Kho món", style = MaterialTheme.typography.headlineSmall) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tìm món") },
                singleLine = true,
            )
        }
        item {
            Text(
                "${visible.size} món · ${state.categories.size} danh mục",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(visible, key = { it.id }) { dish ->
            Card {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AsyncImage(model = dish.imageUrl, contentDescription = dish.name, modifier = Modifier.size(72.dp))
                    Column(Modifier.weight(1f)) {
                        Text(dish.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            dish.nutrition.calories?.let { "${it.toInt()} kcal" } ?: "Chưa có kcal",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        val completeMacro = dish.nutrition.proteinG != null && dish.nutrition.carbsG != null && dish.nutrition.fatG != null
                        Text(
                            if (completeMacro) "Macro đầy đủ" else "Chưa đủ macro",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
