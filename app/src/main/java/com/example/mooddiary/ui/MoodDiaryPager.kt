package com.example.mooddiary.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MoodDiaryPager(
    todayEntry: MoodEntry?,
    entries: List<MoodEntry>,
    appearance: CardAppearance,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onEditMood: () -> Unit,
    onShareMood: () -> Unit,
    onViewAllDiaries: () -> Unit,
    onDiaryClick: (MoodEntry) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            // 🌟 滑动时的缩放/透明度过渡
            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                .coerceIn(-1f, 1f)
            val scale = 1f - (kotlin.math.abs(pageOffset) * 0.05f)
            val alpha = 1f - (kotlin.math.abs(pageOffset) * 0.3f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
            ) {
                when (page) {
                    0 -> {
                        // 第一页：今日情绪
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
                                onShare = onShareMood
                            )
                        }
                    }
                    1 -> {
                        // 第二页：我的日记
                        DiaryOverviewCard(
                            entries = entries,
                            isIos = isIos,
                            lang = lang,
                            customs = customs,
                            customMoodDefs = customMoodDefs,
                            onViewAll = onViewAllDiaries,
                            onEntryClick = onDiaryClick
                        )
                    }
                }
            }
        }

        // 🌟 页码指示器
        PagerIndicator(
            pageCount = 2,
            currentPage = pagerState.currentPage,
            primary = primary,
            isIos = isIos,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 10.dp)
        )

        // 🌟 页面标签提示（可点击切换）
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            PagerTab(
                emoji = "😊",
                label = if (lang == "zh") "情绪" else "Mood",
                selected = pagerState.currentPage == 0,
                isIos = isIos,
                primary = primary
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(0) }
            }
            PagerTab(
                emoji = "📖",
                label = if (lang == "zh") "日记" else "Diary",
                selected = pagerState.currentPage == 1,
                isIos = isIos,
                primary = primary
            ) {
                coroutineScope.launch { pagerState.animateScrollToPage(1) }
            }
        }
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
                else (if (isIos) IosColor.textTertiary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
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
            .graphicsLayer { scaleX = scale; scaleY = scale }
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

