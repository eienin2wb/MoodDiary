package com.example.mooddiary.ui

import android.app.DatePickerDialog as AndroidDatePicker
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.ExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar as JavaCal
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current

    val entries by vm.entries.collectAsState()
    val todayEntry by vm.todayEntry.collectAsState()
    val streak by vm.streak.collectAsState()
    val cardStyle by vm.cardStyle.collectAsState()

    var editingDay by remember { mutableStateOf<Long?>(null) }
    var sharingEntry by remember { mutableStateOf<MoodEntry?>(null) }
    var showStats by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MooooodDiary", fontWeight = FontWeight.Bold) },
                actions = {
                    // 情绪统计
                    IconButton(onClick = { showStats = true }) {
                        Icon(Icons.Default.BarChart, contentDescription = "统计")
                    }
                    // 切换卡片样式
                    IconButton(onClick = {
                        vm.setCardStyle(if (cardStyle == "plain") "gradient" else "plain")
                    }) {
                        Icon(Icons.Default.Palette, contentDescription = "切换卡片样式")
                    }
                    // 补录历史
                    IconButton(onClick = {
                        editingDay = MainViewModel.todayStart() - Storage.DAY_MS
                    }) {
                        Icon(Icons.Default.History, contentDescription = "补录历史")
                    }
                    // 设置
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingDay = MainViewModel.todayStart()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "记录今天",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Spacer(Modifier.height(8.dp))

            TodayCard(
                entry = todayEntry,
                cardStyle = cardStyle,
                onEdit = { editingDay = MainViewModel.todayStart() },
                onShare = { todayEntry?.let { sharingEntry = it } }
            )

            Spacer(Modifier.height(12.dp))

            StreakRow(streak)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "历史记录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "共 ${entries.size} 条",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "还没有记录，点右下角 ＋ 开始",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(entries, key = { it.id }) { e ->
                        EntryRow(
                            entry = e,
                            onEdit = { editingDay = e.dayStart },
                            onShare = { sharingEntry = e },
                            onDelete = { vm.deleteEntry(e.id) }
                        )
                    }
                }
            }
        }
    }

    // ---- 编辑弹窗 ----
    editingDay?.let { day ->
        EditDialog(
            initialDay = day,
            findExisting = { vm.findEntry(it) },
            onDismiss = { editingDay = null },
            onSave = { mood, intensity, note, savedDay ->
                vm.saveEntry(mood, intensity, note, savedDay)
                editingDay = null
            }
        )
    }

    // ---- 分享弹窗 ----
    sharingEntry?.let { entry ->
        ShareDialog(
            entry = entry,
            onDismiss = { sharingEntry = null }
        )
    }

    // ---- 统计弹窗 ----
    if (showStats) {
        StatsDialog(
            entries = entries,
            onDismiss = { showStats = false }
        )
    }

    // ---- 设置弹窗 ----
    if (showSettings) {
        SettingsDialog(
            entries = entries,
            onExport = {
                ExportHelper.exportCsv(context, entries)
            },
            onClearAll = {
                vm.clearAll()
            },
            onDismiss = { showSettings = false }
        )
    }
}

/* ================= 今日卡片 ================= */

@Composable
private fun TodayCard(
    entry: MoodEntry?,
    cardStyle: String,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    // 按压缩放动画
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "emojiScale"
    )

    // ============ 纯色模式（默认） ============
    if (cardStyle == "plain") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onEdit
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            TodayCardContent(
                entry = entry,
                scale = scale,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                accentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onShare = onShare
            )
        }
    }
    // ============ 渐变模式 ============
    else {
        val gradient = entry?.mood?.gradientColors() ?: defaultGradient
        val textColor = entry?.mood?.color() ?: MaterialTheme.colorScheme.onSurface

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(gradient))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onEdit
                )
        ) {
            TodayCardContent(
                entry = entry,
                scale = scale,
                textColor = textColor,
                accentColor = textColor,
                onShare = onShare
            )
        }
    }
}

