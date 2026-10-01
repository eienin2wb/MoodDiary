package com.example.mooddiary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood

/**
 * 快速心情记录。点一下 emoji 立即保存为强度 3、今天的记录。
 *
 * [photoUri] 非空时（来自「瞬间」自拍），会在顶部展示圆形照片缩略图，
 * 保存时一并写入 [MoodEntry.imageUri]。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickMoodSheet(
    existingMood: Mood?,
    photoUri: String? = null,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    onPick: (Mood) -> Unit,
    onOpenFull: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val containerColor: Color =
        if (isIos) Color.White.copy(alpha = 0.98f)
        else MaterialTheme.colorScheme.surface
    val tc: Color =
        if (isIos) IosColor.textPrimary
        else MaterialTheme.colorScheme.onSurface
    val stc: Color =
        if (isIos) IosColor.textSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant
    val primary: Color =
        if (isIos) IosColor.primary
        else MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp, bottom = 6.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(stc.copy(alpha = 0.30f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            // ============ 自拍照片缩略图 ============
            if (photoUri != null) {
                val bitmap = rememberPhotoBitmap(photoUri, maxEdge = 480)
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = primary.copy(alpha = 0.45f),
                                shape = CircleShape
                            )
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            Text(
                text = if (existingMood == null) {
                    if (lang == "zh") "此刻心情如何？" else "How are you feeling?"
                } else {
                    String.format(
                        if (lang == "zh") "今天记录了「%s」"
                        else "Today: %s",
                        labelOf(existingMood, customs, lang)
                    )
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = tc,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (existingMood == null) {
                    if (lang == "zh") "点一下立即保存" else "Tap to save instantly"
                } else {
                    if (lang == "zh") "想换一个？" else "Pick again?"
                },
                fontSize = 12.sp,
                color = stc,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Mood.values().forEach { mood ->
                    Box(modifier = Modifier.weight(1f)) {
                        QuickMoodButton(
                            mood = mood,
                            emoji = emojiOf(mood, customs),
                            label = labelOf(mood, customs, lang),
                            selected = mood == existingMood,
                            isIos = isIos,
                            onClick = { onPick(mood) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            TextButton(onClick = onOpenFull) {
                Text(
                    text = if (lang == "zh") "详细记录 →" else "Detailed log →",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = primary
                )
            }
        }
    }
}


@Composable
private fun QuickMoodButton(
    mood: Mood,
    emoji: String,
    label: String,
    selected: Boolean,
    isIos: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val moodColor: Color = mood.color()

    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.88f
            selected -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "quickMoodScale"
    )

    val selectedBg by animateColorAsState(
        targetValue = if (selected) moodColor.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(180),
        label = "quickMoodBg"
    )

    val selectedBorder by animateColorAsState(
        targetValue = if (selected) moodColor else Color.Transparent,
        animationSpec = tween(180),
        label = "quickMoodBorder"
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            selected -> moodColor
            isIos -> IosColor.textSecondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(180),
        label = "quickMoodLabel"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(selectedBg)
                .border(
                    width = if (selected) 2.dp else 0.dp,
                    color = selectedBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 30.sp)
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = labelColor,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}