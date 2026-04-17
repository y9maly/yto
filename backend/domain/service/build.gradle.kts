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
}