/* ---- TodayCard 内部内容（两种模式共用） ---- */
@Composable
private fun TodayCardContent(
    entry: MoodEntry?,
    scale: Float,
    textColor: Color,
    accentColor: Color,
    onShare: () -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "今天 · ${SimpleDateFormat("M月d日", Locale.getDefault()).format(Date())}",
                style = MaterialTheme.typography.labelLarge,
                color = textColor
            )
            Spacer(Modifier.weight(1f))
            if (entry != null) {
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "分享",
                        tint = accentColor
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (entry == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("😶", fontSize = 48.sp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "还没记录今天",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        "点这里记录此刻的心情",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.mood.emoji,
                    fontSize = 56.sp,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.mood.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        starString(entry.intensity),
                        fontSize = 18.sp,
                        color = accentColor
                    )
                }
            }
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "「${entry.note}」",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

/* ================= 连续打卡 ================= */

@Composable
private fun StreakRow(streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 20.sp)
        Spacer(Modifier.width(6.dp))
        if (streak > 0) {
            Text(
                "你好棒！已连续记录 $streak 天啦",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                "今天还没开始记录呢",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ================= 单条历史 ================= */

@Composable
private fun EntryRow(
    entry: MoodEntry,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = SimpleDateFormat("M月d日 EEEE", Locale.CHINA)
        .format(Date(entry.dayStart))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---- 左侧色条 ----
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(entry.mood.color(), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))

            // ---- emoji 圆 ----
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(entry.mood.softColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.mood.emoji, fontSize = 26.sp)
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.mood.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = entry.mood.color()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        starString(entry.intensity),
                        fontSize = 12.sp,
                        color = entry.mood.color()
                    )
                }
                Text(
                    dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.note,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onShare) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "分享",
                    tint = entry.mood.color()
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/* ================= 编辑弹窗 ================= */

