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
    api(project(":presentation:api:controller"))
    api(project(":presentation:api:krpc"))
    api(project(":presentation:integration:assembler"))
    api(project(":presentation:integration:presenter"))
    api(project(":presentation:integration:authenticator"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
