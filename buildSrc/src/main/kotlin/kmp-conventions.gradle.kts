import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform")
}

// see KotlinHierarchyTemplate.default
// see org.jetbrains.kotlin.gradle.plugin.hierarchy.KotlinHierarchyBuilderImpl

private fun KotlinTarget.isAndroid(): Boolean {
    return this is KotlinAndroidTarget
}

private fun KotlinTarget.isJvm(): Boolean {
    if (this is KotlinJvmTarget)
        return true
    if (this is KotlinWithJavaTarget<*, *> && this.platformType == KotlinPlatformType.jvm)
        return true
    return false
}

private fun KotlinTarget.isWeb() = isJs() || isWasm()

private fun KotlinTarget.isJs(): Boolean {
    return this.platformType == KotlinPlatformType.js
}

private fun KotlinTarget.isWasm() = isWasmJs() || isWasmWasi()

private fun KotlinTarget.isWasmJs(): Boolean {
    if (this.platformType != KotlinPlatformType.wasm)
        return false
    if (this !is KotlinJsIrTarget)
        return false
    if (this.wasmTargetType != KotlinWasmTargetType.JS)
        return false
    return true
}

private fun KotlinTarget.isWasmWasi(): Boolean {
    if (this.platformType != KotlinPlatformType.wasm)
        return false
    if (this !is KotlinJsIrTarget)
        return false
    if (this.wasmTargetType != KotlinWasmTargetType.WASI)
        return false
    return true
}


@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("nonAndroid") {
                withCompilations { !it.target.isAndroid() }
            }

            group("nonJvm") {
                withCompilations { !it.target.isJvm() }
            }

            group("nonWeb") {
                withCompilations { !it.target.isWeb() }
            }

            group("nonJs") {
                withCompilations { !it.target.isJs() }
            }

            group("nonWasm") {
                withCompilations { !it.target.isWasm() }
            }

            group("nonWasmWasi") {
                withCompilations { !it.target.isWasmWasi() }
            }

            group("nonWasmJs") {
                withCompilations { !it.target.isWasmJs() }
            }
        }
    }

    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_11
    }

    js(IR) {
        binaries.library()
        useEsModules()
        generateTypeScriptDefinitions()
        browser()
        nodejs()
    }

    wasmWasi {
        nodejs()
        binaries.library()
    }

    wasmJs {
        browser()
        nodejs()
    }

    linuxX64()
    linuxArm64()
//    macosX64()
    macosArm64()
    mingwX64()
//    androidNativeX86()
//    androidNativeX64()
//    androidNativeArm32()
//    androidNativeArm64()
//    iosX64()
    iosArm64()
    iosSimulatorArm64()
//    watchosX64()
//    watchosArm32()
//    watchosArm64()
//    watchosSimulatorArm64()
//    watchosDeviceArm64()
//    tvosX64()
//    tvosArm64()
//    tvosSimulatorArm64()
}
