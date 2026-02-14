plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:types"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
}
