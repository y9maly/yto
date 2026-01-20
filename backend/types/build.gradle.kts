plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":common:types"))
    api(project(":libs:stdlib"))
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
