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
    api(project(":backend:query"))
    api(project(":libs:paging:core"))

    // Implementation

    implementation(project(":backend:infrastructure:postgres"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
