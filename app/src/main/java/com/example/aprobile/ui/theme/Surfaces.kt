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

/** Cor de fundo sólida dos cards (evita "banding" do gradiente através da transparência). */
fun glassSurfaceColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF16241F) else Color(0xFFF3FAF7)

/** Variante de destaque (mais clara) do fundo de card. */
fun glassSurfaceElevatedColor(isDark: Boolean): Color =
    if (isDark) Color(0xFF1E302A) else Color(0xFFFFFFFF)

/** Borda sutil dos cards. */
fun glassBorderColor(isDark: Boolean): Color =
    if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2EEE9)
