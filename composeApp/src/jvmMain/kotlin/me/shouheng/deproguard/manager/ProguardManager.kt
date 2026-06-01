package me.shouheng.deproguard.manager

import io.github.vinceglb.filekit.*
import me.shouheng.deproguard.retrace.MyReTrace
import me.shouheng.deproguard.retrace.ReTrace
import me.shouheng.deproguard.utils.json
import me.shouheng.deproguard.utils.settings
import java.io.File

/** 反混淆工具 */
object ProguardManager {

    private const val KEY_MAPPING_FILE_PATH = "__last_selected_mapping_file_path__"
    private const val KEY_MAPPING_FILE_PATHS = "__last_selected_mapping_file_paths__"

    private const val KEY_DICT_FILE_PATH = "__last_selected_dict_file_path__"
    private const val KEY_DICT_FILE_PATHS = "__last_selected_dict_file_paths__"

    /** 判断选择的 Mapping 文件是否合法 */
    fun isLegalFile(file: PlatformFile): Boolean = file.isRegularFile() && file.exists() && file.size() > 0

    /** 设置 Mapping 文件路径 */
    fun setMappingFilePath(file: PlatformFile, changeList: Boolean = true) {
        val path = file.absolutePath()
        settings.putString(KEY_MAPPING_FILE_PATH, path)

        // 是否需要修改列表
        if (!changeList) {
            return
        }

        // 将文件写入历史记录中
        var paths = mutableListOf<String>()
        val pathsJson = settings.getStringOrNull(KEY_MAPPING_FILE_PATHS)
        if (!pathsJson.isNullOrEmpty()) {
            paths = json.decodeFromString(pathsJson)
            if (paths.size > 8) {
                paths = paths.subList(0, 8)
            }
        }

        val finalPaths = paths.toMutableSet()
        finalPaths.add(path)
        val newJson = json.encodeToString(finalPaths.toList())
        settings.putString(KEY_MAPPING_FILE_PATHS, newJson)
    }

    /** 获取历史选择的 Mapping 文件 */
    fun getMappingFilePaths(): List<String> {
        var paths = mutableListOf<String>()
        val pathsJson = settings.getStringOrNull(KEY_MAPPING_FILE_PATHS)
        if (!pathsJson.isNullOrEmpty()) {
            paths = json.decodeFromString(pathsJson)
        }
        return paths
    }

    /** 获取 Mapping 文件路径 */
    fun getMappingFilePath(): String? = settings.getStringOrNull(KEY_MAPPING_FILE_PATH)

    /** 设置 Dict 文件路径 */
    fun setDictFilePath(file: PlatformFile, changeList: Boolean = true) {
        val path = file.absolutePath()
        settings.putString(KEY_DICT_FILE_PATH, path)

        if (!changeList) {
            return
        }

        // 将文件写入历史记录中
        var paths = mutableListOf<String>()
        val pathsJson = settings.getStringOrNull(KEY_DICT_FILE_PATHS)
        if (!pathsJson.isNullOrEmpty()) {
            paths = json.decodeFromString(pathsJson)
            if (paths.size > 8) {
                paths = paths.subList(0, 8)
            }
        }

        val finalPaths = paths.toMutableSet()
        finalPaths.add(path)
        val newJson = json.encodeToString(finalPaths.toList())
        settings.putString(KEY_DICT_FILE_PATHS, newJson)
    }

    /** 获取 Dict 文件路径 */
    fun getDictFilePath(): String? = settings.getStringOrNull(KEY_DICT_FILE_PATH)

    /** 获取 Dict 文件路径 */
    fun getDictFilePaths(): List<String> {
        var paths = mutableListOf<String>()
        val pathsJson = settings.getStringOrNull(KEY_DICT_FILE_PATHS)
        if (!pathsJson.isNullOrEmpty()) {
            paths = json.decodeFromString(pathsJson)
        }
        return paths
    }

    /** 设置上次输入的文本 */
    fun setLastInputText(text: String) {
        settings.putString("__last_input_proguard_text__", text)
    }

    /** 获取上次输入的文本 */
    fun getLastInputText(): String? = settings.getStringOrNull("__last_input_proguard_text__")

    /** 进行反混淆 */
    fun retrace(text: String, onSuccess: (String) -> Unit, onFailed: (String) -> Unit) {
        // 判断 Mapping 文件
        var mappingFile: File?
        val mappingFilePath = getMappingFilePath()
        if (mappingFilePath.isNullOrEmpty()) {
            onFailed("Mapping 文件为空")
            return
        }
        mappingFile = File(mappingFilePath)
        if (!mappingFile.isFile
            || !mappingFile.exists()
            || mappingFile.length() == 0L
            || !mappingFile.canRead()) {
            onFailed("Mapping 文件状态异常：不存在或者不可读")
            return
        }

        // 判断 Dict 文件
        val dictFilePath = getDictFilePath()
        var dictFile: File? = null
        if (dictFilePath != null) {
            dictFile = File(dictFilePath)
            if (!dictFile.isFile
                || !dictFile.exists()
                || dictFile.length() == 0L
                || !dictFile.canRead()) {
                onFailed("Dict 文件状态异常：不存在或者不可读")
                return
            }
        }

        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File(tempDir, "temp-proguard.text")
        file.writeText(text)

        val proceed = ReTrace.main(
            arrayOf(
                mappingFile.path,
                file.path,
                dictFile?.path
            )
        )
        onSuccess(proceed)
    }
}
