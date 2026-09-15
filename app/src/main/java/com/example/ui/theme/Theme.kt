package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Emerald400,
    onPrimary = Slate950,
    primaryContainer = Emerald800,
    onPrimaryContainer = Emerald100,
    secondary = BudgetBlue,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate200,
    tertiary = WithdrawAmber,
    onTertiary = Slate950,
    tertiaryContainer = Slate850,
    onTertiaryContainer = WithdrawAmber,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate800,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald700,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = BudgetBlue,
    onSecondary = Color.White,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate800,
    tertiary = WithdrawAmber,
    onTertiary = Color.White,
    tertiaryContainer = WithdrawAmberBg,
    onTertiaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = ExpenseRed,
    onError = Color.White
)

@Composable
fun BizLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional business palette for crisp brand identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
