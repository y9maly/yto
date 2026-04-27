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
    api(project(":presentation:types"))
    api(project(":backend:integration:openidConnectTelegram"))

    val ktorVersion = "3.4.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")
    implementation("com.nimbusds:nimbus-jose-jwt:10.9")
    implementation("com.auth0:java-jwt:4.5.1")
}
