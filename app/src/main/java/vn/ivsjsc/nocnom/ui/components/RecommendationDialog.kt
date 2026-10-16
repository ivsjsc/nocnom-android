package vn.ivsjsc.nocnom.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun RecommendationDialog(
    initialMode: String,
    initialBudget: Long?,
    onDismiss: () -> Unit,
    onSave: (mode: String, budget: Long?) -> Unit,
) {
    var mode by remember { mutableStateOf(initialMode) }
    var budgetText by remember { mutableStateOf(initialBudget?.toString().orEmpty()) }
    val parsedBudget = budgetText.filter(Char::isDigit).toLongOrNull()
    val isBudgetValid = budgetText.isBlank() || (parsedBudget != null && parsedBudget in 5_000L..2_000_000L)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Gợi ý món mỗi ngày",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Cùng cấu hình với Web App. Chế độ này được dùng khi xếp hạng món trong “Đổi món”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RecommendationOption(
                        label = "Cân bằng",
                        description = "Cân đối kcal, sở thích, giá và độ đa dạng",
                        selected = mode == "balanced",
                        onSelect = { mode = "balanced" },
                    )
                    RecommendationOption(
                        label = "Tiết kiệm",
                        description = "Ưu tiên món phù hợp ngân sách sinh viên",
                        selected = mode == "budget",
                        onSelect = { mode = "budget" },
                    )
                    RecommendationOption(
                        label = "Đổi vị",
                        description = "Giảm lặp món và nhóm món gần đây",
                        selected = mode == "variety",
                        onSelect = { mode = "variety" },
                    )
                    RecommendationOption(
                        label = "Nhanh",
                        description = "Ưu tiên món có thông tin quán/link thuận tiện",
                        selected = mode == "quick",
                        onSelect = { mode = "quick" },
                    )
                }

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter(Char::isDigit).take(7) },
                    label = { Text("Ngân sách/bữa (VNĐ)") },
                    supportingText = { Text("Để trống nếu không giới hạn (5.000–2.000.000đ)") },
                    isError = budgetText.isNotBlank() && !isBudgetValid,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Hủy")
                    }

                    Button(
                        onClick = {
                            onSave(mode, parsedBudget)
                            onDismiss()
                        },
                        enabled = isBudgetValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Lưu cài đặt")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationOption(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RadioButton(selected = selected, onClick = onSelect)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
