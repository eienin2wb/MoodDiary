package com.example.mooddiary.ui

import android.app.DatePickerDialog as AndroidDatePicker
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.ExportHelper
import kotlinx.coroutines.delay
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
    val uiStyle by vm.uiStyle.collectAsState()
    val appearance by vm.cardAppearance.collectAsState()
    val shareSettings by vm.shareSettings.collectAsState()
    val isIos = uiStyle == "ios"

    var editingDay by remember { mutableStateOf<Long?>(null) }
    var sharingEntry by remember { mutableStateOf<MoodEntry?>(null) }
    var showStats by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showCardStyleSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (isIos) Color(0xFFF5F5FA)
                else MaterialTheme.colorScheme.background
            )
    ) {
        if (isIos) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB8D0FF), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.1f),
                        radius = size.width * 1.2f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFC7DD), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.25f),
                        radius = size.width * 1.0f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFD5C7FF), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.8f),
                        radius = size.width * 1.1f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB8F0E0), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.95f),
                        radius = size.width * 0.9f
                    )
                )
            }
        }

        Scaffold(
            containerColor = if (isIos) Color.Transparent
            else MaterialTheme.colorScheme.background,
            topBar = {
                if (isIos) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "MooooodDiary",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosColor.textPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )
                            GlassIconButton(Icons.Default.MoreHoriz, "更多") {
                                showMenu = !showMenu
                            }
                        }
                    }
                } else {
                    TopAppBar(
                        title = { Text("MooooodDiary", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "更多")
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (isIos) {
                    var fabVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { delay(150); fabVisible = true }
                    val fabScale by animateFloatAsState(
                        targetValue = if (fabVisible) 1f else 0f,
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "fabScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .graphicsLayer {
                                scaleX = fabScale
                                scaleY = fabScale
                            }
                            .liquidGlass(
                                shape = CircleShape,
                                tint = IosColor.primary.copy(alpha = 0.85f)
                            )
                            .clickable { editingDay = MainViewModel.todayStart() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "记录今天",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    FloatingActionButton(
                        onClick = { editingDay = MainViewModel.todayStart() },
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(4.dp))

                AnimatedContent(
                    targetState = appearance.background,
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
                ) { background ->
                    TodayCard(
                        entry = todayEntry,
                        appearance = appearance.copy(background = background),
                        isIos = isIos,
                        onEdit = { editingDay = MainViewModel.todayStart() },
                        onShare = { todayEntry?.let { sharingEntry = it } }
                    )
                }

                Spacer(Modifier.height(8.dp))
                StreakRow(streak, isIos)

                if (isIos) {
                    IosSectionHeader(
                        text = "历史记录",
                        trailing = "共 ${entries.size} 条"
                    )
                } else {
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
                }

                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌱", fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "还没有记录，点右下角 ＋ 开始",
                                color = if (isIos) IosColor.textSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                        items(entries, key = { it.id }) { e ->
                            EntryRow(
                                entry = e,
                                isFirst = e == entries.first(),
                                isLast = e == entries.last(),
                                isIos = isIos,
                                onEdit = { editingDay = e.dayStart },
                                onShare = { sharingEntry = e },
                                onDelete = { vm.deleteEntry(e.id) }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150)),
            modifier = Modifier.fillMaxSize().zIndex(99f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = false }
            )
        }

        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(160)) + scaleIn(
                initialScale = 0.7f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(200, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(tween(120)) + scaleOut(
                targetScale = 0.7f,
                transformOrigin = TransformOrigin(1f, 0f),
                animationSpec = tween(150)
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 56.dp, end = 12.dp)
                .zIndex(100f)
        ) {
            TopMenuPanel(
                isIos = isIos,
                onStats = { showMenu = false; showStats = true },
                onOpenCardStyle = { showMenu = false; showCardStyleSheet = true },
                onBackfill = {
                    showMenu = false
                    editingDay = MainViewModel.todayStart() - Storage.DAY_MS
                },
                onSettings = { showMenu = false; showSettings = true }
            )
        }
    }

    if (showCardStyleSheet) {
        CardStyleSheet(
            appearance = appearance,
            shareSettings = shareSettings,
            isIos = isIos,
            onAppearanceChange = { vm.updateCardAppearance(it) },
            onShareSettingsChange = { vm.updateShareSettings(it) },
            onDismiss = { showCardStyleSheet = false }
        )
    }

    editingDay?.let { day ->
        EditDialog(
            initialDay = day,
            isIos = isIos,
            findExisting = { vm.findEntry(it) },
            onDismiss = { editingDay = null },
            onSave = { mood, intensity, note, savedDay ->
                vm.saveEntry(mood, intensity, note, savedDay)
                editingDay = null
            }
        )
    }

    sharingEntry?.let { entry ->
        ShareDialog(
            entry = entry,
            settings = shareSettings,
            cardAppearance = appearance,
            onSettingsChange = { vm.updateShareSettings(it) },
            onDismiss = { sharingEntry = null }
        )
    }

    if (showStats) {
        StatsDialog(entries = entries, isIos = isIos, onDismiss = { showStats = false })
    }

    if (showSettings) {
        SettingsDialog(
            entries = entries,
            isIos = isIos,
            onToggleStyle = {
                vm.setUiStyle(if (isIos) "material" else "ios")
            },
            onExport = { ExportHelper.exportCsv(context, entries) },
            onClearAll = { vm.clearAll() },
            onDismiss = { showSettings = false }
        )
    }
}

/* ================= 顶栏下拉菜单 ================= */

@Composable
private fun TopMenuPanel(
    isIos: Boolean,
    onStats: () -> Unit,
    onOpenCardStyle: () -> Unit,
    onBackfill: () -> Unit,
    onSettings: () -> Unit
) {
    val cardBg = if (isIos) Color(0xF7FFFFFF) else MaterialTheme.colorScheme.surface
    val textColor = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    val dividerColor = if (isIos) IosColor.separator else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .padding(vertical = 4.dp)
    ) {
        TopMenuRow("📊", "情绪统计", textColor, onStats)
        TopMenuDivider(dividerColor)
        TopMenuRow("🎨", "卡片样式", textColor, onOpenCardStyle)
        TopMenuDivider(dividerColor)
        TopMenuRow("⏱️", "补录历史", textColor, onBackfill)
        TopMenuDivider(dividerColor)
        TopMenuRow("⚙️", "设置", textColor, onSettings)
    }
}

