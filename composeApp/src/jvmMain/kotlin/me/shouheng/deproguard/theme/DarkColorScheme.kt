package me.shouheng.devtools.devtools.data.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ColorScheme

/** 定义深色主题的配色方案 */
val DarkColorScheme: ColorScheme = darkColorScheme(
    // 主色，蓝色，突出主按钮或高亮元素
    primary = Color(0xFF2196F3),
    // 主色上的文字颜色，使用白色增强对比
    onPrimary = Color.White,

    // 次主色，用于强调性按钮、浮动操作按钮等
    secondary = Color(0xFF03DAC5),
    // 次主色上的文字颜色
    onSecondary = Color.Black,

    // 应用背景色（暗灰黑），开发者风格
    background = Color(0xFF121212),
    // 背景上的文字颜色，浅灰色
    onBackground = Color(0xFFE0E0E0),

    // 卡片、弹窗等表面颜色
    surface = Color(0xFF1E1E1E),
    // 表面上的文字颜色，接近白
    onSurface = Color(0xFFF5F5F5),

    // 错误提示颜色（红色偏粉）
    error = Color(0xFFCF6679),
    // 错误提示背景上的文字颜色
    onError = Color.Black,

    // 边框、分割线等弱提示颜色
    outline = Color(0xFF424242),
)
