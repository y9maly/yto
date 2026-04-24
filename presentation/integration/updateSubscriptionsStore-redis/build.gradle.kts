plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")

    api(project(":presentation:integration:updateSubscriptionsStore"))
    api(project(":backend:types"))
    api(project(":presentation:types"))
}
