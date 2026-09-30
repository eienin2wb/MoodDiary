package com.example.mooddiary.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.ExportHelper
import com.example.mooddiary.util.L
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 所有弹层 / 面板的状态收敛到一个密封类型。
 * 打开任意一个都自动关闭其它,不再需要手写一堆 showXxx = false。
 */
private sealed interface Overlay {
    data class Edit(val dayStart: Long) : Overlay
    data class Share(val entry: MoodEntry) : Overlay
    object Menu : Overlay
    object Stats : Overlay
    object Settings : Overlay
    object CardStyle : Overlay
    object DiaryList : Overlay
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current

    val entries by vm.entries.collectAsState()
    val todayEntry by vm.todayEntry.collectAsState()
    val streak by vm.streak.collectAsState()
    val uiStyle by vm.uiStyle.collectAsState()
    val lang by vm.language.collectAsState()
    val appearance by vm.cardAppearance.collectAsState()
    val shareSettings by vm.shareSettings.collectAsState()
    val customMoods by vm.customMoods.collectAsState()
    val customMoodDefs by vm.customMoodDefs.collectAsState()

    // 修复: 真正使用 uiStyle,而不是写死 false
    val isIos = uiStyle == "ios"

    var overlay by remember { mutableStateOf<Overlay?>(null) }

