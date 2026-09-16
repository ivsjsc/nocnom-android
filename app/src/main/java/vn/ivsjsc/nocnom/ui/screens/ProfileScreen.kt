package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserProfile
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun ProfileScreen(
    state: UserState,
    contentPadding: PaddingValues,
    accountEmail: String?,
    onSaveProfile: (UserProfile) -> Unit,
    onUpdateRecommendation: (String, Long?) -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val profile = state.profile
    var fullName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var dateOfBirth by remember(profile.dateOfBirth) { mutableStateOf(profile.dateOfBirth) }
    var school by remember(profile.school) { mutableStateOf(profile.school) }
    var faculty by remember(profile.faculty) { mutableStateOf(profile.faculty) }
    var studentId by remember(profile.studentId) { mutableStateOf(profile.studentId) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var heightCm by remember(profile.heightCm) {
        mutableStateOf(profile.heightCm?.let { formatNumber(it) }.orEmpty())
    }
    var weightKg by remember(profile.weightKg) {
        mutableStateOf(profile.weightKg?.let { formatNumber(it) }.orEmpty())
    }
    var target by remember(profile.dailyCalorieTarget) {
        mutableStateOf(profile.dailyCalorieTarget.toString())
    }
    var recommendationMode by remember(profile.recommendationMode) {
        mutableStateOf(profile.recommendationMode)
    }
    var budget by remember(profile.mealBudgetVnd) {
        mutableStateOf(profile.mealBudgetVnd?.toString().orEmpty())
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val parsedTarget = target.toIntOrNull()
    val targetValid = parsedTarget != null && parsedTarget in 800..6000
    val parsedHeight = heightCm.replace(',', '.').toDoubleOrNull()
    val parsedWeight = weightKg.replace(',', '.').toDoubleOrNull()
    val heightValid = heightCm.isBlank() || (parsedHeight != null && parsedHeight in 80.0..240.0)
    val weightValid = weightKg.isBlank() || (parsedWeight != null && parsedWeight in 25.0..220.0)
    val parsedBudget = budget.filter(Char::isDigit).toLongOrNull()
    val budgetValid = budget.isBlank() || (parsedBudget != null && parsedBudget in 5_000L..2_000_000L)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa tài khoản nOcnOm?") },
            text = {
                Text(
                    "Tài khoản Firebase và dữ liệu nOcnOm của bạn sẽ bị xóa khỏi Firestore. " +
                        "Hành động này không thể hoàn tác. Nếu phiên đăng nhập đã quá cũ, " +
                        "hệ thống có thể yêu cầu đăng nhập lại trước khi xóa.",
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = contentPadding.calculateTopPadding() + 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Quản lý tài khoản", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        profile.fullName.ifBlank { "Người dùng nOcnOm" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!accountEmail.isNullOrBlank()) {
                        Text(
                            accountEmail,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        if (state.source.name == "FIRESTORE") {
                            "Dữ liệu: Firebase / Firestore · realtime"
                        } else {
                            "Chưa kết nối dữ liệu Firestore"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text("Thông tin cá nhân", style = MaterialTheme.typography.titleMedium)
        }
        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it.take(120) },
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = dateOfBirth,
                onValueChange = { dateOfBirth = it.take(10) },
                label = { Text("Ngày sinh (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = school,
                onValueChange = { school = it.take(160) },
                label = { Text("Trường") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = faculty,
                onValueChange = { faculty = it.take(160) },
                label = { Text("Khoa / đơn vị") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = studentId,
                onValueChange = { studentId = it.take(60) },
                label = { Text("Mã sinh viên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.take(30) },
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        item {
            Text("Sức khỏe & mục tiêu", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it.take(6) },
                    label = { Text("Chiều cao cm") },
                    isError = !heightValid,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it.take(6) },
                    label = { Text("Cân nặng kg") },
                    isError = !weightValid,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        }
        item {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter(Char::isDigit).take(4) },
                label = { Text("Mục tiêu kcal/ngày") },
                supportingText = { Text("Cho phép 800–6.000 kcal") },
                isError = target.isNotBlank() && !targetValid,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Button(
                onClick = {
                    onSaveProfile(
                        profile.copy(
                            fullName = fullName.trim(),
                            dateOfBirth = dateOfBirth.trim(),
                            school = school.trim(),
                            faculty = faculty.trim(),
                            studentId = studentId.trim(),
                            phone = phone.trim(),
                            heightCm = parsedHeight,
                            weightKg = parsedWeight,
                            dailyCalorieTarget = parsedTarget ?: profile.dailyCalorieTarget,
                        ),
                    )
                },
                enabled = targetValid && heightValid && weightValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lưu hồ sơ")
            }
        }

        item { HorizontalDivider() }

        item {
            Text("Gợi ý món mỗi ngày", style = MaterialTheme.typography.titleMedium)
            Text(
                "Cùng cấu hình với Web App. Chế độ này được dùng khi xếp hạng món trong “Đổi món”.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                RecommendationModeRow(
                    label = "Cân bằng",
                    hint = "Cân đối kcal, sở thích, giá và độ đa dạng",
                    selected = recommendationMode == "balanced",
                    onClick = { recommendationMode = "balanced" },
                )
                RecommendationModeRow(
                    label = "Tiết kiệm",
                    hint = "Ưu tiên món phù hợp ngân sách",
                    selected = recommendationMode == "budget",
                    onClick = { recommendationMode = "budget" },
                )
                RecommendationModeRow(
                    label = "Đổi vị",
                    hint = "Giảm lặp món và nhóm món gần đây",
                    selected = recommendationMode == "variety",
                    onClick = { recommendationMode = "variety" },
                )
                RecommendationModeRow(
                    label = "Nhanh",
                    hint = "Ưu tiên món có thông tin quán/link thuận tiện",
                    selected = recommendationMode == "quick",
                    onClick = { recommendationMode = "quick" },
                )
            }
        }

        item {
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter(Char::isDigit).take(7) },
                label = { Text("Ngân sách/bữa (đ)") },
                supportingText = {
                    Text("Để trống nếu không giới hạn · 5.000–2.000.000đ")
                },
                isError = budget.isNotBlank() && !budgetValid,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Button(
                onClick = {
                    onUpdateRecommendation(
                        recommendationMode,
                        parsedBudget,
                    )
                },
                enabled = budgetValid,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lưu cài đặt gợi ý")
            }
        }

        item { HorizontalDivider() }

        item {
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Đồng bộ lại dữ liệu")
            }
        }
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Đăng xuất")
            }
        }
        item {
            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Xóa tài khoản và dữ liệu")
            }
        }
    }
}

@Composable
private fun RecommendationModeRow(
    label: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp, bottom = 8.dp),
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(
                hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
