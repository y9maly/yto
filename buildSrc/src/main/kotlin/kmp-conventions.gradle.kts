import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform")
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate {
        common {
            group("notWasmWasi") {
                // KotlinHierarchyTemplate.default => KotlinHierarchyBuilderImpl => override fun withWasmWasi()
                withCompilations {
                    val target = it.target
                    target.platformType != KotlinPlatformType.wasm ||
                    target !is KotlinJsIrTarget ||
                    target.wasmTargetType != KotlinWasmTargetType.WASI
                }
            }

            group("notJvm") {
                withCompilations {
                    val target = it.target
                    target !is KotlinJvmTarget &&
                    (target !is KotlinWithJavaTarget<*, *> || target.platformType != KotlinPlatformType.jvm)
                }
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
