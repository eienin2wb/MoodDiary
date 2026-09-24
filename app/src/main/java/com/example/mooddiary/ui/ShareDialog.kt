package com.example.mooddiary.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.CardTemplate
import com.example.mooddiary.util.ImageSaver
import com.example.mooddiary.util.ShareCardGenerator
import com.example.mooddiary.util.ShareHelper

@Composable
fun ShareDialog(
    entry: MoodEntry,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var template by remember { mutableStateOf(CardTemplate.CLASSIC) }

    // 模板变化时重新生成
    val bitmap = remember(entry.id, template) {
        ShareCardGenerator.generate(entry, template)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享心情") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---- 模板选择 ----
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CardTemplate.values().toList()) { t ->
                        val selected = t == template
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { template = t }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                t.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold
                                else FontWeight.Normal,
                                color = if (selected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ---- 图片预览 ----
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "预览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1080f / 1920f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val uri = ImageSaver.saveToGallery(context, bitmap)
                val msg = if (uri != null) "已保存到相册" else "保存失败"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (uri != null) onDismiss()
            }) {
                Text("保存相册")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("取消") }
                TextButton(onClick = {
                    ShareHelper.shareImage(context, bitmap)
                }) {
                    Text("分享")
                }
            }
        }
    )
}