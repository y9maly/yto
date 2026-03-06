plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":libs:paging:core"))
    api(project(":backend:types"))
    api(project(":backend:input"))
    api(project(":backend:event"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")

    // Implementation

    implementation(project(":backend:integration:selector"))
    implementation(project(":backend:integration:data:repository"))
    implementation(project(":backend:integration:data:fileStorage"))
}
