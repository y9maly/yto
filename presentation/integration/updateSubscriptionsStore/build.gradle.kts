plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")

    api(project(":backend:types"))
    api(project(":presentation:types"))
}
