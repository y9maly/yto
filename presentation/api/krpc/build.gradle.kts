plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.commonMain.get().kotlin.srcDir("src")

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":presentation:types"))
        api(project(":presentation:input"))
        api(project(":presentation:result"))
        implementation(libs.kotlinx.rpc.core)
    }
}
