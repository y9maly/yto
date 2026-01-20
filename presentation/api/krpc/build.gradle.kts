import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
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
        api(project(":presentation:types"))
        api(project(":presentation:input"))
        api(project(":presentation:result"))
        implementation(libs.kotlinx.rpc.krpc.serialization.json)
        implementation(libs.kotlinx.rpc.krpc.ktor.server)
        implementation(libs.kotlinx.rpc.krpc.server)
    }
}
