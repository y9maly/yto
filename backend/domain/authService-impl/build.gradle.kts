plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:domain:service"))
    api(project(":backend:domain:authService"))

    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    implementation(project(":backend:integration:data:loginRepository"))
    implementation(project(":backend:integration:openidConnectTelegram"))
    implementation(project(":backend:integration:eventCollector"))
}
