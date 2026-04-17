plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:domain:service"))

    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    implementation(project(":backend:integration:eventCollector"))
    implementation(project(":backend:integration:data:repository"))
    implementation(project(":backend:integration:data:fileStorage"))
}
