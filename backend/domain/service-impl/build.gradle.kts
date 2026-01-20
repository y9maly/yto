plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:domain:service"))
    api(project(":backend:types"))
    api(project(":backend:domain:selector"))
    api(project(":backend:integration:data:repository"))
}
