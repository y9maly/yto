import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu") version "0.29.0"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

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
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    }
}

group = "y9to.libs"
version = "1.2-SNAPSHOT"

publishing {
    repositories {
        mavenLocal()
    }
}
