package me.shouheng.deproguard.manager

import androidx.compose.material3.ColorScheme
import me.shouheng.deproguard.utils.settings
import me.shouheng.devtools.devtools.data.theme.DarkColorScheme
import me.shouheng.devtools.devtools.data.theme.LightColorScheme

/** 主体管理 */
object ThemeManger {

    private const val KEY_THEME = "__is_light_theme__"

    private var currentTheme: ColorScheme? = null

    /** 设置主题 */
    fun setTheme(scheme: ColorScheme) {
        currentTheme = scheme
        settings.putBoolean(KEY_THEME, scheme == LightColorScheme)
    }

    /** 获取主题 */
    fun getTheme(): ColorScheme {
        if (currentTheme != null) {
            return currentTheme!!
        }
        val isDarkTheme = settings.getBooleanOrNull(KEY_THEME) == true
        val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
        currentTheme = colorScheme
        return currentTheme!!
    }
}