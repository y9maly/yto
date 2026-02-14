plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    api(project(":backend:types"))
    api(project(":backend:input"))
    api(project(":backend:query"))
    api(project(":presentation:types"))
    api(project(":presentation:input"))
    api(project(":presentation:integration:context"))
}
