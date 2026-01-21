plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":presentation:integration:authenticator"))
    api(project(":backend:types"))
    api(project(":presentation:types"))
}
