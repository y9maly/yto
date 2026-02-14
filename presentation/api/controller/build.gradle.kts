plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    api(project(":libs:paging:core"))
    api(project(":presentation:types"))
    api(project(":presentation:input"))
    api(project(":presentation:result"))
    api(project(":presentation:integration:context"))
    implementation(libs.kotlinx.rpc.core)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
}
