package com.example.mooddiary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.util.L
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.cachedDateFormatter
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.example.mooddiary.data.DiaryEntry
import com.example.mooddiary.data.Storage

/* ================= 来自 PagerTabs.kt ================= */

/**
 * 主页 / 日记 两个主 Tab 的切换条。
 *
 * 它们背后是两套完全独立的数据：主页读情绪记录，日记读 diary.json。
 */
@Composable
internal fun MainTabs(
    currentPage: Int,
    isIos: Boolean,
    primary: Color,
    lang: String,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            PagerTab(
                emoji = "🏠",
                label = L.t("tab_home", lang),
                selected = currentPage == 0,
                isIos = isIos,
                primary = primary
            ) { onSelect(0) }
            PagerTab(
                emoji = "📖",
                label = L.t("tab_diary", lang),
                selected = currentPage == 1,
                isIos = isIos,
                primary = primary
            ) { onSelect(1) }
        }
        PagerIndicator(
            pageCount = 2,
            currentPage = currentPage,
            primary = primary,
            isIos = isIos,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/* ================= 页码指示器（小圆点） ================= */

@Composable
internal fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    primary: Color,
    isIos: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 20.dp else 6.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicatorWidth"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) primary
                else (if (isIos) IosColor.textTertiary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                animationSpec = tween(250),
                label = "indicatorColor"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

/* ================= 页面标签（可点击切换） ================= */

@Composable
internal fun PagerTab(
    emoji: String,
    label: String,
    selected: Boolean,
    isIos: Boolean,
    primary: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "tabScale"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected -> primary
            isIos -> IosColor.textSecondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "tabText"
    )
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = fontWeight,
            color = textColor
        )
        if (selected) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(primary)
            )
        }
    }
}

/* ================= 来自 HomeTab.kt ================= */

/**
 * 历史列表的月份分组标题格式，例如「2026年9月」/「September 2026」。
 * 用 [cachedDateFormatter] 复用实例，避免每次重组都新建。
 */
internal fun monthLabelFormatter(lang: String): SimpleDateFormat {
    val pattern = if (isCjkLang(lang)) "yyyy年M月" else "MMMM yyyy"
    return cachedDateFormatter(pattern, localeFor(lang))
}

/**
 * 主页 Tab：今日情绪卡 + 连续打卡 + 情绪记录时间线。
 *
 * 只消费 MoodEntry（情绪数据）；日记相关的数据在 [DiaryTab]，两者互不影响。
 */
