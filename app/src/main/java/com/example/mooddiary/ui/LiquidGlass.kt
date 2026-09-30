package com.example.mooddiary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS 26 风格 Liquid Glass 玻璃效果
 * - 半透明白色玻璃底
 * - 高光渐变边框（模拟光线折射）
 */
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.55f),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(tint)
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),   // 左上角高光
                Color.White.copy(alpha = 0.15f),  // 中间
                Color.White.copy(alpha = 0.6f)    // 右下角
            )
        ),
        shape = shape
    )