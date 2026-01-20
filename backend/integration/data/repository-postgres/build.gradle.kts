plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:infrastructure:postgres"))
    api(project(":backend:types"))
    api(project(":backend:integration:data:repository"))
}
