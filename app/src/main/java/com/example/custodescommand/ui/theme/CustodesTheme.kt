
package com.example.custodescommand.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustodesDarkColors = darkColorScheme(
    primary = Color(0xFFD6B65F),
    onPrimary = Color(0xFF2C2107),

    primaryContainer = Color(0xFF51411C),
    onPrimaryContainer = Color(0xFFF7E3A8),

    background = Color(0xFF101015),
    onBackground = Color(0xFFF2EFE8),

    surface = Color(0xFF1B1B24),
    onSurface = Color(0xFFF2EFE8),

    surfaceVariant = Color(0xFF2B2930),
    onSurfaceVariant = Color(0xFFCEC6BB)
)

@Composable
fun CustodesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CustodesDarkColors,
        content = content
    )
}
