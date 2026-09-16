package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserState
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
) {
    val zone = ZoneId.of("Asia/Ho_Chi_Minh")
    val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val money = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    val grouped = state.logs.groupBy { log ->
        if (log.timestamp <= 0L) "Chưa xác định ngày"
        else Instant.ofEpochMilli(log.timestamp).atZone(zone).toLocalDate().format(dateFormat)
    }

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
            TextButton(onClick = onBack) { Text("← Quay lại") }
            Text("Lịch sử ăn", style = MaterialTheme.typography.headlineSmall)
        }

        if (grouped.isEmpty()) {
            item {
                Card {
                    Text(
                        "Chưa có bữa ăn nào được ghi nhận.",
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                    )
                }
            }
        } else {
            grouped.forEach { (date, logs) ->
                item(key = "header-$date") {
                    val kcal = logs.sumOf { it.nutrition.calories ?: 0.0 }.toInt()
                    Text("$date · $kcal kcal", style = MaterialTheme.typography.titleMedium)
                }
                items(logs, key = { it.id }) { log ->
                    Card {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(log.dishName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    listOfNotNull(
                                        log.mealKey?.name,
                                        log.vendorName.takeIf(String::isNotBlank),
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Column {
                                Text("${(log.nutrition.calories ?: 0.0).toInt()} kcal")
                                if (log.price > 0) Text("${money.format(log.price)} đ", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
