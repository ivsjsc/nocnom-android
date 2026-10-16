package vn.ivsjsc.nocnom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val NocnomPrimary = Color(0xFF2563EB)
val NocnomPrimaryDark = Color(0xFF3B82F6)
val NocnomBlueDark = Color(0xFF1D4ED8)
val NocnomBlueLight = Color(0xFFEFF6FF)

val CalorieAmber = Color(0xFFC2410C)
val CalorieAmberEnd = Color(0xFF9A3412)
val CalorieCardBrush = Brush.horizontalGradient(listOf(Color(0xFFC2410C), Color(0xFF9A3412)))
val HeroMenuBrush = Brush.verticalGradient(listOf(Color(0xFF1E40AF), Color(0xFF2563EB)))

private val LightColors = lightColorScheme(
    primary = NocnomPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = NocnomPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF121A2A),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1C273D),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF293751),
    outlineVariant = Color(0xFF334155),
    error = Color(0xFFEF4444),
    onError = Color.White,
)

@Composable
fun NocnomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NocnomTypography,
        content = content,
    )
}
