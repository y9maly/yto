plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:integration:data:fileStorage"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
