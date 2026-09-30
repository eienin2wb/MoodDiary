package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L
import kotlinx.coroutines.delay
import java.util.Calendar as JavaCal

data class UnifiedStat(
    val emoji: String,
    val label: String,
    val color: Color,
    val count: Int,
    val percent: Float,
    val key: String
)


@Composable
internal fun StatsDialog(
    entries: List<MoodEntry>, isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onDismiss: () -> Unit
) {
    val cal = JavaCal.getInstance()
    val y = cal.get(JavaCal.YEAR)
    val m = cal.get(JavaCal.MONTH)
    // 月份区间只算一次，避免每个条目都新建一个 Calendar 去比较年月
    val monthStart = remember(y, m) {
        JavaCal.getInstance().apply {
            set(JavaCal.YEAR, y)
            set(JavaCal.MONTH, m)
            set(JavaCal.DAY_OF_MONTH, 1)
            set(JavaCal.HOUR_OF_DAY, 0)
            set(JavaCal.MINUTE, 0)
            set(JavaCal.SECOND, 0)
            set(JavaCal.MILLISECOND, 0)
        }.timeInMillis
    }
    val monthEnd = remember(monthStart) {
        JavaCal.getInstance().apply {
            timeInMillis = monthStart
            add(JavaCal.MONTH, 1)
        }.timeInMillis
    }
    val monthEntries = remember(entries, monthStart, monthEnd) {
        entries.filter { it.dayStart in monthStart until monthEnd }
    }
    val total = monthEntries.size
    val monthLabel = "${y}年${m + 1}月"

    val unifiedStats = remember(entries, customs, customMoodDefs, lang) {
        val list = mutableListOf<UnifiedStat>()

        Mood.values().forEach { mood ->
            val count = monthEntries.count { it.customMoodId == null && it.mood == mood }
            list.add(
                UnifiedStat(
                    emoji = emojiOf(mood, customs),
                    label = labelOf(mood, customs, lang),
                    color = mood.color(),
                    count = count,
                    percent = if (total > 0) count.toFloat() / total else 0f,
                    key = "preset_${mood.name}"
                )
            )
        }

        customMoodDefs.forEach { def ->
            val count = monthEntries.count { it.customMoodId == def.id }
            list.add(
                UnifiedStat(
                    emoji = def.emoji,
                    label = def.label,
                    color = Color(0xFF007AFF),
                    count = count,
                    percent = if (total > 0) count.toFloat() / total else 0f,
                    key = "custom_${def.id}"
                )
            )
        }

        list.sortedByDescending { it.count }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(
                L.t("stats_title", lang) + " · " + monthLabel,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        String.format(L.t("stats_month_count", lang), total),
                        fontSize = 15.sp,
                        color = if (isIos) IosColor.textSecondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    if (total > 0) {
                        Text(
                            "${monthEntries.size} / ${unifiedStats.count { it.count > 0 }} 种",
                            fontSize = 12.sp,
                            color = if (isIos) IosColor.textTertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (total == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            L.t("stats_empty", lang),
                            color = if (isIos) IosColor.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    unifiedStats.forEachIndexed { i, stat ->
                        var grew by remember { mutableStateOf(false) }
                        LaunchedEffect(stat.key) {
                            delay(i * 80L)
                            grew = true
                        }
                        UnifiedStatBar(stat, isIos, grew)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    L.t("close", lang),
                    fontSize = 17.sp,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}


@Composable
internal fun UnifiedStatBar(
    stat: UnifiedStat, isIos: Boolean, grew: Boolean
) {
    val barFraction by animateFloatAsState(
        targetValue = if (grew) stat.percent.coerceIn(0f, 1f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bf_${stat.key}"
    )
    val rowAlpha by animateFloatAsState(
        targetValue = if (grew) 1f else 0f,
        animationSpec = tween(300),
        label = "rowA_${stat.key}"
    )
    val rowX by animateFloatAsState(
        targetValue = if (grew) 0f else -12f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "rowX_${stat.key}"
    )

    Column(
        modifier = Modifier.graphicsLayer {
            alpha = rowAlpha
            translationX = rowX
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                stat.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            if (stat.count == 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    "·",
                    fontSize = 12.sp,
                    color = if (isIos) IosColor.textTertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${stat.count}  ·  ${(stat.percent * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = if (stat.count > 0) FontWeight.SemiBold
                else FontWeight.Normal,
                color = if (stat.count > 0)
                    (if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
                else if (isIos) IosColor.textTertiary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isIos) IosColor.bg
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (stat.count > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barFraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(stat.color)
                )
            }
        }
    }
}


