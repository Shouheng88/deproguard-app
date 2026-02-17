package me.shouheng.deproguard.ui.widget

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.shouheng.deproguard.data.UIConst.TOOLBAR_HEIGHT
import me.shouheng.deproguard.data.UIConst.TOOLBAR_H_PADDING

/** 标题栏 */
@Preview
@Composable
fun PageToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .height(TOOLBAR_HEIGHT.dp)
            .padding(horizontal = TOOLBAR_H_PADDING.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}