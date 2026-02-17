import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

            // JetBrains Compose Multiplatform 的 ViewModel 支持
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta03")

            val fileKit = "0.10.0-beta04"
            implementation("io.github.vinceglb:filekit-core:$fileKit")
            implementation("io.github.vinceglb:filekit-dialogs:$fileKit")
            implementation("io.github.vinceglb:filekit-dialogs-compose:$fileKit")
            implementation("io.github.vinceglb:filekit-coil:$fileKit")

            // 数据持久化
            implementation("com.russhwolf:multiplatform-settings:1.3.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("com.guardsquare:proguard-base:7.4.0")
        }
    }
}

compose.desktop {
    application {
        mainClass = "me.shouheng.deproguard.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DeProguard"
            packageVersion = "1.0.0"
            description = "An convenient development tool to deproguard error stacks."
            vendor = "wyuhuan"
            copyright = "© 2026 wyuhuan"
            // 添加 java sql 类库
            modules("java.sql")
        }
        buildTypes.release.proguard {
            isEnabled.set(true) // 是否开启混淆
            obfuscate.set(true)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
    }
}
