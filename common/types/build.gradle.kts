import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.commonMain.get().kotlin.srcDir("src")

kotlin {
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_11
    }

    js(IR) {
        binaries.library()
        generateTypeScriptDefinitions()
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    sourceSets.commonMain.dependencies {
        api(project(":libs:stdlib"))
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    }
}
