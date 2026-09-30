package com.example.mooddiary.ui

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.mooddiary.util.DEFAULT_MAX_IMAGE_EDGE
import com.example.mooddiary.util.decodeImageDownsampled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 加载用户选择的照片，用于卡片背景 / 设置页预览。
 *
 * 相比直接 `BitmapFactory.decodeStream`：
 * - 解码放在 [Dispatchers.IO]，不再阻塞主线程；
 * - 按 `inSampleSize` 下采样，内存占用降到原来的 1/4 ~ 1/16；
 * - [produceState] 以 uri 为 key，同一个 uri 不会重复解码。
 *
 * 失败时返回 null，由调用方回退到默认外观。
 */
@Composable
fun rememberPhotoBitmap(
    uri: String?,
    maxEdge: Int = DEFAULT_MAX_IMAGE_EDGE
): Bitmap? {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri, maxEdge) {
        value = if (uri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { decodeImageDownsampled(context, uri.toUri(), maxEdge) }
        }
    }
    return bitmap
}
