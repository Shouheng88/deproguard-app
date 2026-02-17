package me.shouheng.deproguard.utils

import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true // 忽略JSON中未知的字段
    isLenient = true // 宽松模式（允许JSON格式不严格）
    encodeDefaults = true // 序列化时包含默认值
}
