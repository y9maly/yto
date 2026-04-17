plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation(project(":presentation:infrastructure:jwtManager"))

    api(project(":presentation:integration:tokenProvider"))
    api(project(":backend:types"))
    api(project(":presentation:types"))
}