@Composable
private fun EditDialog(
    initialDay: Long,
    findExisting: (Long) -> MoodEntry?,
    onDismiss: () -> Unit,
    onSave: (Mood, Int, String, Long) -> Unit
) {
    val context = LocalContext.current

    var selectedDay by remember { mutableLongStateOf(initialDay) }

    val existing = findExisting(selectedDay)

    var mood by remember(selectedDay) { mutableStateOf(existing?.mood ?: Mood.HAPPY) }
    var intensity by remember(selectedDay) {
        mutableFloatStateOf((existing?.intensity ?: 3).toFloat())
    }
    var note by remember(selectedDay) { mutableStateOf(existing?.note ?: "") }

    fun showDatePicker() {
        val c = JavaCal.getInstance().apply { timeInMillis = selectedDay }
        AndroidDatePicker(
            context,
            { _, y, m, d ->
                val newCal = JavaCal.getInstance().apply {
                    set(JavaCal.YEAR, y)
                    set(JavaCal.MONTH, m)
                    set(JavaCal.DAY_OF_MONTH, d)
                    set(JavaCal.HOUR_OF_DAY, 0)
                    set(JavaCal.MINUTE, 0)
                    set(JavaCal.SECOND, 0)
                    set(JavaCal.MILLISECOND, 0)
                }
                selectedDay = newCal.timeInMillis
            },
            c.get(JavaCal.YEAR),
            c.get(JavaCal.MONTH),
            c.get(JavaCal.DAY_OF_MONTH)
        ).show()
    }

    val isToday = selectedDay == MainViewModel.todayStart()
    val dateLabel = if (isToday) "今天"
    else SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(Date(selectedDay))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existing == null) "记录个心情吧" else "编辑记录")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { },
                    label = { Text("日期") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker() }) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "选择日期"
                            )
                        }
                    }
                )

                Text("啥心情呢", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Mood.values().forEach { m ->
                        val selected = m == mood
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) m.softColor() else Color.Transparent
                                )
                                .clickable { mood = m }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(m.emoji, fontSize = 28.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                m.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold
                                else FontWeight.Normal,
                                color = if (selected) m.color()
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Text(
                    "程度  ${starString(intensity.toInt())}",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 1f..5f,
                    steps = 3
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("发生了什么？（可选）") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isToday && existing == null) {
                    Text(
                        "将在 $dateLabel 补录一条记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(mood, intensity.toInt(), note.trim(), selectedDay)
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

/* ================= 月度情绪统计 ================= */

data class MoodStat(val mood: Mood, val count: Int, val percent: Float)

/** 计算本月的情绪分布，按次数降序 */
private fun calcMonthlyStats(entries: List<MoodEntry>): List<MoodStat> {
    val cal = JavaCal.getInstance()
    val y = cal.get(JavaCal.YEAR)
    val m = cal.get(JavaCal.MONTH)

    val monthEntries = entries.filter {
        val c = JavaCal.getInstance().apply { timeInMillis = it.dayStart }
        c.get(JavaCal.YEAR) == y && c.get(JavaCal.MONTH) == m
    }

    val total = monthEntries.size.toFloat()
    if (total == 0f) {
        return Mood.values().map { MoodStat(it, 0, 0f) }
    }
    return Mood.values().map { mood ->
        val count = monthEntries.count { it.mood == mood }
        MoodStat(mood, count, count / total)
    }.sortedByDescending { it.count }
}

@Composable
private fun StatsDialog(
    entries: List<MoodEntry>,
    onDismiss: () -> Unit
) {
    val stats = calcMonthlyStats(entries)
    val total = stats.sumOf { it.count }
    val cal = JavaCal.getInstance()
    val monthLabel = "${cal.get(JavaCal.YEAR)}年${cal.get(JavaCal.MONTH) + 1}月"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("情绪统计 · $monthLabel") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "本月记录 $total 天",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (total == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "本月还没有记录呢 🌱",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    stats.forEach { stat ->
                        StatBar(stat)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun StatBar(stat: MoodStat) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.mood.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                stat.mood.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${stat.count} 次  ${(stat.percent * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(stat.percent.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(stat.mood.color())
            )
        }
    }
}

/* ================= 设置弹窗 ================= */

@Composable
private fun SettingsDialog(
    entries: List<MoodEntry>,
    onExport: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SettingRow(
                    emoji = "📤",
                    title = "导出数据（CSV）",
                    subtitle = "共 ${entries.size} 条记录，可发到微信/抖音，还可将导入AI智能分析",
                    onClick = onExport
                )

                HorizontalDivider()

                SettingRow(
                    emoji = "🗑️",
                    title = "清空所有数据",
                    subtitle = "不可恢复，请谨慎操作",
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { showClearConfirm = true }
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "关于",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "每日情绪账本 v1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "记录每一天的心情，生成精美卡片",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "本软件由魏文彬开发" +
                                "  ©Cmpss 2026"
                        ,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("确定清空所有数据？") },
            text = {
                Text("将永久删除全部 ${entries.size} 条记录，不可恢复。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("我真的确定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("等等！我再想想")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    emoji: String,
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ================= 工具 ================= */

/** 把 1~5 的强度转成 ★★★☆☆ */
private fun starString(n: Int): String {
    val c = n.coerceIn(0, 5)
    return "★".repeat(c) + "☆".repeat(5 - c)
}

/* ================= 情绪配色 ================= */

/** 每种情绪的主色（文字、色条用） */
fun Mood.color(): Color = when (this) {
    Mood.HAPPY   -> Color(0xFFF5A623)
    Mood.CALM    -> Color(0xFF4CAF93)
    Mood.SAD     -> Color(0xFF5B7DB1)
    Mood.ANGRY   -> Color(0xFFE05353)
    Mood.ANXIOUS -> Color(0xFF9C6BC7)
}

/** 每种情绪的浅色底（emoji 圆、情绪按钮背景用） */
fun Mood.softColor(): Color = when (this) {
    Mood.HAPPY   -> Color(0xFFFFF3DC)
    Mood.CALM    -> Color(0xFFE0F4F0)
    Mood.SAD     -> Color(0xFFE4ECF7)
    Mood.ANGRY   -> Color(0xFFFFE5E5)
    Mood.ANXIOUS -> Color(0xFFF2E4FA)
}

/** 情绪对应的渐变起止色（顶部→底部），用于顶部大卡片 */
fun Mood.gradientColors(): List<Color> = when (this) {
    Mood.HAPPY   -> listOf(Color(0xFFFFE4A1), Color(0xFFFFC06C))
    Mood.CALM    -> listOf(Color(0xFFD5F3EE), Color(0xFF9CD9CE))
    Mood.SAD     -> listOf(Color(0xFFDCE7F7), Color(0xFF9AB4D9))
    Mood.ANGRY   -> listOf(Color(0xFFFFDCDC), Color(0xFFF19191))
    Mood.ANXIOUS -> listOf(Color(0xFFEEDCFA), Color(0xFFC09DE0))
}

/** 没记录今天时的默认灰蓝渐变 */
val defaultGradient: List<Color> = listOf(Color(0xFFE8EAF6), Color(0xFFBFC5E0))