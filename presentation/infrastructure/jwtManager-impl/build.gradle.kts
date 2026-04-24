plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation("com.auth0:java-jwt:4.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")
    implementation(project(":backend:domain:service"))

    api(project(":presentation:infrastructure:jwtManager"))
    api(project(":backend:types"))
}
