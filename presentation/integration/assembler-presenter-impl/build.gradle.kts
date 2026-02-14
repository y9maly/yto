plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    api(project(":presentation:integration:assembler"))
    api(project(":presentation:integration:presenter"))
    api(project(":backend:types"))
    api(project(":backend:input"))
    api(project(":backend:query"))
    api(project(":backend:domain:service"))
    api(project(":presentation:types"))
}
