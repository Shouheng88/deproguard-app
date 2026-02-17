package me.shouheng.deproguard.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class SnackMessage(
    val type: Type,
    val content: String,
) {
    enum class Type(
        val backgroundColor: Color,
        val icon: ImageVector
    ) {
        SUCCESS(Colors.SUCCESS, Icons.Default.CheckCircle),
        INFO(Colors.INFO, Icons.Default.Info),
        WARN(Colors.WARN, Icons.Default.Warning),
        ERROR(Colors.ERROR, Icons.Default.Error);

        companion object {
            fun from(actionLabel: String?): Type {
                return entries.firstOrNull { it.name == actionLabel } ?: INFO
            }
        }
    }

    companion object {
        fun success(content: String) = SnackMessage(Type.SUCCESS, content)
        fun info(content: String) = SnackMessage(Type.INFO, content)
        fun warn(content: String) = SnackMessage(Type.WARN, content)
        fun error(content: String) = SnackMessage(Type.ERROR, content)
    }
}