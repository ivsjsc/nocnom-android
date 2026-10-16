package vn.ivsjsc.nocnom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

private data class Citation(
    val title: String,
    val details: String,
    val identifier: String? = null,
)

private val citations = listOf(
    Citation(
        title = "Resting energy expenditure — Mifflin–St Jeor",
        details = "Mifflin MD, St Jeor ST, Hill LA, Scott BJ, Daugherty SA, Koh YO. A new predictive equation for resting energy expenditure in healthy individuals. Am J Clin Nutr. 1990;51(2):241–247.",
        identifier = "DOI: 10.1093/ajcn/51.2.241",
    ),
    Citation(
        title = "BMI và ngưỡng hành động cho người châu Á (WHO)",
        details = "WHO Expert Consultation. Appropriate body-mass index for Asian populations and its implications for policy and intervention strategies. Lancet. 2004;363(9403):157–163.",
        identifier = "DOI: 10.1016/S0140-6736(03)15268-3",
    ),
    Citation(
        title = "Asia-Pacific BMI Reference",
        details = "WHO Western Pacific Region, IASO, IOTF. The Asia-Pacific Perspective: Redefining Obesity and its Treatment. 2000.",
    ),
    Citation(
        title = "Dinh dưỡng & Hiệu suất Vận động (ACSM)",
        details = "Thomas DT, Erdman KA, Burke LM. Nutrition and Athletic Performance. J Acad Nutr Diet. 2016;116(3):501–528.",
        identifier = "DOI: 10.1016/j.jand.2015.12.006",
    ),
    Citation(
        title = "Protein & Thể thao (ISSN Position Stand)",
        details = "Jäger R, Kerksick CM, Campbell BI, et al. International Society of Sports Nutrition Position Stand: protein and exercise. J Int Soc Sports Nutr. 2017;14:20.",
        identifier = "DOI: 10.1186/s12970-017-0177-8",
    ),
    Citation(
        title = "Dietary Reference Intakes (DRI)",
        details = "Institute of Medicine / National Academies. Dietary Reference Intakes for Energy, Carbohydrate, Fiber, Fat, Fatty Acids, Cholesterol, Protein, Amino Acids and Water.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMethodologyScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉ số & Cơ sở tính toán",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero Intro Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = NocnomPrimary,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "MINH BẠCH PHƯƠNG PHÁP & KHOA HỌC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = NocnomPrimary,
                                letterSpacing = 0.8.sp,
                            )
                        }

                        Text(
                            text = "Công thức & Cơ sở dữ liệu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                        )

                        Text(
                            text = "nOcnOm áp dụng các tiêu chuẩn y tế quốc tế và hướng dẫn của WHO dành cho người châu Á. Mọi công thức đều được công khai minh bạch để sinh viên dễ dàng theo dõi sức khỏe an toàn.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }

            // Section 1: BMI Asia-Pacific
            item {
                MethodologyCard(
                    icon = Icons.Default.Scale,
                    title = "Chỉ số BMI chuẩn Châu Á (Asia-Pacific)",
                    subtitle = "WHO Western Pacific Region & IOTF",
                    content = """
                        • Thiếu cân (Gầy): BMI < 18.5
                        • Bình thường: BMI từ 18.5 đến 22.9
                        • Tiền béo phì (Thừa cân): BMI từ 23.0 đến 24.9
                        • Béo phì: BMI ≥ 25.0
                        
                        Khoảng cân nặng lý tưởng của nOcnOm được tính trực tiếp từ dải BMI 18.5 – 22.9 theo chiều cao thực tế của sinh viên.
                    """.trimIndent(),
                )
            }

            // Section 2: BMR & TDEE
            item {
                MethodologyCard(
                    icon = Icons.Default.Calculate,
                    title = "Năng lượng BMR & TDEE (Mifflin–St Jeor)",
                    subtitle = "Phương trình dự đoán tiêu hao năng lượng nghỉ",
                    content = """
                        • Nam: BMR = (10 × kg) + (6.25 × cm) - (5 × tuổi) + 5
                        • Nữ: BMR = (10 × kg) + (6.25 × cm) - (5 × tuổi) - 161
                        
                        TDEE (Tổng năng lượng tiêu hao mỗi ngày) = BMR × Hệ số vận động (1.2 đến 1.9).
                        
                        Mục tiêu calo hàng ngày được điều chỉnh dựa trên mục tiêu:
                        • Duy trì cân nặng: 100% TDEE
                        • Giảm cân an toàn: TDEE - 15% (khoảng 300–500 kcal)
                        • Tăng cân: TDEE + 15%
                    """.trimIndent(),
                )
            }

            // Section 3: Macronutrients
            item {
                MethodologyCard(
                    icon = Icons.Default.Favorite,
                    title = "Phân bổ Macronutrients (Đạm, Tinh bột, Béo)",
                    subtitle = "Khuyến nghị từ ACSM, ISSN & DRI",
                    content = """
                        • Protein: 1.2 – 2.0g trên mỗi kg thể trọng (tương đương 20–30% tổng năng lượng) nhằm bảo toàn và phát triển cơ bắp.
                        • Carbohydrate: 45–60% tổng năng lượng cung cấp nhiên liệu chính cho não bộ và hoạt động học tập.
                        • Chất béo (Fat): 20–35% tổng năng lượng, ưu tiên chất béo không bão hòa.
                        
                        Nguyên tắc nOcnOm: 1g Protein = 4 kcal, 1g Carb = 4 kcal, 1g Fat = 9 kcal. Không suy đoán macro khi món ăn chỉ có số kcal gốc.
                    """.trimIndent(),
                )
            }

            // Section 4: Academic Citations
            item {
                Text(
                    text = "TÀI LIỆU THAM KHẢO Y KHOA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            items(citations) { citation ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = citation.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = citation.details,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp,
                        )
                        citation.identifier?.let { id ->
                            Text(
                                text = id,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NocnomPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodologyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: String,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NocnomPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp),
            )


            Text(
                text = content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp,
            )
        }
    }
}
