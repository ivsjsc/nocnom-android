package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.ivsjsc.nocnom.domain.model.UserState

@Composable
fun ProfileScreen(
    state: UserState,
    contentPadding: PaddingValues,
    accountEmail: String?,
    onUpdateTarget: (Int) -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
) {
    var target by remember(state.profile.dailyCalorieTarget) {
        mutableStateOf(state.profile.dailyCalorieTarget.toString())
    }
    val parsed = target.toIntOrNull()
    val valid = parsed != null && parsed in 800..6000

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
        item { Text("Quản lý tài khoản", style = MaterialTheme.typography.headlineSmall) }
        item {
            Card {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        state.profile.fullName.ifBlank { "Người dùng nOcnOm" },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!accountEmail.isNullOrBlank()) {
                        Text(accountEmail, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        if (state.source.name == "FIRESTORE") {
                            "Dữ liệu: Firebase / Firestore"
                        } else {
                            "Đang chờ dữ liệu Firestore của tài khoản"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it.filter(Char::isDigit).take(4) },
                label = { Text("Mục tiêu kcal/ngày") },
                supportingText = { Text("Cho phép 800–6.000 kcal") },
                isError = target.isNotBlank() && !valid,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { parsed?.takeIf { it in 800..6000 }?.let(onUpdateTarget) },
                    enabled = valid,
                ) { Text("Lưu") }
                OutlinedButton(onClick = onRefresh) { Text("Đồng bộ lại") }
            }
        }
        item { HorizontalDivider() }
        item {
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Đăng xuất")
            }
        }
    }
}
