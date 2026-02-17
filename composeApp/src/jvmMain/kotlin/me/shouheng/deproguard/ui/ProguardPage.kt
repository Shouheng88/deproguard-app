package me.shouheng.deproguard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import deproguard.composeapp.generated.resources.Res
import deproguard.composeapp.generated.resources.tab_proguard_name
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.launch
import me.shouheng.deproguard.manager.ProguardManager
import me.shouheng.deproguard.manager.showError
import me.shouheng.deproguard.manager.showSuccess
import me.shouheng.deproguard.ui.widget.PageTitle
import me.shouheng.deproguard.ui.widget.PageToolbar
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProguardPage() {
    var text by remember { mutableStateOf(ProguardManager.getLastInputText() ?: "") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column {
            Toolbar {
                val mappingFilePath = ProguardManager.getMappingFilePath()
                if (text.isEmpty()) {
                    showError("待混淆的文本为空，请输入之后再尝试！")
                    return@Toolbar
                }
                if (mappingFilePath.isNullOrEmpty()) {
                    showError("请选择使用的 Mapping 文件！")
                    return@Toolbar
                }
                ProguardManager.retrace(
                    text,
                    onSuccess = {
                        text = it
                        showSuccess("反混淆成功！")
                    },
                    onFailed = { message ->
                        showError("反混淆失败：$message")
                    }
                )
            }
            Column (
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp),
                horizontalAlignment = Alignment.End
            ) {
                // 待反混淆的文本
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        ProguardManager.setLastInputText(text)
                    },
                    textStyle = TextStyle(
                        fontSize = 14.sp
                    ),
                    label = { Text("输入需要反混淆的文本") },
                    singleLine = false,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 300.dp)
                        .fillMaxWidth()
                )
                // Mapping 文件选择器
                MappingFileChooser()
                // Dict 文件选择器
                DictFileChooser()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MappingFileChooser() {

    var mappingFilePath by remember { mutableStateOf(ProguardManager.getMappingFilePath()) }
    var showMappingDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = showMappingDropdown,
            onExpandedChange = { showMappingDropdown = !showMappingDropdown },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = mappingFilePath ?: "暂未选择 Mapping 文件",
                onValueChange = {},
                readOnly = true,
                label = { Text("选择 Mapping 文件") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                textStyle = TextStyle(
                    fontSize = 13.sp
                ),
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        Modifier.clickable { showMappingDropdown = !showMappingDropdown }
                    )
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = showMappingDropdown,
                onDismissRequest = { showMappingDropdown = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                ProguardManager.getMappingFilePaths().forEach { path ->
                    DropdownMenuItem(
                        text = { Text(path, fontSize = 13.sp) },
                        onClick = {
                            mappingFilePath = path
                            val file = PlatformFile(path)
                            ProguardManager.setMappingFilePath(file, false)
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    val file = FileKit.openFilePicker()
                    file ?: return@launch
                    val legal = ProguardManager.isLegalFile(file)
                    if (!legal) {
                        showError("选择的文件不合法！")
                        return@launch
                    }
                    ProguardManager.setMappingFilePath(file)
                    mappingFilePath = file.absolutePath()
                }
            }
        ) {
            Icon(Icons.Default.FileOpen, null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DictFileChooser() {

    var dictFilePath by remember { mutableStateOf(ProguardManager.getDictFilePath()) }
    var showDictDropdown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = showDictDropdown,
            onExpandedChange = { showDictDropdown = !showDictDropdown },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = dictFilePath ?: "暂未选择 Dict 文件",
                onValueChange = {},
                readOnly = true,
                label = { Text("选择 Dict 文件") },
                leadingIcon = {
                    Icon(Icons.Default.Description, contentDescription = null)
                },
                textStyle = TextStyle(
                    fontSize = 13.sp
                ),
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        Modifier.clickable { showDictDropdown = !showDictDropdown }
                    )
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = showDictDropdown,
                onDismissRequest = { showDictDropdown = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                ProguardManager.getDictFilePaths().forEach { path ->
                    DropdownMenuItem(
                        text = { Text(path, fontSize = 13.sp) },
                        onClick = {
                            dictFilePath = path
                            val file = PlatformFile(path)
                            ProguardManager.setDictFilePath(file, false)
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                scope.launch {
                    val file = FileKit.openFilePicker()
                    file ?: return@launch
                    val legal = ProguardManager.isLegalFile(file)
                    if (!legal) {
                        showError("选择的文件不合法！")
                        return@launch
                    }
                    ProguardManager.setDictFilePath(file)
                    dictFilePath = file.absolutePath()
                }
            }
        ) {
            Icon(Icons.Default.FileOpen, null, tint = Color.White)
        }
    }
}

@Composable
private fun Toolbar(onDeProguard: () -> Unit) {
    PageToolbar {
        PageTitle(
            stringResource(Res.string.tab_proguard_name),
            Icons.Default.EnhancedEncryption,
            modifier = Modifier.weight(1f)
        )
        // 执行反混淆
        Button(onClick = onDeProguard) {
            Text("开始 | 反混淆", style = TextStyle(color = Color.White))
        }
    }
}