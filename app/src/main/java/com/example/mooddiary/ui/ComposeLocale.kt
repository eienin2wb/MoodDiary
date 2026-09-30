package com.example.mooddiary.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * 当前系统 Locale 的可观测版本。
 *
 * 直接在 composable 里调用 `Locale.getDefault()` 读不到 Compose 的状态，
 * 系统语言切换后 UI 不会重组，日期会停留在旧语言（lint 也会报
 * NonObservableLocale 错误）。[LocalConfiguration] 则由 Compose 监听，
 * 配置变化时会正常触发重组。
 *
 * 注意：当前 Compose 版本（1.10.4）还没有 `LocalLocale`，
 * 因此这里用 `LocalConfiguration.current.locales`。
 */
@Composable
fun rememberPlatformLocale(): Locale = LocalConfiguration.current.locales[0]
