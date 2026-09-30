package com.example.mooddiary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.Mood
import com.example.mooddiary.util.L

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