@Composable
private fun TopMenuRow(
    emoji: String,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
private fun TopMenuDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 40.dp)
            .height(0.5.dp)
            .background(color)
    )
}

/* ================= 玻璃图标按钮 ================= */

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(40.dp)
            .liquidGlass(
                shape = CircleShape,
                tint = Color.White.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = IosColor.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

/* ================= 今日卡片 ================= */

@Composable
private fun TodayCard(
    entry: MoodEntry?,
    appearance: CardAppearance,
    isIos: Boolean,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(150),
        label = "cardAlpha"
    )

    val radius = RoundedCornerShape(appearance.cornerRadius.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
    ) {
        val wrapper: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
            when {
                // 照片背景
                appearance.background == "photo" && appearance.imageUri != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(radius)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onEdit
                            )
                    ) {
                        PhotoBackground(uri = appearance.imageUri)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                        content()
                    }
                }
                // 玻璃
                appearance.background == "glass" && isIos -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = radius,
                                tint = Color.White.copy(alpha = 0.55f)
                            )
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onEdit
                            )
                    ) { content() }
                }
                // 渐变
                appearance.background == "gradient" -> {
                    val gradient = entry?.mood?.gradientColors() ?: defaultGradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isIos) Modifier.liquidGlass(radius, Color.Transparent)
                                else Modifier
                            )
                            .clip(radius)
                            .background(Brush.verticalGradient(gradient))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onEdit
                            )
                    ) { content() }
                }
                // 纯色：iOS 白卡
                isIos -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onEdit
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = radius
                    ) { content() }
                }
                // Material 纯色
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = onEdit
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = radius
                    ) { content() }
                }
            }
        }

        val textColor = when {
            appearance.background == "photo" -> Color.White
            appearance.background == "plain" && isIos -> IosColor.textPrimary
            appearance.background == "plain" -> MaterialTheme.colorScheme.onPrimaryContainer
            appearance.background == "glass" && isIos -> IosColor.textPrimary
            else -> entry?.mood?.color() ?: if (isIos) IosColor.textPrimary
            else MaterialTheme.colorScheme.onSurface
        }
        val accentColor = when {
            appearance.background == "photo" -> Color.White
            appearance.background == "plain" -> textColor
            appearance.background == "glass" && isIos -> IosColor.primary
            else -> textColor
        }

        wrapper {
            TodayCardContent(
                entry = entry,
                appearance = appearance,
                textColor = textColor,
                accentColor = accentColor,
                isIos = isIos,
                onShare = onShare
            )
        }
    }
}

