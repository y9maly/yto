plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:integration:selector"))
    api(project(":backend:types"))
    api(project(":backend:query"))
    api(project(":backend:integration:data:repository"))
}
