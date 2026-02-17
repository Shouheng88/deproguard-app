package me.shouheng.devtools.devtools.data.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

/** 定义浅色主题的配色方案 */
val LightColorScheme: ColorScheme = lightColorScheme(
    // 主色，稍深的蓝色，科技感强
    primary = Color(0xFF0CA17C),
    // 主色上的文字颜色，白色对比强
    onPrimary = Color.White,

    // 次主色，清爽的青绿色，用于强调按钮等
    secondary = Color(0xFF0CA17C),
    // 次主色上的文字颜色
    onSecondary = Color.White,

    // 导航栏选中之后的背景颜色
    secondaryContainer = Color(0x100CA17C),

    // 整体背景，浅灰色，看起来更现代
    background = Color(0xFFF0F0F0),
    // 背景上的文字颜色，深灰
    onBackground = Color(0xFF424242),

    // 卡片、弹窗等组件的背景色，纯白
    surface = Color(0xFFF9F9F9),
    // dropdown 菜单等的颜色
    surfaceContainer = Color(0xFFF9F9F9),
    // 表面上的文字颜色
    onSurface = Color(0xFF424242),

    // 错误色，醒目的红色
    error = Color(0xFFD32F2F),
    // 错误提示上的文字颜色
    onError = Color.White,

    // 边框、分割线颜色，淡灰色
    outline = Color(0xFFDFDFDF)
)
