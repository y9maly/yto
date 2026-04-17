plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:infrastructure:postgres"))
    api(project(":backend:types"))
    api(project(":backend:integration:data:repository"))

    implementation(project(":backend:infrastructure:postgres"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}
