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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import vn.ivsjsc.nocnom.domain.model.Dish
import vn.ivsjsc.nocnom.domain.model.LogEntry
import vn.ivsjsc.nocnom.domain.model.MealKey
import vn.ivsjsc.nocnom.domain.model.UserState
import vn.ivsjsc.nocnom.domain.time.VietnamTime
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

@Composable
fun HealthScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onOpenProfile: () -> Unit,
    onUpsertMealLog: (String, MealKey, Dish, String, Long) -> Unit = { _, _, _, _, _ -> },
    onRemoveMealLog: (String) -> Unit = {},
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

    val editRange = remember { VietnamTime.editableDateRange() }
    var calendarDate by remember { mutableStateOf(today) }
    var selectedDayKey by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val groupedDays = remember(state.logs) {
        state.logs
            .groupBy { VietnamTime.dateKey(it.timestamp) }
            .map { (dateKey, logs) ->
                DayLogGroup(
                    dateKey = dateKey,
                    logs = logs.sortedBy { it.mealKey?.ordinal ?: 99 },
                    totalCalories = logs.sumOf { it.nutrition.calories ?: 0.0 }.toInt(),
                )
            }
            .sortedByDescending { it.dateKey }
    }

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

        // Section: Header Nhật ký ăn uống theo ngày
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = NocnomPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Nhật ký ăn uống theo ngày",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // Card 5: Ghi bù hoặc chỉnh lịch sử
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = NocnomPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Ghi bù hoặc chỉnh lịch sử",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                ) {
                                    Text(
                                        text = "TỐI ĐA 3 NGÀY TRƯỚC",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NocnomPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val minDateFormatted = VietnamTime.formatDateOnly(editRange.first)
                            Text(
                                text = "Chọn từ $minDateFormatted đến hôm nay. Ngày tương lai và ngày cũ hơn chỉ được xem.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp,
                            )
                        }
                    }

                    // Date selector row & "Mở ngày" button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true },
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = VietnamTime.formatDayWithDate(calendarDate),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Button(
                            onClick = { selectedDayKey = calendarDate },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NocnomPrimary),
                            modifier = Modifier.height(48.dp),
                        ) {
                            Text(
                                text = "Mở ngày",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }

        // List of past log days
        if (groupedDays.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestaurantMenu,
                                contentDescription = null,
                                tint = NocnomPrimary,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Text(
                            text = "Chưa có lịch sử",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Có thể dùng lịch phía trên để ghi bù cho hôm nay hoặc tối đa 3 ngày trước.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }
        } else {
            items(groupedDays, key = { it.dateKey }) { dayGroup ->
                Card(
                    onClick = { selectedDayKey = dayGroup.dateKey },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = NocnomPrimary,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = VietnamTime.formatDayWithDate(dayGroup.dateKey),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "${dayGroup.logs.size} bữa",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "·",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF7ED),
                                ) {
                                    Text(
                                        text = "≈ ${money.format(dayGroup.totalCalories)} kcal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEA580C),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }

    // Modal / Dialog for Day Detail (similar to Web-app popup)
    selectedDayKey?.let { dKey ->
        DayDetailDialog(
            dateKey = dKey,
            state = state,
            isEditable = VietnamTime.isDateEditable(dKey),
            onDismiss = { selectedDayKey = null },
            onUpsertLog = { mKey, dish, vendorName, price ->
                onUpsertMealLog(dKey, mKey, dish, vendorName, price)
            },
            onRemoveLog = onRemoveMealLog,
        )
    }

    // DatePicker Dialog for selecting past 3 days or today
    if (showDatePicker) {
        val selectableDates = remember(editRange) {
            val dates = mutableListOf<String>()
            var cur = java.time.LocalDate.parse(editRange.first)
            val end = java.time.LocalDate.parse(editRange.second)
            while (!cur.isAfter(end)) {
                dates.add(cur.toString())
                cur = cur.plusDays(1)
            }
            dates.reversed()
        }

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = {
                Text(
                    text = "Chọn ngày ghi nhận",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    selectableDates.forEach { d ->
                        val isSelected = d == calendarDate
                        Surface(
                            onClick = {
                                calendarDate = d
                                showDatePicker = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NocnomPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, NocnomPrimary) else null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = VietnamTime.formatDayWithDate(d),
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) NocnomPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = NocnomPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Đóng")
                }
            },
        )
    }
}

private data class DayLogGroup(
    val dateKey: String,
    val logs: List<LogEntry>,
    val totalCalories: Int,
)

@Composable
private fun DayDetailDialog(
    dateKey: String,
    state: UserState,
    isEditable: Boolean,
    onDismiss: () -> Unit,
    onUpsertLog: (MealKey, Dish, String, Long) -> Unit,
    onRemoveLog: (String) -> Unit,
) {
    val money = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
    val dayLogs = state.logs
        .filter { VietnamTime.dateKey(it.timestamp) == dateKey }
        .sortedBy { it.mealKey?.ordinal ?: 99 }

    val totalCalories = dayLogs.sumOf { it.nutrition.calories ?: 0.0 }.toInt()

    var isEditMode by remember { mutableStateOf(false) }
    var editingMealKey by remember { mutableStateOf<MealKey?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Header: LỊCH SỬ TRONG NGÀY + Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LỊCH SỬ TRONG NGÀY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NocnomPrimary,
                            letterSpacing = 0.8.sp,
                        )
                        Text(
                            text = VietnamTime.formatDayWithDate(dateKey),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "${dayLogs.size} bữa đã ghi nhận",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isEditable) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Text(
                                    text = if (isEditable) "Có thể chỉnh sửa" else "Chỉ xem",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isEditable) NocnomPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng")
                    }
                }

                // Orange Card: TỔNG CALO TIÊU THỤ (giống Web-app ảnh 2)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF97316),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Column {
                            Text(
                                text = "TỔNG CALO TIÊU THỤ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.9f),
                                letterSpacing = 0.5.sp,
                            )
                            Text(
                                text = "≈ ${money.format(totalCalories)} kcal",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                            )
                        }
                    }
                }

                // Action button: Chỉnh sửa / Xong chỉnh sửa
                if (isEditable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        FilledTonalButton(
                            onClick = { isEditMode = !isEditMode },
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Icon(
                                imageVector = if (isEditMode) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditMode) "Xong chỉnh sửa" else "Chỉnh sửa")
                        }
                    }
                }

                // List of meals in the day or edit forms
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (isEditMode) {
                        items(listOf(MealKey.A, MealKey.B, MealKey.C)) { mKey ->
                            val existing = dayLogs.firstOrNull { it.mealKey == mKey }
                            val label = when (mKey) {
                                MealKey.A -> "Bữa sáng"
                                MealKey.B -> "Bữa trưa"
                                MealKey.C -> "Bữa tối"
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = NocnomPrimary,
                                        )
                                        if (existing != null) {
                                            IconButton(
                                                onClick = { onRemoveLog(existing.id) },
                                                modifier = Modifier.size(28.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Xóa",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }

                                    if (existing != null) {
                                        Text(
                                            text = existing.dishName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                        )
                                        Text(
                                            text = "Quán: ${existing.vendorName.ifBlank { "Không ghi quán" }} · ${money.format(existing.price)}đ",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    } else {
                                        Text(
                                            text = "Chưa ghi món cho bữa này",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { editingMealKey = mKey },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(if (existing != null) "Đổi món này" else "Chọn món ghi bữa")
                                    }
                                }
                            }
                        }
                    } else {
                        if (dayLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "Chưa có món ăn nào được ghi trong ngày này.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        } else {
                            items(dayLogs, key = { it.id }) { log ->
                                val mealTitle = when (log.mealKey) {
                                    MealKey.A -> "BỮA SÁNG"
                                    MealKey.B -> "BỮA TRƯA"
                                    MealKey.C -> "BỮA TỐI"
                                    null -> "BỮA ĂN"
                                }
                                val dish = state.dishes.firstOrNull { it.name == log.dishName }
                                val calo = (log.nutrition.calories ?: 0.0).toInt()

                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (!dish?.imageUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = dish?.imageUrl,
                                                    contentDescription = log.dishName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(14.dp)),
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFFEFF6FF)),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Restaurant,
                                                        contentDescription = null,
                                                        tint = NocnomPrimary,
                                                        modifier = Modifier.size(24.dp),
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = mealTitle,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = NocnomPrimary,
                                                    letterSpacing = 0.5.sp,
                                                )
                                                Text(
                                                    text = log.dishName,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                )
                                                Text(
                                                    text = "≈ $calo kcal · ${money.format(log.price)}đ",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }

                                        // Quán:
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Text(
                                                text = "Quán: ${log.vendorName.ifBlank { "Không ghi quán" }}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dish picker for a specific meal
    editingMealKey?.let { mKey ->
        var query by remember { mutableStateOf("") }
        val filteredDishes = state.dishes.filter {
            query.isBlank() || it.name.contains(query, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { editingMealKey = null },
            title = {
                Text(
                    text = "Chọn món cho " + when (mKey) {
                        MealKey.A -> "Bữa sáng"
                        MealKey.B -> "Bữa trưa"
                        MealKey.C -> "Bữa tối"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Tìm món ăn...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filteredDishes, key = { it.id }) { dish ->
                            val defaultVendor = dish.vendors.firstOrNull()
                            val price = defaultVendor?.price ?: 0
                            Surface(
                                onClick = {
                                    onUpsertLog(
                                        mKey,
                                        dish,
                                        defaultVendor?.name ?: "Không ghi quán",
                                        price,
                                    )
                                    editingMealKey = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dish.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                        )
                                        val kcal = dish.nutrition.calories?.toInt()?.let { "$it kcal" } ?: "0 kcal"
                                        Text(
                                            text = "$kcal · ${money.format(price)}đ",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editingMealKey = null }) {
                    Text("Đóng")
                }
            },
        )
    }
}
