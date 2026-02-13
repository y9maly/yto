plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:event"))
    api(project(":backend:types"))
    api(project(":backend:reference"))
    api(project(":backend:input"))
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")

    // Implementation

    implementation(project(":backend:integration:selector"))
    implementation(project(":backend:integration:data:repository"))
    implementation(project(":backend:integration:data:fileStorage"))
}
