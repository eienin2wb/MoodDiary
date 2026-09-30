package com.example.mooddiary.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.util.L

@Composable
internal fun AnimatedDialog(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + scaleIn(
            initialScale = 0.85f,
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            targetScale = 0.85f,
            animationSpec = tween(150)
        )
    ) { content() }
}


@Composable
internal fun EmptyState(lang: String, isIos: Boolean, onAddClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌱", fontSize = 56.sp,
                modifier = Modifier.graphicsLayer { scaleX = breath; scaleY = breath })
            Spacer(Modifier.height(12.dp))
            Text(L.t("empty_hint", lang),
                color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
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


