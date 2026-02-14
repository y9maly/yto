plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:event"))
    api(project(":backend:types"))
    api(project(":backend:input"))
    api(project(":backend:query"))
}
