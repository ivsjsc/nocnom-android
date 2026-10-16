package vn.ivsjsc.nocnom.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import vn.ivsjsc.nocnom.R
import vn.ivsjsc.nocnom.domain.model.UserProfile
import vn.ivsjsc.nocnom.ui.theme.NocnomPrimary

@Composable
fun NocnomHeader(
    profile: UserProfile,
    accountEmail: String?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRefresh: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onOpenMethodology: () -> Unit,
    onSignOut: () -> Unit,
    onLogoClick: () -> Unit = {},
) {
    var showAccountMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLogoClick() }
                    .padding(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFCF7EC))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_nocnom_launcher),
                        contentDescription = "nOcnOm Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .padding(2.dp),
                    )
                }

                Column {
                    Text(
                        text = "nOcnOm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 22.sp,
                    )
                    Text(
                        text = "SINH VIÊN ĐHQG",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.8.sp,
                    )
                }
            }

            // Right: Actions (Refresh, Theme, Avatar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Làm mới dữ liệu",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Theme Toggle Button
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkTheme) "Bật giao diện sáng" else "Bật giao diện tối",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // User Avatar Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NocnomPrimary, Color(0xFF1D4ED8)),
                            ),
                        )
                        .border(1.5.dp, Color(0xFF60A5FA), RoundedCornerShape(14.dp))
                        .clickable { showAccountMenu = true },
                    contentAlignment = Alignment.Center,
                ) {
                    if (profile.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = profile.fullName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        val initials = getInitials(profile.fullName.ifBlank { accountEmail ?: "NG" })
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }

    if (showAccountMenu) {
        AccountMenuDialog(
            profile = profile,
            accountEmail = accountEmail,
            onDismiss = { showAccountMenu = false },
            onOpenProfile = {
                showAccountMenu = false
                onOpenProfile()
            },
            onOpenRecommendations = {
                showAccountMenu = false
                onOpenRecommendations()
            },
            onOpenMethodology = {
                showAccountMenu = false
                onOpenMethodology()
            },
            onSignOut = {
                showAccountMenu = false
                onSignOut()
            },
        )
    }
}

@Composable
fun AccountMenuDialog(
    profile: UserProfile,
    accountEmail: String?,
    onDismiss: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onOpenMethodology: () -> Unit,
    onSignOut: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
            ) {
                // Kicker Header
                Text(
                    text = "TÀI KHOẢN NOCNOM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.2.sp,
                )

                Spacer(modifier = Modifier.height(12.dp))

                // User Identity Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(NocnomPrimary),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (profile.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = profile.fullName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    text = getInitials(profile.fullName.ifBlank { accountEmail ?: "NG" }),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.fullName.ifBlank { "Sinh viên nOcnOm" },
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = accountEmail ?: "noc@nom.com",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Menu Items
                AccountMenuItem(
                    icon = Icons.Default.Tune,
                    title = "Quản lý tài khoản",
                    onClick = onOpenProfile,
                )

                AccountMenuItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Gợi ý món mỗi ngày",
                    onClick = onOpenRecommendations,
                )

                AccountMenuItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Chỉ số & cơ sở tính toán",
                    onClick = onOpenMethodology,
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                )

                AccountMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Đăng xuất",
                    textColor = MaterialTheme.colorScheme.error,
                    iconColor = MaterialTheme.colorScheme.error,
                    onClick = onSignOut,
                )


            }
        }
    }
}

@Composable
private fun AccountMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun getInitials(name: String): String {
    val clean = name.trim().split(" ").filter { it.isNotBlank() }
    if (clean.isEmpty()) return "NG"
    if (clean.size == 1) return clean[0].take(2).uppercase()
    return "${clean.first().first()}${clean.last().first()}".uppercase()
}
