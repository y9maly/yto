plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:types"))
    api(project(":backend:reference"))
    api(project(":backend:input"))

    // Implementation

    implementation(project(":backend:integration:selector"))
    implementation(project(":backend:integration:data:repository"))
}
