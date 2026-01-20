plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:integration:data:repository-postgres"))
    api(project(":backend:infrastructure:postgres"))
}
