package com.example.mooddiary.ui

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.ExportHelper
import com.example.mooddiary.util.L
import com.example.mooddiary.util.SelfieCapture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloat

/* ================= 来自 MainScreen.kt ================= */

/**
 * 所有弹层 / 面板的状态收敛到一个密封类型。
 *
 * 同一时间只允许一个 Overlay 存在，
 * 因此不需要维护大量 showXxx = false。
 */
private sealed interface Overlay {
    data class Edit(val dayStart: Long) : Overlay
    data class Share(val entry: MoodEntry) : Overlay

    data object Menu : Overlay
    data object Stats : Overlay
    data object Settings : Overlay
    data object CardStyle : Overlay

    /**
     * 快速心情 BottomSheet。
     *
     * [photoUri] 非空时表示来自「瞬间」自拍，会在 Sheet 顶部展示缩略图，
     * 保存时一并写入 [MoodEntry.imageUri]。
     */
    data class QuickMood(val photoUri: String? = null) : Overlay

    /** 日记编辑器：id == null 表示新建 */
    data class DiaryEdit(val id: Long?) : Overlay
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MainScreen(
    vm: MainViewModel = viewModel()
) {
    val context = LocalContext.current

    // ============================================================
    // State
    // ============================================================

    val entries by vm.entries.collectAsState()
    val todayEntry by vm.todayEntry.collectAsState()
    val streak by vm.streak.collectAsState()

    val uiStyle by vm.uiStyle.collectAsState()
    val lang by vm.language.collectAsState()
    val appearance by vm.cardAppearance.collectAsState()
    val shareSettings by vm.shareSettings.collectAsState()

    val customMoods by vm.customMoods.collectAsState()
    val customMoodDefs by vm.customMoodDefs.collectAsState()

    /** 日记使用独立的数据流 */
    val diaries by vm.diaries.collectAsState()

    // ============================================================
    // Appearance
    // ============================================================
    // 显式标注为 Boolean，避免类型推断被 imports 里的同名符号污染
    // （之前出现过被 WideNavigationRailValue 覆盖的报错）

    val isIos: Boolean = uiStyle == "ios"

    val backgroundColor: Color =
        if (isIos) {
            Color(0xFFF5F5FA)
        } else {
            MaterialTheme.colorScheme.background
        }

    val primary: Color =
        if (isIos) {
            IosColor.primary
        } else {
            MaterialTheme.colorScheme.primary
        }

    // ============================================================
    // Navigation / Overlay
    // ============================================================

    val pagerState = rememberPagerState(
        pageCount = { 2 }
    )

    val scope = rememberCoroutineScope()

    var overlay by remember {
        mutableStateOf<Overlay?>(null)
    }

    // ------------------------------------------------------------
    // 「瞬间」自拍
    // ------------------------------------------------------------

    /** 拍照输出的临时文件 URI，拍完拿到结果后清空 */
    var pendingSelfieUri by remember { mutableStateOf<Uri?>(null) }

    val selfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = pendingSelfieUri
        pendingSelfieUri = null

        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            overlay = Overlay.QuickMood(uri.toString())
        }
    }

    /**
     * 「瞬间」入口：打开前置相机。
     *
     * 若设备没有相机应用，弹 Toast 提示。
     */
    val launchInstant: () -> Unit = remember {
        {
            val uri = SelfieCapture.createUri(context)
            pendingSelfieUri = uri
            val ok = SelfieCapture.launch(selfieLauncher, uri)
            if (!ok) {
                pendingSelfieUri = null
                Toast.makeText(
                    context,
                    if (lang == "zh") "无法打开相机" else "Camera unavailable",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 快速心情 BottomSheet（无照片入口）。
     */
    val openQuick: () -> Unit = remember {
        {
            overlay = Overlay.QuickMood(photoUri = null)
        }
    }

    /**
     * 完整心情编辑器。
     */
    val openToday: () -> Unit = remember {
        {
            overlay = Overlay.Edit(
                MainViewModel.todayStart()
            )
        }
    }

    // ============================================================
    // Main UI
    // ============================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        if (isIos) {
            IosBackground()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,

            topBar = {
                NotionHeader(
                    title = " 🍂",
                    subtitle = headerDate(lang),

                    onInstantClick = launchInstant,
                    onMomentClick = openToday,

                    onMenuClick = {
                        overlay =
                            if (overlay is Overlay.Menu) {
                                null
                            } else {
                                Overlay.Menu
                            }
                    },

                    moreLabel = L.t("menu_more", lang),

                    isIos = isIos,
                    lang = lang
                )
            },

            floatingActionButton = {

                val onFabClick: () -> Unit =
                    if (pagerState.currentPage == 1) {
                        {
                            overlay = Overlay.DiaryEdit(null)
                        }
                    } else {
                        launchInstant
                    }

                if (isIos) {
                    IosFab(onClick = onFabClick)
                } else {
                    MaterialFab(onClick = onFabClick)
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                MainTabs(
                    currentPage = pagerState.currentPage,
                    isIos = isIos,
                    primary = primary,
                    lang = lang,

                    onSelect = { page ->
                        scope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    beyondViewportPageCount = 1
                ) { page ->

                    when (page) {

                        0 -> {
                            HomeTab(
                                todayEntry = todayEntry,
                                entries = entries,
                                streak = streak,

                                appearance = appearance,

                                isIos = isIos,
                                lang = lang,

                                customs = customMoods,
                                customMoodDefs = customMoodDefs,

                                onEditMood = openToday,

                                onShareMood = {
                                    overlay = Overlay.Share(it)
                                },

                                onEditEntry = {
                                    overlay = Overlay.Edit(it.dayStart)
                                },

                                onDeleteEntry = {
                                    vm.deleteEntry(it.id)
                                }
                            )
                        }

                        1 -> {
                            DiaryTab(
                                diaries = diaries,

                                isIos = isIos, lang = lang,

                                onNew = {
                                    overlay = Overlay.DiaryEdit(null)
                                },

                                onEdit = {
                                    overlay = Overlay.DiaryEdit(it.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ============================================================
        // Menu Scrim
        // ============================================================

        AnimatedVisibility(
            visible = overlay is Overlay.Menu,

            enter = fadeIn(animationSpec = tween(140)),
            exit = fadeOut(animationSpec = tween(120)),

            modifier = Modifier
                .fillMaxSize()
                .zIndex(99f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha = if (isIos) 0.06f else 0.10f
                        )
                    )
                    .clickable {
                        overlay = null
                    }
            )
        }

        // ============================================================
        // Top Menu
        // ============================================================

        AnimatedVisibility(
            visible = overlay is Overlay.Menu,

            enter =
                fadeIn(animationSpec = tween(150)) +
                        scaleIn(
                            initialScale = 0.92f,
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 1f,
                                pivotFractionY = 0f
                            ),
                            animationSpec = tween(
                                durationMillis = 220,
                                easing = FastOutSlowInEasing
                            )
                        ),

            exit =
                fadeOut(animationSpec = tween(100)) +
                        scaleOut(
                            targetScale = 0.94f,
                            transformOrigin = TransformOrigin(
                                pivotFractionX = 1f,
                                pivotFractionY = 0f
                            ),
                            animationSpec = tween(durationMillis = 140)
                        ),

            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 84.dp, end = 16.dp)
                .zIndex(100f)
        ) {
            TopMenuPanel(
                isIos = isIos,
                lang = lang,

                onStats = { overlay = Overlay.Stats },
                onOpenCardStyle = { overlay = Overlay.CardStyle },

                onBackfill = {
                    overlay = Overlay.Edit(
                        MainViewModel.todayStart() - Storage.DAY_MS
                    )
                },

                onSettings = { overlay = Overlay.Settings }
            )
        }
    }

    // ================================================================
    // Dialogs
    // ================================================================

    // ------------------------------------------------
    // Quick Mood (BottomSheet)
    // ------------------------------------------------

    val quickState = overlay as? Overlay.QuickMood

    if (quickState != null) {
        QuickMoodSheet(
            existingMood = todayEntry?.mood,
            photoUri = quickState.photoUri,
            isIos = isIos,
            lang = lang,
            customs = customMoods,

            onPick = { mood ->
                vm.saveEntry(
                    mood = mood,
                    intensity = 3,
                    note = "",
                    dayStart = MainViewModel.todayStart(),
                    imageUri = quickState.photoUri,
                    customMoodId = null
                )
                overlay = null
            },

            onOpenFull = {
                overlay = Overlay.Edit(MainViewModel.todayStart())
            },

            onDismiss = {
                overlay = null
            }
        )
    }

    // ------------------------------------------------
    // Mood Edit (BottomSheet)
    // ------------------------------------------------

    val editState = overlay as? Overlay.Edit

    if (editState != null) {
        EditDialog(
            initialDay = editState.dayStart,

            isIos = isIos, lang = lang,

            customs = customMoods,
            customMoodDefs = customMoodDefs,

            findExisting = { vm.findEntry(it) },

            onCustomSave = { mood, style ->
                vm.setCustomMood(mood, style)
            },

            onAddCustomMood = { emoji, label ->
                vm.addCustomMoodDef(emoji, label)
            },

            onDeleteCustomMood = { id ->
                vm.deleteCustomMoodDef(id)
            },

            onDismiss = { overlay = null },

            onSave = {
                    mood, intensity, note, savedDay, imageUri, customMoodId ->

                vm.saveEntry(
                    mood, intensity, note,
                    savedDay, imageUri, customMoodId
                )

                overlay = null
            },

            onDelete = { dayStart ->
                val e = vm.findEntry(dayStart)
                if (e != null) {
                    vm.deleteEntry(e.id)
                    overlay = null
                }
            }
        )
    }

    // ------------------------------------------------
    // Card Style
    // ------------------------------------------------

    AnimatedDialog(
        visible = overlay is Overlay.CardStyle
    ) {
        CardStyleSheet(
            appearance = appearance,
            shareSettings = shareSettings,

            isIos = isIos, lang = lang,

            onAppearanceChange = { vm.updateCardAppearance(it) },
            onShareSettingsChange = { vm.updateShareSettings(it) },
            onDismiss = { overlay = null }
        )
    }

    // ------------------------------------------------
    // Share
    // ------------------------------------------------

    val shareState = overlay as? Overlay.Share

    AnimatedDialog(visible = shareState != null) {
        shareState?.let { state ->
            ShareDialog(
                entry = state.entry,

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

    // ------------------------------------------------
    // Statistics
    // ------------------------------------------------

    AnimatedDialog(
        visible = overlay is Overlay.Stats
    ) {
        StatsDialog(
            entries = entries,

            isIos = isIos, lang = lang,

            customs = customMoods,
            customMoodDefs = customMoodDefs,

            onDismiss = { overlay = null }
        )
    }

    // ------------------------------------------------
    // Settings
    // ------------------------------------------------

    AnimatedDialog(
        visible = overlay is Overlay.Settings
    ) {
        SettingsDialog(
            entries = entries,

            isIos = isIos, lang = lang,

            onToggleStyle = {
                vm.setUiStyle(if (isIos) "material" else "ios")
            },

            onPickLanguage = { vm.setLanguage(it) },

            onExport = {
                ExportHelper.exportCsv(context, entries)
            },

            onClearAll = { vm.clearAll() },

            onDismiss = { overlay = null }
        )
    }

    // ------------------------------------------------
    // Diary Editor
    // ------------------------------------------------

    val diaryEditState = overlay as? Overlay.DiaryEdit

    AnimatedDialog(
        visible = diaryEditState != null
    ) {
        diaryEditState?.let { state ->

            DiaryEditorDialog(
                initial = state.id?.let { vm.findDiary(it) },

                isIos = isIos,
                lang = lang,

                customs = customMoods,

                onSave = { content, dayStart, imageUri, mood ->
                    vm.saveDiary(
                        id = state.id,
                        content = content,
                        dayStart = dayStart,
                        imageUri = imageUri,
                        mood = mood
                    )
                    overlay = null
                },

                onDelete =
                    if (state.id != null) {
                        {
                            vm.deleteDiary(state.id)
                            overlay = null
                        }
                    } else {
                        null
                    },

                onDismiss = { overlay = null }
            )
        }
    }
}

/* ================================================================
 * iOS Background
 * ================================================================ */

@Composable
private fun IosBackground() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFB0CEFF).copy(alpha = 0.72f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.08f,
                    size.height * 0.06f
                ),
                radius = size.width * 1.45f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFC0DB).copy(alpha = 0.62f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.96f,
                    size.height * 0.18f
                ),
                radius = size.width * 1.30f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFCFBDFF).copy(alpha = 0.54f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.03f,
                    size.height * 0.78f
                ),
                radius = size.width * 1.40f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFA8ECD5).copy(alpha = 0.48f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 1.00f,
                    size.height * 0.94f
                ),
                radius = size.width * 1.20f
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFE0B2).copy(alpha = 0.32f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * 0.58f,
                    size.height * 0.48f
                ),
                radius = size.width * 1.10f
            )
        )
    }
}

/* ================================================================
 * iOS FAB
 * ================================================================ */

@Composable
private fun IosFab(
    onClick: () -> Unit
) {
    var fabVisible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(120)
        fabVisible = true
    }

    val fabScale by animateFloatAsState(
        targetValue = if (fabVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "fabScale"
    )

    val fabElevation by animateDpAsState(
        targetValue = if (fabVisible) 8.dp else 0.dp,
        animationSpec = tween(durationMillis = 260),
        label = "fabElevation"
    )

    Box(
        modifier = Modifier
            .size(62.dp)
            .graphicsLayer {
                scaleX = fabScale
                scaleY = fabScale
            }
            .shadow(
                elevation = fabElevation,
                shape = CircleShape
            )
            .clip(CircleShape)
            .liquidGlassPro(
                CircleShape,
                IosColor.primary.copy(alpha = 0.86f)
            )
            .clickable(onClick = onClick),

        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add",
            tint = Color.White,
            modifier = Modifier.size(29.dp)
        )
    }
}

/* ================================================================
 * Material FAB
 * ================================================================ */

@Composable
private fun MaterialFab(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,

        modifier = Modifier.size(58.dp),

        shape = RoundedCornerShape(18.dp),

        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,

        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 8.dp
        )
    ) {

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "add",
            modifier = Modifier.size(28.dp)
        )
    }
}

/* ================= 来自 ScreenChrome.kt ================= */

@Composable
internal fun AnimatedDialog(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            targetScale = 0.85f,
            animationSpec = tween(150)
        )
    ) {
        content()
    }
}


@Composable
internal fun EmptyState(
    lang: String,
    isIos: Boolean,
    onAddClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")

    val breath by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "🌱",
                fontSize = 56.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = breath
                    scaleY = breath
                }
            )

            Spacer(Modifier.height(12.dp))

            Text(
                L.t("empty_hint", lang),
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
            ) {

                Icon(
                    Icons.Default.Add,
                    null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    if (lang == "zh") "记录今天" else "Record Today",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}