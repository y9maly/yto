import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {}

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