@Composable
internal fun HomeTab(
    todayEntry: MoodEntry?,
    entries: List<MoodEntry>,
    streak: Int,
    appearance: CardAppearance,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onEditMood: () -> Unit,
    onShareMood: (MoodEntry) -> Unit,
    onEditEntry: (MoodEntry) -> Unit,
    onDeleteEntry: (MoodEntry) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = appearance,
            transitionSpec = {
                (fadeIn(tween(300)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                )) togetherWith (fadeOut(tween(200)) + scaleOut(
                    targetScale = 1.04f,
                    animationSpec = tween(200)
                ))
            },
            label = "cardBgSwitch"
        ) { animAppearance ->
            TodayCard(
                entry = todayEntry,
                appearance = animAppearance,
                isIos = isIos,
                lang = lang,
                customs = customs,
                customMoodDefs = customMoodDefs,
                onEdit = onEditMood,
                onShare = { todayEntry?.let(onShareMood) }
            )
        }

        Spacer(Modifier.height(8.dp))
        StreakRow(streak, isIos, lang)

        if (isIos) {
            IosSectionHeader(
                L.t("history", lang),
                String.format(L.t("total_count", lang), entries.size)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    L.t("history", lang),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    String.format(L.t("total_count", lang), entries.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (entries.isEmpty()) {
            EmptyState(lang, isIos, onAddClick = onEditMood)
        } else {
            val monthSdf = remember(lang) { monthLabelFormatter(lang) }

            val groups = remember(entries, monthSdf) {
                entries.groupBy { monthSdf.format(Date(it.dayStart)) }.toList()
            }
            val firstIds = remember(groups) {
                buildSet { groups.forEach { (_, ms) -> ms.firstOrNull()?.let { add(it.id) } } }
            }
            val lastIds = remember(groups) {
                buildSet { groups.forEach { (_, ms) -> ms.lastOrNull()?.let { add(it.id) } } }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                groups.forEach { (monthLabel, monthEntries) ->
                    item(key = "header_$monthLabel") {
                        MonthHeader(
                            monthLabel, monthEntries, isIos, lang,
                            customs, customMoodDefs
                        )
                    }
                    items(monthEntries, key = { it.id }) { e ->
                        TimelineEntryRow(
                            entry = e,
                            isFirst = e.id in firstIds,
                            isLast = e.id in lastIds,
                            isIos = isIos,
                            lang = lang,
                            customs = customs,
                            customMoodDefs = customMoodDefs,
                            appearance = appearance,
                            onEdit = { onEditEntry(e) },
                            onShare = { onShareMood(e) },
                            onDelete = { onDeleteEntry(e) }
                        )
                    }
                }
            }
        }
    }
}

/* ================= 来自 DiaryTab.kt ================= */

/**
 * 日记 Tab。
 *
 * 数据来自独立的 [DiaryEntry] 列表（diary.json），
 * 与主页的情绪记录 [com.example.mooddiary.data.MoodEntry] 完全无关。
 */
@Composable
internal fun DiaryTab(
    diaries: List<DiaryEntry>,
    isIos: Boolean,
    lang: String,
    onNew: () -> Unit,
    onEdit: (DiaryEntry) -> Unit
) {
    val recent7 = remember(diaries) {
        val since = System.currentTimeMillis() - 7 * Storage.DAY_MS
        diaries.count { it.dayStart >= since }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(4.dp))
        DiarySummaryCard(
            total = diaries.size,
            recent7 = recent7,
            isIos = isIos,
            lang = lang,
            onNew = onNew
        )

        if (diaries.isEmpty()) {
            DiaryEmptyState(lang, isIos, onNew)
        } else {
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(diaries, key = { it.id }) { d ->
                    DiaryListItem(
                        entry = d,
                        isIos = isIos,
                        lang = lang,
                        onClick = { onEdit(d) }
                    )
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

/* ================= 顶部统计卡 ================= */

@Composable
private fun DiarySummaryCard(
    total: Int,
    recent7: Int,
    isIos: Boolean,
    lang: String,
    onNew: () -> Unit
) {
    val cardBg = if (isIos) Color.White else MaterialTheme.colorScheme.surface
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isIos) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        L.t("diary_title", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = tc
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        String.format(L.t("diary_count", lang), total) + " · " +
                            String.format(L.t("diary_recent7", lang), recent7),
                        fontSize = 12.sp,
                        color = stc
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(primary.copy(alpha = 0.1f))
                        .clickable { onNew() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        L.t("diary_new", lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                }
            }
        }
    }
}

/* ================= 空状态 ================= */

@Composable
private fun DiaryEmptyState(lang: String, isIos: Boolean, onNew: () -> Unit) {
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📝", fontSize = 56.sp)
            Spacer(Modifier.height(14.dp))
            Text(
                L.t("diary_empty", lang),
                fontSize = 14.sp,
                color = stc,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(onClick = onNew) {
                Text(L.t("diary_new", lang))
            }
            Spacer(Modifier.height(64.dp))
        }
    }
}

/* ================= 单条日记 ================= */

@Composable
internal fun DiaryListItem(
    entry: DiaryEntry,
    isIos: Boolean,
    lang: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "diaryItemScale"
    )

    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    // 有情绪标签就用情绪配色，否则用主题色——标签只是为了好看，不依赖情绪记录
    val accent = entry.mood?.color() ?: primary
    val soft = entry.mood?.softColor() ?: primary.copy(alpha = 0.12f)
    val emoji = entry.mood?.emoji ?: "📖"
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isIos) Color(0xFFF5F5F7)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(soft),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                fullDateText(entry.dayStart, lang),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Spacer(Modifier.height(6.dp))
            Text(
                entry.content,
                fontSize = 13.sp,
                color = tc,
                maxLines = 4,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = stc,
            modifier = Modifier.size(18.dp)
        )
    }
}

