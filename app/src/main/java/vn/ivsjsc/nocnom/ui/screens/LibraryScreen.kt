package vn.ivsjsc.nocnom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.Normalizer
import java.text.NumberFormat
import java.util.Locale
import java.util.regex.Pattern
import vn.ivsjsc.nocnom.domain.model.Dish
import vn.ivsjsc.nocnom.domain.model.UserState
import vn.ivsjsc.nocnom.ui.components.AddEditDishDialog
import vn.ivsjsc.nocnom.ui.components.DishDetailDialog
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

@Composable
fun LibraryScreen(
    state: UserState,
    contentPadding: PaddingValues,
    onAddDish: (Dish) -> Unit,
    onUpdateDish: (Dish) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("all") }

    var viewingDish by remember { mutableStateOf<Dish?>(null) }
    var editingDish by remember { mutableStateOf<Dish?>(null) }
    var isAddingDish by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    var copiedToast by remember { mutableStateOf(false) }

    val money = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")) }


    // Normalized search helper
    fun removeDiacritics(str: String): String {
        val nfd = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfd).replaceAll("").replace('đ', 'd').replace('Đ', 'D').lowercase()
    }

    val normalizedQuery = removeDiacritics(searchQuery.trim())

    val filteredDishes = state.dishes.filter { dish ->
        val matchesCategory = selectedCategoryId == "all" || dish.categoryId == selectedCategoryId
        val matchesSearch = normalizedQuery.isBlank() ||
            removeDiacritics(dish.name).contains(normalizedQuery) ||
            dish.vendors.any { removeDiacritics(it.name).contains(normalizedQuery) }
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header Card (QUẢN LÝ KHO MÓN)
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
                    // Header title & count badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "QUẢN LÝ KHO MÓN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = NocnomPrimary,
                            letterSpacing = 1.sp,
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF64748B).copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = "${state.dishes.size} món",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }

                    Text(
                        text = "Tìm món, kiểm tra calo và quản lý các quán đang bán.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên món, danh mục hoặc quán...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Xóa tìm kiếm",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    )

                    // Stat Cards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = "TỔNG SỐ MÓN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${state.dishes.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = "TỔNG DANH MỤC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp,
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${state.categories.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }

                    // Action buttons: + MÓN MỚI and SAO CHÉP
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Button(
                            onClick = { isAddingDish = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NocnomPrimary),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MÓN MỚI", fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val summary = state.dishes.joinToString("\n") { dish ->
                                    val price = dish.vendors.firstOrNull()?.price ?: 0L
                                    val kcal = dish.nutrition.calories?.toInt() ?: 0
                                    "${dish.name} - $kcal kcal - ${money.format(price)}đ"
                                }
                                clipboardManager.setText(AnnotatedString(summary))
                                copiedToast = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copiedToast) "ĐÃ SAO CHÉP" else "SAO CHÉP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Horizontal Category Filter Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // "TẤT CẢ" Pill
                CategoryPill(
                    label = "TẤT CẢ",
                    isSelected = selectedCategoryId == "all",
                    onSelect = { selectedCategoryId = "all" },
                )

                // Category Pills from state
                state.categories.forEach { category ->
                    CategoryPill(
                        label = category.name.uppercase(),
                        isSelected = selectedCategoryId == category.id,
                        onSelect = { selectedCategoryId = category.id },
                    )
                }
            }
        }

        // Dish Cards List
        if (filteredDishes.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "Không tìm thấy món ăn phù hợp",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Thử tìm kiếm với từ khóa khác hoặc bấm + MÓN MỚI để thêm món.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(filteredDishes, key = { it.id }) { dish ->
                val categoryName = state.categories.firstOrNull { it.id == dish.categoryId }?.name ?: "Món ăn"
                val calories = dish.nutrition.calories?.toInt() ?: 0
                val vendorCount = dish.vendors.size.coerceAtLeast(1)

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewingDish = dish },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // Dish Thumbnail Image
                        if (!dish.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = dish.imageUrl,
                                contentDescription = dish.name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }

                        // Dish Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NocnomPrimary.copy(alpha = 0.12f),
                                ) {
                                    Text(
                                        text = categoryName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NocnomPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFD97706).copy(alpha = 0.12f),
                                ) {
                                    Text(
                                        text = "Tham khảo",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD97706),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = dish.name,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "$vendorCount quán bán · ≈ $calories kcal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Actions on Right: Edit, Star, View
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                // Edit button
                                IconButton(
                                    onClick = { editingDish = dish },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Sửa món",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp),
                                    )
                                }

                                // Favorite Star button
                                IconButton(
                                    onClick = { onToggleFavorite(dish.id) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp)),
                                ) {
                                    Icon(
                                        imageVector = if (dish.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Yêu thích",
                                        tint = if (dish.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }

                            // View pill button
                            Surface(
                                onClick = { viewingDish = dish },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(28.dp),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Xem chi tiết",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dish Detail Dialog
    viewingDish?.let { dish ->
        val categoryName = state.categories.firstOrNull { it.id == dish.categoryId }?.name ?: "Món ăn"
        DishDetailDialog(
            dish = dish,
            categoryName = categoryName,
            onDismiss = { viewingDish = null },
            onEditDish = {
                viewingDish = null
                editingDish = dish
            },
        )
    }

    // Add Dish Dialog
    if (isAddingDish) {
        AddEditDishDialog(
            categories = state.categories,
            onDismiss = { isAddingDish = false },
            onSaveDish = onAddDish,
        )
    }

    // Edit Dish Dialog
    editingDish?.let { dish ->
        AddEditDishDialog(
            initialDish = dish,
            categories = state.categories,
            onDismiss = { editingDish = null },
            onSaveDish = onUpdateDish,
        )
    }
}

@Composable
private fun CategoryPill(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) NocnomPrimary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
