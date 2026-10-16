package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay
import vn.ivsjsc.nocnom.domain.meal.RecommendationEngine
import vn.ivsjsc.nocnom.domain.model.DayMenu
import vn.ivsjsc.nocnom.domain.model.Dish
import vn.ivsjsc.nocnom.domain.model.MealKey
import vn.ivsjsc.nocnom.domain.model.MealSlot
import vn.ivsjsc.nocnom.domain.model.UserState
import vn.ivsjsc.nocnom.domain.time.VietnamTime
import vn.ivsjsc.nocnom.ui.components.DishDetailDialog
import vn.ivsjsc.nocnom.ui.theme.CalorieCardBrush
import vn.ivsjsc.nocnom.ui.theme.HeroMenuBrush
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

@Composable
fun HomeScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onOpenHistory: () -> Unit,
    onUpdateMeal: (MealKey, String?, Boolean) -> Unit,
    onUpdateMealForDay: (String, MealKey, String?, Boolean) -> Unit,
    onMarkMealEaten: (MealKey) -> Unit,
    onUnmarkMealEaten: (MealKey) -> Unit,
) {
    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val today = VietnamTime.dateKey(currentTimeMillis)
    val dayKey = VietnamTime.dayKey(currentTimeMillis)
    val todayLogs = state.logs.filter { VietnamTime.dateKey(it.timestamp) == today }
    val consumed = todayLogs.sumOf { it.nutrition.calories ?: 0.0 }.toInt()
    val target = state.profile.dailyCalorieTarget
    val protein = todayLogs.sumOf { it.nutrition.proteinG ?: 0.0 }
    val carbs = todayLogs.sumOf { it.nutrition.carbsG ?: 0.0 }
    val fat = todayLogs.sumOf { it.nutrition.fatG ?: 0.0 }

    val todayMenu = state.timetable[dayKey] ?: DayMenu(dayName = VietnamTime.vietnameseDayFull(dayKey))
    val eatenMealKeys = todayLogs.mapNotNull { it.mealKey }.toSet()

    // Dialog state for viewing a meal's dish
    var selectedDishDetail by remember { mutableStateOf<Pair<Dish, MealKey>?>(null) }

    // Dialog state for changing dish
    var changingMealTarget by remember { mutableStateOf<Triple<String, MealKey, MealSlot>?>(null) }

    val money = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")) }

    val hour = VietnamTime.hour(currentTimeMillis)

    val greeting = when (hour) {
        in 4..10 -> "Chào buổi sáng"
        in 11..13 -> "Chào buổi trưa"
        in 14..17 -> "Chào buổi chiều"
        in 18..21 -> "Chào buổi tối"
        else -> "Chào đêm muộn"
    }

    val greetingIcon = when (hour) {
        in 4..10 -> Icons.Default.WbSunny
        in 11..17 -> Icons.Default.LightMode
        else -> Icons.Default.NightsStay
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
        // Row 1: Greeting & Live Clock Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Greeting Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = greetingIcon,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        Text(
                            text = greeting,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                        )
                    }
                }

                // Clock Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFF59E0B), Color(0xFFEA580C)),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(19.dp),
                            )
                        }

                        Text(
                            text = VietnamTime.formatTime(currentTimeMillis),
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        // Row 2: Hero Calorie Card (Rich Amber Orange gradient)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CalorieCardBrush)
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left: ĐÃ TIÊU THỤ
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    text = "ĐÃ TIÊU THỤ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.8.sp,
                                )
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = money.format(consumed),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    lineHeight = 30.sp,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "kcal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(Color.White.copy(alpha = 0.25f)),
                        )

                        // Right: MỤC TIÊU NGÀY
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = "MỤC TIÊU NGÀY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.9f),
                                    letterSpacing = 0.8.sp,
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = money.format(target),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    lineHeight = 30.sp,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "kcal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Row 3: Blue Hero Menu Card ("Hôm nay Thứ ...")
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeroMenuBrush)
                        .padding(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column {
                            Text(
                                text = "Hôm nay ${VietnamTime.vietnameseDayFull(dayKey)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 24.sp,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "MENU 3 BỮA · TỰ LÀM MỚI MỖI NGÀY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.85f),
                                letterSpacing = 1.sp,
                            )
                        }

                        // 3 Meal Rows
                        listOf(
                            Triple(MealKey.A, "BỮA SÁNG", todayMenu.options[MealKey.A] ?: MealSlot()),
                            Triple(MealKey.B, "BỮA TRƯA", todayMenu.options[MealKey.B] ?: MealSlot()),
                            Triple(MealKey.C, "BỮA TỐI", todayMenu.options[MealKey.C] ?: MealSlot()),
                        ).forEach { (mKey, mLabel, slot) ->
                            val dish = state.dishes.firstOrNull { it.id == slot.dishId }
                            val isEaten = mKey in eatenMealKeys

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (dish != null && !slot.skipped) {
                                            selectedDishDetail = dish to mKey
                                        } else {
                                            changingMealTarget = Triple(dayKey, mKey, slot)
                                        }
                                    },
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    // Thumbnail or skipped icon
                                    if (slot.skipped) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Block,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.9f),
                                            )
                                        }
                                    } else if (dish?.imageUrl?.isNotBlank() == true) {
                                        AsyncImage(
                                            model = dish.imageUrl,
                                            contentDescription = dish.name,
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop,
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Restaurant,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.8f),
                                            )
                                        }
                                    }

                                    // Meal Info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White.copy(alpha = 0.85f),
                                            letterSpacing = 0.8.sp,
                                        )

                                        if (slot.skipped) {
                                            Text(
                                                text = if (mKey == MealKey.B) "Không ăn buổi trưa" else "Không ăn bữa này",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = Color.White.copy(alpha = 0.95f),
                                            )
                                        } else {
                                            Text(
                                                text = dish?.name ?: "Chưa chọn món",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                            )
                                            val kcal = dish?.nutrition?.calories?.toInt()?.let { "≈ $it kcal" } ?: "≈ 0 kcal"
                                            val price = dish?.vendors?.firstOrNull()?.price?.takeIf { it > 0 }
                                                ?.let { "${money.format(it)}đ" } ?: "chưa có giá"

                                            Text(
                                                text = "$kcal · $price",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White.copy(alpha = 0.85f),
                                            )
                                        }
                                    }

                                    // Action Badge / Button
                                    if (isEaten) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .rotate(-6f)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.White, CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .clickable { onUnmarkMealEaten(mKey) },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Đã ăn",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                Text(
                                                    text = "ĐÃ ĂN",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    letterSpacing = 0.5.sp,
                                                )
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (dish != null && !slot.skipped) {
                                                    selectedDishDetail = dish to mKey
                                                } else {
                                                    changingMealTarget = Triple(dayKey, mKey, slot)
                                                }
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White,
                                                contentColor = NocnomPrimary,
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                            modifier = Modifier.height(38.dp),
                                        ) {
                                            Text(
                                                text = "XEM",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
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

        // Row 4: Weekly Table Intro & Accordion ("Lịch ăn tuần này")
        item {
            Card(
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NocnomPrimary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Column {
                        Text(
                            text = "Lịch ăn tuần này",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Các ngày còn lại trong tuần. Chạm ngày để xem; chỉ mở bộ chọn khi cần đổi món.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }

        // Weekly Days List (excluding today)
        val dayKeys = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
        val otherDays = dayKeys.filter { it != dayKey }

        items(otherDays, key = { it }) { otherDay ->
            val otherMenu = state.timetable[otherDay] ?: DayMenu(dayName = VietnamTime.vietnameseDayFull(otherDay))
            var isExpanded by remember { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = VietnamTime.vietnameseDayFull(otherDay),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                Triple(MealKey.A, "Bữa sáng", otherMenu.options[MealKey.A] ?: MealSlot()),
                                Triple(MealKey.B, "Bữa trưa", otherMenu.options[MealKey.B] ?: MealSlot()),
                                Triple(MealKey.C, "Bữa tối", otherMenu.options[MealKey.C] ?: MealSlot()),
                            ).forEach { (mKey, mLabel, slot) ->
                                val dish = state.dishes.firstOrNull { it.id == slot.dishId }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                                                text = mLabel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                text = if (slot.skipped) "Không ăn bữa này" else dish?.name ?: "Chưa chọn món",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }

                                        TextButton(
                                            onClick = { changingMealTarget = Triple(otherDay, mKey, slot) },
                                        ) {
                                            Text("Đổi món")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Row 5: Macro Summary Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Macro hôm nay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MacroCard("Protein", protein, Modifier.weight(1f))
                        MacroCard("Carb", carbs, Modifier.weight(1f))
                        MacroCard("Fat", fat, Modifier.weight(1f))
                    }
                    Text(
                        text = "Chỉ cộng macro từ món có dữ liệu macro thực tế; không suy ngược macro từ kcal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Row 6: History Button
        item {
            OutlinedButton(
                onClick = onOpenHistory,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xem lịch sử ăn uống")
            }
        }
    }

    // Dish Detail Dialog when clicking "XEM"
    selectedDishDetail?.let { (dish, mKey) ->
        val categoryName = state.categories.firstOrNull { it.id == dish.categoryId }?.name ?: "Món ăn"
        val isEaten = mKey in eatenMealKeys

        DishDetailDialog(
            dish = dish,
            categoryName = categoryName,
            isEaten = isEaten,
            onDismiss = { selectedDishDetail = null },
            onToggleEaten = {
                if (isEaten) onUnmarkMealEaten(mKey) else onMarkMealEaten(mKey)
            },
            onSelectForMeal = {
                changingMealTarget = Triple(dayKey, mKey, todayMenu.options[mKey] ?: MealSlot())
            },
        )
    }

    // Changing Meal Dish Picker Dialog
    changingMealTarget?.let { (tDayKey, mKey, slot) ->
        var query by remember { mutableStateOf("") }
        val recommendations = RecommendationEngine.rank(state, mKey)
        val visibleDishes = recommendations.filter {
            query.isBlank() || it.dish.name.contains(query, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { changingMealTarget = null },
            title = {
                Text(
                    text = "Đổi món ${mealKeyLabel(mKey).lowercase()}",
                    fontWeight = FontWeight.Black,
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
                        placeholder = { Text("Tìm tên món...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (slot.skipped) "Đang đánh dấu: Không ăn" else "Trạng thái: Có ăn",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        TextButton(
                            onClick = {
                                onUpdateMealForDay(tDayKey, mKey, slot.dishId.takeIf(String::isNotBlank), !slot.skipped)
                                changingMealTarget = null
                            },
                        ) {
                            Text(if (slot.skipped) "Ăn bữa này" else "Không ăn bữa này")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(visibleDishes, key = { it.dish.id }) { item ->
                            Surface(
                                onClick = {
                                    onUpdateMealForDay(tDayKey, mKey, item.dish.id, false)
                                    changingMealTarget = null
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
                                            text = item.dish.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                        )
                                        val kcal = item.dish.nutrition.calories?.toInt()?.let { "$it kcal" } ?: "0 kcal"
                                        val price = item.minPrice?.takeIf { it > 0 }?.let { "${money.format(it)}đ" }
                                        Text(
                                            text = listOfNotNull(kcal, price).joinToString(" · "),
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
                TextButton(onClick = { changingMealTarget = null }) {
                    Text("Đóng")
                }
            },
        )
    }
}

@Composable
private fun MacroCard(label: String, grams: Double, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text("%.1f g".format(grams), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private fun mealKeyLabel(mealKey: MealKey): String = when (mealKey) {
    MealKey.A -> "Bữa sáng"
    MealKey.B -> "Bữa trưa"
    MealKey.C -> "Bữa tối"
}
