package com.example.photos.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF252821)
private val Paper = Color(0xFFF7F5EF)
private val Canvas = Color(0xFFFFFEFA)
private val Clay = Color(0xFF9C4C37)
private val Mint = Color(0xFFBFD9C4)

private val LightColors = lightColorScheme(
    primary = Clay,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DCCF),
    onPrimaryContainer = Color(0xFF4C2118),
    secondary = Color(0xFF456650),
    onSecondary = Color.White,
    secondaryContainer = Mint,
    onSecondaryContainer = Color(0xFF1C3825),
    background = Canvas,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFECE9E1),
    onSurfaceVariant = Color(0xFF51554D),
    outline = Color(0xFF787B72),
    error = Color(0xFFB3261E),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF1AD98),
    onPrimary = Color(0xFF572515),
    primaryContainer = Color(0xFF753723),
    onPrimaryContainer = Color(0xFFFFDACC),
    secondary = Color(0xFFB1D1B6),
    onSecondary = Color(0xFF1D3B27),
    secondaryContainer = Color(0xFF34523C),
    onSecondaryContainer = Color(0xFFCDEBD0),
    background = Color(0xFF151814),
    onBackground = Color(0xFFF0F1E9),
    surface = Color(0xFF222620),
    onSurface = Color(0xFFF0F1E9),
    surfaceVariant = Color(0xFF343A33),
    onSurfaceVariant = Color(0xFFC2C8BE),
    outline = Color(0xFF929A90),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val PhotoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

private val PhotoShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun PhotoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PhotoTypography,
        shapes = PhotoShapes,
        content = content,
    )
}
