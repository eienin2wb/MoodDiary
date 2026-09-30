package com.example.mooddiary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 顶部栏：
 *   第一行  标题(可选) ......... [📸 瞬间][📝 记录][⋯]
 *   第二行            日期（整行居中）
 *
 * 日期独占一行，彻底避免窄屏上与右侧按钮组重合。
 */
@Composable
internal fun NotionHeader(
    title: String,
    subtitle: String,
    onInstantClick: () -> Unit,
    onMomentClick: () -> Unit,
    onMenuClick: () -> Unit,
    moreLabel: String,
    isIos: Boolean,
    lang: String
) {
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val bg = if (isIos) Color(0xFFF5F5FA) else MaterialTheme.colorScheme.background
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 12.dp)
    ) {
        // ── 第一行：标题(可选) + 右侧按钮组 ──────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = tc,
                    lineHeight = 34.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
            } else {
                // 无标题时把按钮组推到右边
                Spacer(Modifier.weight(1f))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 瞬间
                QuickHeaderButton(
                    emoji = "📸",
                    label = if (lang == "zh") "瞬间" else "Instant",
                    color = Color(0xFFFFB74D),
                    onClick = onInstantClick
                )

                // 记录
                QuickHeaderButton(
                    emoji = "📝",
                    label = if (lang == "zh") "记录" else "Moment",
                    color = primary,
                    onClick = onMomentClick
                )

                // ⋯ 菜单（按下高亮，松手自动恢复）
                val menuInteraction = remember { MutableInteractionSource() }
                val menuPressed by menuInteraction.collectIsPressedAsState()
                val menuBg by animateColorAsState(
                    targetValue = if (menuPressed) {
                        if (isIos) Color.Black.copy(alpha = 0.05f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    } else Color.Transparent,
                    animationSpec = tween(120),
                    label = "hdrMenuBg"
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(menuBg)
                        .clickable(
                            interactionSource = menuInteraction,
                            indication = null
                        ) { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreHoriz,
                        contentDescription = moreLabel,
                        tint = stc,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // ── 第二行：日期独立一行，整行居中 ────────────────────
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = stc,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* ================= 顶部快捷按钮 ================= */

@Composable
internal fun QuickHeaderButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "quickBtnScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        animationSpec = tween(150),
        label = "quickBtnBg"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 16.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
internal fun QuickActionMenu(
    isIos: Boolean,
    lang: String,
    primary: Color,
    onInstantClick: () -> Unit,
    onMomentClick: () -> Unit
) {
    val menuBg = if (isIos) {
        Modifier.liquidGlassPro(
            RoundedCornerShape(22.dp),
            Color.White.copy(alpha = 0.85f)
        )
    } else {
        Modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                clip = false
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
    }

    Row(
        modifier = menuBg.padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            emoji = "📸",
            label = if (lang == "zh") "瞬间" else "Instants",
            subtitle = if (lang == "zh") "快速记录" else "Quick",
            color = Color(0xFFFFB74D),
            onClick = onInstantClick
        )
        QuickActionButton(
            emoji = "📝",
            label = if (lang == "zh") "记录" else "Moments",
            subtitle = if (lang == "zh") "完整日记" else "Full diary",
            color = primary,
            onClick = onMomentClick
        )
    }
}

@Composable
internal fun QuickActionButton(
    emoji: String,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "quickBtnScale"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        animationSpec = tween(150),
        label = "quickBtnBg"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    subtitle,
                    fontSize = 10.sp,
                    color = color.copy(alpha = 0.75f),
                    maxLines = 1
                )
            }
        }
    }
}