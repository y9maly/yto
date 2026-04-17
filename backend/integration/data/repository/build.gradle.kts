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
    api(project(":libs:paging:core"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
}
