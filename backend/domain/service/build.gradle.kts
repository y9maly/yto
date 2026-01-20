plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:types"))

    // Implementation

    implementation(project(":backend:domain:selector"))
    implementation(project(":backend:integration:data:repository"))
}
