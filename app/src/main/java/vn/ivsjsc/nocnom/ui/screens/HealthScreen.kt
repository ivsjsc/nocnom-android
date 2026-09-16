package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun HealthScreen(state: UserState, contentPadding: PaddingValues) {
    val weight = state.profile.weightKg
    val heightM = state.profile.heightCm?.div(100.0)
    val bmi = if (weight != null && heightM != null && heightM > 0) weight / (heightM * heightM) else null

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
        item { Text("Sức khỏe", style = MaterialTheme.typography.headlineSmall) }
        item { HealthCard("BMI · Asia-Pacific reference", bmi?.let { "%.1f".format(it) } ?: "Chưa đủ dữ liệu") }
        item { HealthCard("Mục tiêu năng lượng", "${state.profile.dailyCalorieTarget} kcal/ngày") }
        item { HealthCard("Mục tiêu", when (state.profile.healthGoal) { "lose" -> "Giảm cân"; "gain" -> "Tăng cân"; else -> "Duy trì" }) }
        item {
            Text(
                "Các chỉ số là công cụ theo dõi/tham khảo. nOcnOm không tự bịa macro cho món chỉ có kcal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HealthCard(title: String, value: String) {
    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
