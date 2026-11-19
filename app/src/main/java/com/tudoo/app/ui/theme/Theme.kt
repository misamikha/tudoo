package com.tudoo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DodoCream = Color(0xFFF7F4D5)
val DodoPink = Color(0xFFF7DCDC)
val DodoSage = Color(0xFF839557)
val DodoDarkGreen = Color(0xFF0A3323)
val DodoRose = Color(0xFFD3968C)
val DodoSurface = Color(0xFFFFFBEA)

private val DodoColorScheme = lightColorScheme(
    primary = DodoDarkGreen,
    onPrimary = Color.White,
    primaryContainer = DodoSage,
    onPrimaryContainer = Color.White,
    secondary = DodoSage,
    onSecondary = Color.White,
    secondaryContainer = DodoPink,
    onSecondaryContainer = DodoDarkGreen,
    tertiary = DodoRose,
    onTertiary = Color.White,
    background = DodoCream,
    onBackground = DodoDarkGreen,
    surface = DodoSurface,
    onSurface = DodoDarkGreen,
    surfaceVariant = DodoPink,
    onSurfaceVariant = DodoDarkGreen,
    outline = DodoSage
)

@Composable
fun TudooTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DodoColorScheme,
        content = content
    )
}
