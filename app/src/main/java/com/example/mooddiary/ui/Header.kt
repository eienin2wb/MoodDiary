package com.example.mooddiary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Shadow
import com.example.mooddiary.data.Mood
import com.example.mooddiary.util.L

/* ================= 来自 NotionHeader.kt ================= */

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
    val tc =
        if (isIos) IosColor.textPrimary
        else MaterialTheme.colorScheme.onSurface

    val stc =
        if (isIos) IosColor.textSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant

    val bg =
        if (isIos) Color(0xFFF5F5FA)
        else MaterialTheme.colorScheme.background

    val primary =
        if (isIos) IosColor.primary
        else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Spacer(Modifier.weight(1f))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickHeaderButton(
                    emoji = "📸",
                    label = if (lang == "zh") "瞬间" else "Instant",
                    color = Color(0xFFFFB74D),
                    onClick = onInstantClick
                )

                QuickHeaderButton(
                    emoji = "📝",
                    label = if (lang == "zh") "记录" else "Moment",
                    color = primary,
                    onClick = onMomentClick
                )

                val menuInteraction = remember {
                    MutableInteractionSource()
                }

                val menuPressed by menuInteraction.collectIsPressedAsState()

                val menuBg by animateColorAsState(
                    targetValue = if (menuPressed) {
                        if (isIos) {
                            Color.Black.copy(alpha = 0.06f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    } else {
                        Color.Transparent
                    },
                    animationSpec = tween(120),
                    label = "headerMenuBackground"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(menuBg)
                        .clickable(
                            interactionSource = menuInteraction,
                            indication = null
                        ) {
                            onMenuClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = moreLabel,
                        tint = stc,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

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

@Composable
internal fun QuickHeaderButton(
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quickHeaderScale"
    )

    val bgAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        animationSpec = tween(150),
        label = "quickHeaderBackground"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(color.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 16.sp
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
internal fun QuickActionMenu(
    isIos: Boolean,
    onInstantClick: () -> Unit,
    onMomentClick: () -> Unit,
    lang: String
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = if (isIos) {
            Modifier
                .fillMaxWidth()
                .liquidGlassPro(
                    shape,
                    Color.White.copy(alpha = 0.85f)
                )
        } else {
            Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = shape
                )
                .clip(shape)
                .background(
                    MaterialTheme.colorScheme.surface
                )
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                emoji = "📸",
                title = if (lang == "zh") "瞬间" else "Instant",
                subtitle = if (lang == "zh") "快速记录" else "Quick",
                color = Color(0xFFFFB74D),
                onClick = onInstantClick
            )

            QuickActionButton(
                modifier = Modifier.weight(1f),
                emoji = "📝",
                title = if (lang == "zh") "记录" else "Moment",
                subtitle = if (lang == "zh") "完整日记" else "Full diary",
                color = if (isIos) {
                    IosColor.primary
                } else {
                    MaterialTheme.colorScheme.primary
                },
                onClick = onMomentClick
            )
        }
    }
}

@Composable
internal fun QuickActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.60f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quickActionScale"
    )

    val backgroundAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.08f,
        animationSpec = tween(150),
        label = "quickActionBackground"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(
                color.copy(alpha = backgroundAlpha)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            }
            .padding(
                horizontal = 14.dp,
                vertical = 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 21.sp
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = color.copy(alpha = 0.72f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* ================= 来自 TopMenu.kt ================= */

@Composable
internal fun IosFloatingHeader(
    title: String, subtitle: String, onMenuClick: () -> Unit,
    menuOpen: Boolean, moreLabel: String
) {
    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .liquidGlassPro(RoundedCornerShape(30.dp), Color.White.copy(alpha = 0.55f))
                .padding(start = 24.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = IosColor.textPrimary, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = IosColor.textSecondary.copy(alpha = 0.85f), maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            val menuScale by animateFloatAsState(
                targetValue = if (menuOpen) 0.92f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "menuScale"
            )
            Box(
                modifier = Modifier.size(42.dp)
                    .graphicsLayer { scaleX = menuScale; scaleY = menuScale }
                    .clip(CircleShape)
                    .background(if (menuOpen) Color.White.copy(alpha = 0.78f) else Color.White.copy(alpha = 0.38f))
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MoreHoriz, moreLabel, tint = IosColor.primary, modifier = Modifier.size(22.dp))
            }
        }
    }
}


@Composable
internal fun TopMenuPanel(
    isIos: Boolean, lang: String,
    onStats: () -> Unit, onOpenCardStyle: () -> Unit,
    onBackfill: () -> Unit, onSettings: () -> Unit
) {
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val dc = if (isIos) IosColor.separator.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    val panelModifier = if (isIos) {
        Modifier
            .width(260.dp)
            .liquidGlassPro(RoundedCornerShape(22.dp), Color.White.copy(alpha = 0.92f))
    } else {
        Modifier
            .width(260.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(22.dp),
                clip = false
            )
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
    }

    Column(
        modifier = panelModifier.padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⋯", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = stc)
            Spacer(Modifier.width(8.dp))
            Text(L.t("menu_more", lang), fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                color = stc, letterSpacing = 0.5.sp)
        }
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(0.5.dp).background(dc))
        TopMenuRow("📊", Color(0xFFE3F2FD), L.t("menu_stats", lang),
            if (lang == "zh") "查看本月情绪分布" else "View monthly mood stats",
            tc, stc, onStats)
        TopMenuRow("🎨", Color(0xFFF3E5F5), L.t("menu_card_style", lang),
            if (lang == "zh") "背景、圆角、分享模板" else "Background, radius, templates",
            tc, stc, onOpenCardStyle)
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)
            .height(0.5.dp).background(dc))
        TopMenuRow("⏱️", Color(0xFFFFF3E0), L.t("menu_backfill", lang),
            if (lang == "zh") "补记错过的日期" else "Record a past day",
            tc, stc, onBackfill)
        TopMenuRow("⚙️", Color(0xFFECEFF1), L.t("menu_settings", lang),
            if (lang == "zh") "语言、导出、清空数据" else "Language, export, clear data",
            tc, stc, onSettings)
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 6.dp)
            .height(0.5.dp).background(dc))
        Text(L.t("version", lang), fontSize = 10.sp, color = stc.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp),
            textAlign = TextAlign.Center)
    }
}


@Composable
internal fun TopMenuRow(
    icon: String, iconBg: Color,
    title: String, subtitle: String,
    textColor: Color, subColor: Color,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val bg by animateColorAsState(
        targetValue = if (pressed) Color.White.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(120), label = "menuRowBg"
    )
    val itemScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "menuRowScale"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(14.dp)).background(bg)
            .graphicsLayer { scaleX = itemScale; scaleY = itemScale }
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                color = textColor, maxLines = 1)
            Spacer(Modifier.height(1.dp))
            Text(subtitle, fontSize = 11.sp, color = subColor, maxLines = 1)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = subColor.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
    }
}

