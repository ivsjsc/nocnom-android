package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale
import vn.ivsjsc.nocnom.domain.model.UserState
import vn.ivsjsc.nocnom.domain.time.VietnamTime
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

@Composable
fun HealthScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onOpenProfile: () -> Unit,
) {
    val weight = state.profile.weightKg
    val heightCm = state.profile.heightCm
    val heightM = heightCm?.div(100.0)
    val bmi = if (weight != null && heightM != null && heightM > 0) weight / (heightM * heightM) else null

    // Ideal weight range for Asia-Pacific: BMI 18.5 to 22.9
    val minIdealWeight = if (heightM != null && heightM > 0) 18.5 * heightM * heightM else null
    val maxIdealWeight = if (heightM != null && heightM > 0) 22.9 * heightM * heightM else null

    val bmiCategory = when {
        bmi == null -> "Chưa đủ dữ liệu"
        bmi < 18.5 -> "Thiếu cân (Gầy)"
        bmi < 23.0 -> "Trong khoảng tham khảo"
        bmi < 25.0 -> "Tiền béo phì"
        else -> "Béo phì"
    }

    val bmiTagColor = when {
        bmi == null -> MaterialTheme.colorScheme.onSurfaceVariant
        bmi in 18.5..22.9 -> Color(0xFF059669) // Green
        bmi < 18.5 -> Color(0xFFD97706) // Yellow
        else -> Color(0xFFDC2626) // Red
    }

    val today = VietnamTime.dateKey()
    val todayLogs = state.logs.filter { VietnamTime.dateKey(it.timestamp) == today }
    val consumed = todayLogs.sumOf { it.nutrition.calories ?: 0.0 }.toInt()
    val target = state.profile.dailyCalorieTarget
    val progress = if (target <= 0) 0f else (consumed.toFloat() / target).coerceIn(0f, 1f)
    val progressPct = (progress * 100).toInt()
    val remaining = (target - consumed).coerceAtLeast(0)

    val protein = todayLogs.sumOf { it.nutrition.proteinG ?: 0.0 }
    val carbs = todayLogs.sumOf { it.nutrition.carbsG ?: 0.0 }
    val fat = todayLogs.sumOf { it.nutrition.fatG ?: 0.0 }

    val money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Card 1: Health & Nutrition Center Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF3B82F6)),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // Status Tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bmiTagColor.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, bmiTagColor.copy(alpha = 0.3f)),
                        ) {
                            Text(
                                text = bmiCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = bmiTagColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "TRUNG TÂM SỨC KHỎE & DINH DƯỠNG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NocnomPrimary,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = "Sức khỏe nOcnOm",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Thể trạng, cân bằng Calo & Nhật ký dinh dưỡng sinh viên",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenProfile,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hồ sơ sức khỏe ↗",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }

        // Card 2: BMI Physical Index Card
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = NocnomPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "Chỉ số thể trạng",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "BMI · Asia-Pacific reference",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bmiTagColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = bmiCategory,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = bmiTagColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    // BMI Value & Height/Weight Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = bmi?.let { "%.1f".format(it) } ?: "—",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            val wText = weight?.let { "%.0f kg".format(it) } ?: "-- kg"
                            val hText = heightCm?.let { "%.0f cm".format(it) } ?: "-- cm"
                            Text(
                                text = "$wText · $hText",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }

                    // Segmented BMI Range Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                        ) {
                            Box(modifier = Modifier.weight(18.5f).fillMaxHeight().background(Color(0xFFEAB308))) // Underweight
                            Box(modifier = Modifier.weight(4.4f).fillMaxHeight().background(Color(0xFF10B981)))  // Normal
                            Box(modifier = Modifier.weight(2.0f).fillMaxHeight().background(Color(0xFFF97316)))  // Pre-obese
                            Box(modifier = Modifier.weight(5.0f).fillMaxHeight().background(Color(0xFFEF4444)))  // Obese
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("< 18.5 (Gầy)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                            Text("18.5 - 22.9", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("23 - 24.9", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                            Text("≥ 25", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }

                    // Reference Weight Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "Khoảng cân nặng tham khảo:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (minIdealWeight != null && maxIdealWeight != null) {
                                        "%.0f - %.0f kg".format(minIdealWeight, maxIdealWeight)
                                    } else {
                                        "Cần cập nhật chiều cao"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Text(
                                text = "BMI hiện nằm trong khoảng tham khảo. BMI chỉ là chỉ số sàng lọc và không phản ánh đầy đủ thành phần cơ thể.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        // Card 3: Năng lượng hôm nay (Energy Intake vs Target)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF7ED)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column {
                                Text(
                                    text = "Năng lượng hôm nay",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "Calo nạp / Mục tiêu",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        ) {
                            Text(
                                text = "Mục tiêu ≈ ${money.format(target)} kcal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NocnomPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    // Intake Big Text & Percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = money.format(consumed),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = " / ≈ ${money.format(target)} kcal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }

                        Text(
                            text = "$progressPct%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = NocnomPrimary,
                        )
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NocnomPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )

                    // Meals & Remaining row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${todayLogs.size} bữa ăn hôm nay",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Còn khoảng ${money.format(remaining)} kcal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Card 4: Macro hôm nay
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Phân bổ Macro hôm nay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Đạm (Protein)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("%.1f g".format(protein), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Carb", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("%.1f g".format(carbs), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        ) {
                            Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Chất béo (Fat)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("%.1f g".format(fat), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            }
                        }
                    }

                    Text(
                        text = "Chỉ tính macro từ những món có dữ liệu thực tế. nOcnOm tuân thủ nguyên tắc 4/4/9 và không tự tạo macro ảo.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}
