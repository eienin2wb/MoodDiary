package com.example.mooddiary.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object IosColor {
    val bg = Color(0xFFF2F2F7)
    val card = Color(0xFFFFFFFF)
    val primary = Color(0xFF007AFF)
    val danger = Color(0xFFFF3B30)
    val success = Color(0xFF34C759)
    val textPrimary = Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val textTertiary = Color(0xFFC7C7CC)
    val separator = Color(0xFFC6C6C8)
    val groupHeader = Color(0xFF6D6D72)
}

object IosRadius {
    val card = 24.dp
    val button = 14.dp
    val smallButton = 10.dp
    val largeButton = 20.dp
    val capsule = 999.dp
}

@Composable
fun RowScope.IosIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = IosColor.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun IosSectionHeader(text: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text.uppercase(),
            fontSize = 12.sp,
            color = IosColor.groupHeader,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                trailing,
                fontSize = 12.sp,
                color = IosColor.groupHeader,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IosDivider(startPadding: Int = 56) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding.dp)
            .height(0.5.dp)
            .background(IosColor.separator)
    )
}