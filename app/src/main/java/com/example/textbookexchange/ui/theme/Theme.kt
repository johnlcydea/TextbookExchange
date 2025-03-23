package com.example.textbookexchange.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), // Purple for headers and titles
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5), // Teal for buttons like "Add New Book" and "Edit"
    onSecondary = Color.Black,
    error = Color(0xFFB00020), // Red for "Logout" and "Delete" buttons
    onError = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Gray
)

@Composable
fun TextbookExchangeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}