@Composable
private fun TodayCardContent(
    entry: MoodEntry?,
    appearance: CardAppearance,
    textColor: Color,
    accentColor: Color,
    isIos: Boolean,
    onShare: () -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        val platformLocale = LocalConfiguration.current.locales[0]
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (appearance.showDate) {
                Text(
                    "今天 · ${SimpleDateFormat("M月d日", platformLocale).format(Date())}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.weight(1f))
            if (entry != null) {
                val shareBtnModifier = if (isIos && appearance.background != "photo") {
                    Modifier
                        .size(36.dp)
                        .liquidGlass(
                            shape = CircleShape,
                            tint = Color.White.copy(alpha = 0.4f)
                        )
                        .clickable(onClick = onShare)
                } else {
                    Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onShare)
                }
                Box(modifier = shareBtnModifier, contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "分享",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (entry == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appearance.showEmoji) {
                    Text("😶", fontSize = 48.sp)
                    Spacer(Modifier.width(16.dp))
                }
                Column {
                    Text(
                        "还没记录今天",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "点这里记录此刻的心情",
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appearance.showEmoji) {
                    Text(entry.mood.emoji, fontSize = 56.sp)
                    Spacer(Modifier.width(16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.mood.label,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    if (appearance.showStars) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            starString(entry.intensity),
                            fontSize = 18.sp,
                            color = accentColor
                        )
                    }
                }
            }
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    "「${entry.note}」",
                    fontSize = 15.sp,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/* ================= 照片背景 ================= */

@Composable
private fun BoxScope.PhotoBackground(uri: String) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, uri) {
        value = try {
            val input = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
            android.graphics.BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            null
        }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
}

/* ================= 连续打卡 ================= */

@Composable
private fun StreakRow(streak: Int, isIos: Boolean) {
    val animatedStreak by animateIntAsState(
        targetValue = streak,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "streakCount"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        if (streak > 0) {
            Text(
                "你好棒！已连续记录 $animatedStreak 天啦",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                "今天还没开始记录呢",
                fontSize = 15.sp,
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ================= 单条历史 ================= */

@Composable
private fun EntryRow(
    entry: MoodEntry,
    isFirst: Boolean,
    isLast: Boolean,
    isIos: Boolean,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(40); entered = true }
    val itemAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "itemAlpha"
    )
    val itemOffsetY by animateFloatAsState(
        targetValue = if (entered) 0f else 24f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "itemOffsetY"
    )

    val dateText = SimpleDateFormat("M月d日 EEEE", Locale.CHINA)
        .format(Date(entry.dayStart))

    Box(
        modifier = Modifier.graphicsLayer {
            alpha = itemAlpha
            translationY = itemOffsetY
        }
    ) {
        if (isIos) {
            val shape = RoundedCornerShape(
                topStart = if (isFirst) IosRadius.card else 0.dp,
                topEnd = if (isFirst) IosRadius.card else 0.dp,
                bottomStart = if (isLast) IosRadius.card else 0.dp,
                bottomEnd = if (isLast) IosRadius.card else 0.dp
            )
            Column(Modifier.padding(horizontal = 16.dp)) {
                if (!isFirst) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 56.dp)
                            .height(0.5.dp)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(shape = shape, tint = Color.White.copy(alpha = 0.5f))
                        .clickable(onClick = onEdit)
                        .padding(vertical = 12.dp, horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(entry.mood.softColor().copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(entry.mood.emoji, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                entry.mood.label,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = IosColor.textPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                starString(entry.intensity),
                                fontSize = 11.sp,
                                color = entry.mood.color()
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(dateText, fontSize = 13.sp, color = IosColor.textSecondary)
                        if (entry.note.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                entry.note,
                                fontSize = 14.sp,
                                color = IosColor.textPrimary.copy(alpha = 0.75f),
                                maxLines = 2
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .liquidGlass(
                                shape = CircleShape,
                                tint = Color.White.copy(alpha = 0.4f)
                            )
                            .clickable(onClick = onShare),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "分享",
                            tint = IosColor.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = IosColor.textTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        } else {
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
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(entry.mood.color(), RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(12.dp))
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
    }
}

/* ================= 编辑弹窗 ================= */

@Composable
private fun EditDialog(
    initialDay: Long,
    isIos: Boolean,
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
                    set(JavaCal.YEAR, y); set(JavaCal.MONTH, m)
                    set(JavaCal.DAY_OF_MONTH, d)
                    set(JavaCal.HOUR_OF_DAY, 0); set(JavaCal.MINUTE, 0)
                    set(JavaCal.SECOND, 0); set(JavaCal.MILLISECOND, 0)
                }
                selectedDay = newCal.timeInMillis
            },
            c.get(JavaCal.YEAR), c.get(JavaCal.MONTH), c.get(JavaCal.DAY_OF_MONTH)
        ).show()
    }

    val isToday = selectedDay == MainViewModel.todayStart()
    val dateLabel = if (isToday) "今天"
    else SimpleDateFormat("yyyy年M月d日", Locale.CHINA).format(Date(selectedDay))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.95f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(
                if (existing == null) "记录个心情吧" else "编辑记录",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(IosRadius.button))
                        .background(
                            if (isIos) IosColor.bg
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { showDatePicker() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("日期", fontSize = 15.sp,
                            color = if (isIos) IosColor.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text(
                            dateLabel, fontSize = 15.sp,
                            color = if (isIos) IosColor.primary
                            else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.DateRange, contentDescription = null,
                            tint = if (isIos) IosColor.primary
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text("啥心情呢", fontSize = 13.sp,
                    color = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
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
                                .background(if (selected) m.softColor() else Color.Transparent)
                                .clickable { mood = m }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(m.emoji, fontSize = 28.sp)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                m.label, fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) m.color()
                                else if (isIos) IosColor.textSecondary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Text(
                    "程度  ${starString(intensity.toInt())}",
                    fontSize = 13.sp,
                    color = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 1f..5f, steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary,
                        activeTrackColor = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text("发生了什么？（可选）",
                            color = if (isIos) IosColor.textTertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    minLines = 2, maxLines = 4,
                    shape = RoundedCornerShape(IosRadius.button),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isIos) IosColor.separator
                        else MaterialTheme.colorScheme.outline,
                        focusedContainerColor = if (isIos) IosColor.bg
                        else MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = if (isIos) IosColor.bg
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isToday && existing == null) {
                    Text("将在 $dateLabel 补录一条记录", fontSize = 13.sp,
                        color = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(mood, intensity.toInt(), note.trim(), selectedDay)
            }) {
                Text("保存", fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontSize = 17.sp,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary)
            }
        }
    )
}

/* ================= 统计弹窗 ================= */

data class MoodStat(val mood: Mood, val count: Int, val percent: Float)

private fun calcMonthlyStats(entries: List<MoodEntry>): List<MoodStat> {
    val cal = JavaCal.getInstance()
    val y = cal.get(JavaCal.YEAR); val m = cal.get(JavaCal.MONTH)
    val monthEntries = entries.filter {
        val c = JavaCal.getInstance().apply { timeInMillis = it.dayStart }
        c.get(JavaCal.YEAR) == y && c.get(JavaCal.MONTH) == m
    }
    val total = monthEntries.size.toFloat()
    if (total == 0f) return Mood.values().map { MoodStat(it, 0, 0f) }
    return Mood.values().map { mood ->
        val count = monthEntries.count { it.mood == mood }
        MoodStat(mood, count, count / total)
    }.sortedByDescending { it.count }
}

@Composable
private fun StatsDialog(
    entries: List<MoodEntry>,
    isIos: Boolean,
    onDismiss: () -> Unit
) {
    val stats = calcMonthlyStats(entries)
    val total = stats.sumOf { it.count }
    val cal = JavaCal.getInstance()
    val monthLabel = "${cal.get(JavaCal.YEAR)}年${cal.get(JavaCal.MONTH) + 1}月"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.95f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text("情绪统计 · $monthLabel", fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("本月记录 $total 天", fontSize = 15.sp,
                    color = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant)

                if (total == 0) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("本月还没有记录呢 🌱",
                            color = if (isIos) IosColor.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    stats.forEach { stat -> StatBar(stat, isIos) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", fontSize = 17.sp,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun StatBar(stat: MoodStat, isIos: Boolean) {
    var grew by remember { mutableStateOf(false) }
    LaunchedEffect(stat.mood) { grew = true }
    val targetFraction = stat.percent.coerceIn(0f, 1f)
    val barFraction by animateFloatAsState(
        targetValue = if (grew) targetFraction else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "barFraction"
    )

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.mood.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(stat.mood.label, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Text("${stat.count} 次  ${(stat.percent * 100).toInt()}%", fontSize = 13.sp,
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth().height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isIos) IosColor.bg
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(barFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(stat.mood.color())
            )
        }
    }
}

/* ================= 设置弹窗 ================= */

@Composable
private fun SettingsDialog(
    entries: List<MoodEntry>,
    isIos: Boolean,
    onToggleStyle: () -> Unit,
    onExport: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.95f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text("设置", fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingSwitchRow(
                    emoji = "🎨",
                    title = "iOS 玻璃风格",
                    subtitle = if (isIos) "当前：iOS 玻璃风" else "当前：经典 Material 风",
                    checked = isIos,
                    isIos = isIos,
                    onToggle = onToggleStyle
                )
                DividerLine(isIos)

                SettingRow(
                    emoji = "📤",
                    title = "导出数据（CSV）",
                    subtitle = "共 ${entries.size} 条记录，可发到微信/抖音，还可将导入AI智能分析",
                    isIos = isIos,
                    onClick = onExport
                )
                DividerLine(isIos)

                SettingRow(
                    emoji = "🗑️",
                    title = "清空所有数据",
                    subtitle = "不可恢复，请谨慎操作",
                    titleColor = if (isIos) IosColor.danger
                    else MaterialTheme.colorScheme.error,
                    isIos = isIos,
                    onClick = { showClearConfirm = true }
                )
                DividerLine(isIos)

                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text("关于", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = if (isIos) IosColor.textPrimary
                            else MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(6.dp))
                    val subColor = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Text("每日情绪账本 v1.0", fontSize = 13.sp, color = subColor)
                    Text("记录每一天的心情，你的内心助手", fontSize = 13.sp, color = subColor)
                    Text("本软件由魏文彬开发", fontSize = 13.sp, color = subColor)
                    Text("Copyright © 2026 Cmpss. All Rights Reserved. ", fontSize = 13.sp, color = subColor)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", fontSize = 17.sp,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary)
            }
        }
    )

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = if (isIos) Color.White.copy(alpha = 0.95f)
            else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
            title = {
                Text("确定清空所有数据？", fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary
                    else MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Text("将永久删除全部 ${entries.size} 条记录，不可恢复。",
                    fontSize = 15.sp,
                    color = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll(); showClearConfirm = false; onDismiss()
                }) {
                    Text("我真的确定清空", fontSize = 17.sp,
                        color = if (isIos) IosColor.danger
                        else MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("等等！我再想想", fontSize = 17.sp,
                        color = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
private fun DividerLine(isIos: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 34.dp)
            .height(0.5.dp)
            .background(
                if (isIos) IosColor.separator
                else MaterialTheme.colorScheme.outlineVariant
            )
    )
}

@Composable
private fun SettingRow(
    emoji: String,
    title: String,
    subtitle: String,
    isIos: Boolean,
    titleColor: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(IosRadius.smallButton))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = titleColor ?: if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp,
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = if (isIos) IosColor.textTertiary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    emoji: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    isIos: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp,
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isIos) IosColor.primary
                else MaterialTheme.colorScheme.primary
            )
        )
    }
}

/* ================= 工具 ================= */

private fun starString(n: Int): String {
    val c = n.coerceIn(0, 5)
    return "★".repeat(c) + "☆".repeat(5 - c)
}

/* ================= 情绪配色 ================= */

fun Mood.color(): Color = when (this) {
    Mood.HAPPY   -> Color(0xFFF5A623)
    Mood.CALM    -> Color(0xFF4CAF93)
    Mood.SAD     -> Color(0xFF5B7DB1)
    Mood.ANGRY   -> Color(0xFFE05353)
    Mood.ANXIOUS -> Color(0xFF9C6BC7)
}

fun Mood.softColor(): Color = when (this) {
    Mood.HAPPY   -> Color(0xFFFFF3DC)
    Mood.CALM    -> Color(0xFFE0F4F0)
    Mood.SAD     -> Color(0xFFE4ECF7)
    Mood.ANGRY   -> Color(0xFFFFE5E5)
    Mood.ANXIOUS -> Color(0xFFF2E4FA)
}

fun Mood.gradientColors(): List<Color> = when (this) {
    Mood.HAPPY   -> listOf(Color(0xFFFFE4A1), Color(0xFFFFC06C))
    Mood.CALM    -> listOf(Color(0xFFD5F3EE), Color(0xFF9CD9CE))
    Mood.SAD     -> listOf(Color(0xFFDCE7F7), Color(0xFF9AB4D9))
    Mood.ANGRY   -> listOf(Color(0xFFFFDCDC), Color(0xFFF19191))
    Mood.ANXIOUS -> listOf(Color(0xFFEEDCFA), Color(0xFFC09DE0))
}

val defaultGradient: List<Color> = listOf(Color(0xFFE8EAF6), Color(0xFFBFC5E0))