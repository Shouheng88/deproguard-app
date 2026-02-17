package me.shouheng.deproguard.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource

/**
 * 首页 TAB 数据结构
 */
data class HomeTabItem(
    val title: StringResource,
    val icon: ImageVector,
    val route: String,
    val content: @Composable () -> Unit
)