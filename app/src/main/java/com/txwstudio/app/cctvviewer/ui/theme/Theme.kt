package com.txwstudio.app.cctvviewer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val CameraColors = darkColorScheme(
    primary = Color(0xFF9BDCD0),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005047),
    onPrimaryContainer = Color(0xFFB7F8EC),
    secondary = Color(0xFFB4CCC6),
    onSecondary = Color(0xFF203630),
    background = Color(0xFF101815),
    onBackground = Color(0xFFE0E4DF),
    surface = Color(0xFF101815),
    onSurface = Color(0xFFE0E4DF),
    surfaceVariant = Color(0xFF263A34),
    onSurfaceVariant = Color(0xFFBFC9C2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun CCTVViewerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = CameraColors, content = content)
}
