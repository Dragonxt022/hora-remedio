package com.example.aprobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Raios padronizados
val CardRadius = 20.dp
val LargeCardRadius = 24.dp
val SmallCardRadius = 16.dp
val FieldRadius = 12.dp
val ButtonRadius = 14.dp
val ChipRadius = 20.dp

// Espaçamentos padronizados
val ScreenHorizontalPadding = 16.dp
val ScreenVerticalPadding = 8.dp

/** Cor de fundo "vidro" usada nos cards sobre o gradiente. */
fun glassSurfaceColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f)

/** Variante mais clara (destaque) do fundo de card. */
fun glassSurfaceElevatedColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF1F332E).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.90f)

/** Borda sutil dos cards. */
fun glassBorderColor(isDark: Boolean): Color =
    if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.80f)
