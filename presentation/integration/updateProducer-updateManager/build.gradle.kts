
plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation(project(":presentation:infrastructure:updateManager"))

    api(project(":presentation:integration:updateProducer"))
    api(project(":backend:types"))
    api(project(":presentation:types"))
}
