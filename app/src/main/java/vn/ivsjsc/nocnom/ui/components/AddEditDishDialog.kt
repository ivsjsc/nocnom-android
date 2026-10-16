package vn.ivsjsc.nocnom.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.UUID
import vn.ivsjsc.nocnom.domain.model.Category
import vn.ivsjsc.nocnom.domain.model.Dish
import vn.ivsjsc.nocnom.domain.model.NutritionSnapshot
import vn.ivsjsc.nocnom.domain.model.Vendor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDishDialog(
    initialDish: Dish? = null,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSaveDish: (Dish) -> Unit,
) {
    var name by remember { mutableStateOf(initialDish?.name.orEmpty()) }
    var selectedCategoryId by remember {
        mutableStateOf(initialDish?.categoryId ?: categories.firstOrNull()?.id.orEmpty())
    }
    var caloriesText by remember {
        mutableStateOf(initialDish?.nutrition?.calories?.toInt()?.toString().orEmpty())
    }
    var proteinText by remember {
        mutableStateOf(initialDish?.nutrition?.proteinG?.toString().orEmpty())
    }
    var carbsText by remember {
        mutableStateOf(initialDish?.nutrition?.carbsG?.toString().orEmpty())
    }
    var fatText by remember {
        mutableStateOf(initialDish?.nutrition?.fatG?.toString().orEmpty())
    }
    var imageUrl by remember { mutableStateOf(initialDish?.imageUrl.orEmpty()) }
    var vendorName by remember {
        mutableStateOf(initialDish?.vendors?.firstOrNull()?.name.orEmpty())
    }
    var priceText by remember {
        mutableStateOf(initialDish?.vendors?.firstOrNull()?.price?.takeIf { it > 0 }?.toString().orEmpty())
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && selectedCategoryId.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (initialDish == null) "Thêm món mới" else "Chỉnh sửa món ăn",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên món ăn *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                ) {
                    val currentCategoryName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Chọn danh mục"
                    OutlinedTextField(
                        value = currentCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Danh mục *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                // Nutrition info
                Text(
                    text = "Dinh dưỡng ước tính",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { caloriesText = it.filter(Char::isDigit) },
                    label = { Text("Năng lượng (kcal)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        label = { Text("Đạm (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        label = { Text("Carb (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = fatText,
                        onValueChange = { fatText = it },
                        label = { Text("Béo (g)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                // Vendor and price
                Text(
                    text = "Quán bán & Giá tham khảo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Tên quán bán / Khu vực KTX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter(Char::isDigit) },
                    label = { Text("Giá bán (VNĐ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Đường dẫn ảnh món (URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                            val dishId = initialDish?.id ?: "custom-${UUID.randomUUID()}"
                            val priceLong = priceText.toLongOrNull() ?: 0L
                            val vendors = if (vendorName.isNotBlank() || priceLong > 0) {
                                listOf(
                                    Vendor(
                                        id = initialDish?.vendors?.firstOrNull()?.id ?: "vendor-1",
                                        name = vendorName.ifBlank { "Quán ăn KTX" },
                                        price = priceLong,
                                    ),
                                )
                            } else {
                                initialDish?.vendors ?: emptyList()
                            }

                            val nutrition = NutritionSnapshot(
                                calories = caloriesText.toDoubleOrNull(),
                                proteinG = proteinText.toDoubleOrNull(),
                                carbsG = carbsText.toDoubleOrNull(),
                                fatG = fatText.toDoubleOrNull(),
                                nutritionSource = "user-manual",
                            )

                            val dish = Dish(
                                id = dishId,
                                name = name.trim(),
                                categoryId = selectedCategoryId,
                                imageUrl = imageUrl.trim().takeIf { it.isNotBlank() } ?: initialDish?.imageUrl,
                                vendors = vendors,
                                nutrition = nutrition,
                                isFavorite = initialDish?.isFavorite ?: false,
                            )
                            onSaveDish(dish)
                            onDismiss()
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Lưu món")
                    }
                }
            }
        }
    }
}
