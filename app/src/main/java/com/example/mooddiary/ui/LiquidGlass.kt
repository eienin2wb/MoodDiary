package com.example.mooddiary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 玻璃边框高光：左上角最亮、中间变暗、右下角再亮，模拟光线折射。 */
private val glassBorderColors = listOf(
    Color.White.copy(alpha = 0.95f),
    Color.White.copy(alpha = 0.20f),
    Color.White.copy(alpha = 0.55f)
)

/** pro 版边框高光，比普通版更亮一些。 */
private val glassProBorderColors = listOf(
    Color.White.copy(alpha = 0.98f),
    Color.White.copy(alpha = 0.15f),
    Color.White.copy(alpha = 0.65f)
)

/**
 * iOS 27 Liquid Glass 效果
 * - 半透明玻璃底
 * - 外边框高光（左亮右暗）
 *
 * 这些函数是 `@Composable` 的，为的是把 [Brush] 用 [remember] 缓存下来：
 * 它们会在重组时被反复调用，之前每次都要重新构造一遍渐变色列表。
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.48f),
    borderWidth: Dp = 1.dp
): Modifier {
    val borderBrush = remember { Brush.linearGradient(colors = glassBorderColors) }
    return this
        .clip(shape)
        .background(tint)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

/**
 * iOS 27 增强版玻璃：内部渐变 + 高光边框
 */
@Composable
fun Modifier.liquidGlassPro(
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.42f)
): Modifier {
    val surfaceBrush = remember(tint) {
        Brush.verticalGradient(
            colors = listOf(
                tint.copy(alpha = tint.alpha * 1.15f),
                tint.copy(alpha = tint.alpha * 0.85f)
            )
        )
    }
    val borderBrush = remember { Brush.linearGradient(colors = glassProBorderColors) }
    return this
        .clip(shape)
        .background(surfaceBrush)
        .border(width = 1.2.dp, brush = borderBrush, shape = shape)
}
