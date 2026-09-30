package com.example.mooddiary.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val formatterCache = ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>()

/**
 * 复用 [SimpleDateFormat]。
 *
 * 它的构造开销不小，而日期文本在列表里是「每一行 × 每次重组」都会格式化的，
 * 每次都 new 一个会白白产生大量对象。
 *
 * [SimpleDateFormat] 自身不是线程安全的，所以按线程各缓存一份。
 */
fun cachedDateFormatter(pattern: String, locale: Locale): SimpleDateFormat {
    val key = "$pattern@$locale"
    val local = formatterCache.getOrPut(key) {
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat = SimpleDateFormat(pattern, locale)
        }
    }
    return local.get()!!
}
