plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    implementation(project(":backend:domain:service"))
    implementation(project(":backend:types"))
    implementation(project(":containers:monolith"))
    implementation(project(":presentation:api:controller"))
    implementation(project(":presentation:api:krpc"))
    implementation(project(":presentation:integration:assembler"))
    implementation(project(":presentation:integration:presenter"))
}
