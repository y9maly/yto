plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":play:service"))
    api(project(":presentation:api:krpc"))
    api(project(":presentation:api:krpc-impl"))
    api(project(":presentation:integration:authenticator-silly"))
    api(project(":presentation:integration:assembler-presenter-impl"))
}