    val openToday: () -> Unit = remember {
        { overlay = Overlay.Edit(MainViewModel.todayStart()) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isIos) Color(0xFFF5F5FA) else MaterialTheme.colorScheme.background)
    ) {
        if (isIos) IosBackground()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotionHeader(
                    title = "Cmpss",
                    subtitle = headerDate(lang),
                    onInstantClick = openToday,
                    onMomentClick = openToday,
                    onMenuClick = {
                        overlay = if (overlay is Overlay.Menu) null else Overlay.Menu
                    },
                    moreLabel = L.t("menu_more", lang),
                    isIos = isIos,
                    lang = lang
                )
            },
            floatingActionButton = {
                if (isIos) IosFab(onClick = openToday)
                else MaterialFab(onClick = openToday)
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(4.dp))

                MoodDiaryPager(
                    todayEntry = todayEntry,
                    entries = entries,
                    appearance = appearance,
                    isIos = isIos,
                    lang = lang,
                    customs = customMoods,
                    customMoodDefs = customMoodDefs,
                    onEditMood = openToday,
                    onShareMood = { todayEntry?.let { overlay = Overlay.Share(it) } },
                    onViewAllDiaries = { overlay = Overlay.DiaryList },
                    onDiaryClick = { overlay = Overlay.Edit(it.dayStart) }
                )

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
                    EmptyState(lang, isIos, onAddClick = openToday)
                } else {
                    // 月份格式按语言选择,不再写死 Locale.CHINA
                    val monthSdf = rememberMonthFormatter(lang)

                    val groups = remember(entries, monthSdf) {
                        entries.groupBy { monthSdf.format(Date(it.dayStart)) }.toList()
                    }
                    // 用 Set<Long> 代替 Map<Long, Boolean>,判断更轻
                    val firstIds = remember(groups) {
                        buildSet {
                            groups.forEach { (_, ms) -> ms.firstOrNull()?.let { add(it.id) } }
                        }
                    }
                    val lastIds = remember(groups) {
                        buildSet {
                            groups.forEach { (_, ms) -> ms.lastOrNull()?.let { add(it.id) } }
                        }
                    }

                    LazyColumn(
                        // 关键: 拿剩余空间,避免被上面变高的 Pager 挤没
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        groups.forEach { (monthLabel, monthEntries) ->
                            item(key = "header_$monthLabel") {
                                MonthHeader(
                                    monthLabel, monthEntries, isIos, lang,
                                    customMoods, customMoodDefs
                                )
                            }
                            items(monthEntries, key = { it.id }) { e ->
                                TimelineEntryRow(
                                    entry = e,
                                    isFirst = e.id in firstIds,
                                    isLast = e.id in lastIds,
                                    isIos = isIos,
                                    lang = lang,
                                    customs = customMoods,
                                    customMoodDefs = customMoodDefs,
                                    appearance = appearance,
                                    onEdit = { overlay = Overlay.Edit(e.dayStart) },
                                    onShare = { overlay = Overlay.Share(e) },
                                    onDelete = { vm.deleteEntry(e.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 菜单遮罩 ────────────────────────────────────
        AnimatedVisibility(
            visible = overlay is Overlay.Menu,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(99f)
        ) {
            Box(Modifier.fillMaxSize().clickable { overlay = null })
        }

        // ── 菜单面板 ────────────────────────────────────
        AnimatedVisibility(
            visible = overlay is Overlay.Menu,
            enter = fadeIn(tween(160)) + scaleIn(
                initialScale = 0.7f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(220, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(120)) + scaleOut(
                targetScale = 0.7f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(150)
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 88.dp, end = 16.dp)
                .zIndex(100f)
        ) {
            TopMenuPanel(
                isIos = isIos,
                lang = lang,
                onStats = { overlay = Overlay.Stats },
                onOpenCardStyle = { overlay = Overlay.CardStyle },
                onBackfill = { overlay = Overlay.Edit(MainViewModel.todayStart() - Storage.DAY_MS) },
                onSettings = { overlay = Overlay.Settings }
            )
        }
    }

    // ── 各种弹层 ────────────────────────────────────────

    AnimatedDialog(visible = overlay is Overlay.CardStyle) {
        CardStyleSheet(
            appearance = appearance,
            shareSettings = shareSettings,
            isIos = isIos,
            lang = lang,
            onAppearanceChange = { vm.updateCardAppearance(it) },
            onShareSettingsChange = { vm.updateShareSettings(it) },
            onDismiss = { overlay = null }
        )
    }

    val editState = overlay as? Overlay.Edit
    AnimatedDialog(visible = editState != null) {
        editState?.let { s ->
            EditDialog(
                initialDay = s.dayStart,
                isIos = isIos,
                lang = lang,
                customs = customMoods,
                customMoodDefs = customMoodDefs,
                findExisting = { vm.findEntry(it) },
                onCustomSave = { m, style -> vm.setCustomMood(m, style) },
                onAddCustomMood = { e, l -> vm.addCustomMoodDef(e, l) },
                onDeleteCustomMood = { id -> vm.deleteCustomMoodDef(id) },
                onDismiss = { overlay = null },
                onSave = { mood, intensity, note, savedDay, imageUri, customMoodId ->
                    vm.saveEntry(mood, intensity, note, savedDay, imageUri, customMoodId)
                    overlay = null
                }
            )
        }
    }

    val shareState = overlay as? Overlay.Share
    AnimatedDialog(visible = shareState != null) {
        shareState?.let { s ->
            ShareDialog(
                entry = s.entry,
                settings = shareSettings,
                cardAppearance = appearance,
                lang = lang,
                customs = customMoods,
                customMoodDefs = customMoodDefs,
                onSettingsChange = { vm.updateShareSettings(it) },
                onDismiss = { overlay = null }
            )
        }
    }

    AnimatedDialog(visible = overlay is Overlay.Stats) {
        StatsDialog(
            entries = entries,
            isIos = isIos,
            lang = lang,
            customs = customMoods,
            customMoodDefs = customMoodDefs,
            onDismiss = { overlay = null }
        )
    }

    AnimatedDialog(visible = overlay is Overlay.Settings) {
        SettingsDialog(
            entries = entries,
            isIos = isIos,
            lang = lang,
            // isIos 现在真实反映当前风格,切回 material 才有效
            onToggleStyle = { vm.setUiStyle(if (isIos) "material" else "ios") },
            onPickLanguage = { vm.setLanguage(it) },
            onExport = { ExportHelper.exportCsv(context, entries) },
            onClearAll = { vm.clearAll() },
            onDismiss = { overlay = null }
        )
    }

    AnimatedDialog(visible = overlay is Overlay.DiaryList) {
        DiaryListDialog(
            entries = entries,
            isIos = isIos,
            lang = lang,
            customs = customMoods,
            customMoodDefs = customMoodDefs,
            onEntryClick = { overlay = Overlay.Edit(it.dayStart) },
            onDismiss = { overlay = null }
        )
    }
}

/* ================= 抽出来的小组件 ================= */

@Composable
private fun rememberMonthFormatter(lang: String): SimpleDateFormat = remember(lang) {
    val cjk = lang.startsWith("zh") || lang.startsWith("ja")
    val locale = if (cjk) Locale.CHINA else Locale.getDefault()
    val pattern = if (cjk) "yyyy年M月" else "MMMM yyyy"
    SimpleDateFormat(pattern, locale)
}

@Composable
private fun IosBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.radialGradient(
            colors = listOf(Color(0xFFB0CEFF), Color.Transparent),
            center = Offset(size.width * 0.1f, size.height * 0.08f),
            radius = size.width * 1.3f))
        drawRect(brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFC0DB), Color.Transparent),
            center = Offset(size.width * 0.95f, size.height * 0.22f),
            radius = size.width * 1.1f))
        drawRect(brush = Brush.radialGradient(
            colors = listOf(Color(0xFFCFBDFF), Color.Transparent),
            center = Offset(size.width * 0.05f, size.height * 0.75f),
            radius = size.width * 1.2f))
        drawRect(brush = Brush.radialGradient(
            colors = listOf(Color(0xFFA8ECD5), Color.Transparent),
            center = Offset(size.width * 0.98f, size.height * 0.92f),
            radius = size.width * 1.0f))
        drawRect(brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFE0B2), Color.Transparent),
            center = Offset(size.width * 0.6f, size.height * 0.5f),
            radius = size.width * 0.9f))
    }
}

@Composable
private fun IosFab(onClick: () -> Unit) {
    var fabVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(150)
        fabVisible = true
    }
    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow),
        label = "fabScale"
    )
    Box(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            }
            .liquidGlassPro(CircleShape, IosColor.primary.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "add",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun MaterialFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "add",
            modifier = Modifier.size(28.dp)
        )
    }
}