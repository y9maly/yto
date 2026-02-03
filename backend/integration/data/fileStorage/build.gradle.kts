plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
}
