package me.shouheng.deproguard.utils

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

private val prefs = Preferences.userRoot().node("user-preferences")

val settings: Settings = PreferencesSettings(prefs)
