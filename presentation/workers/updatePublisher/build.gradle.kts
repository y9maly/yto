plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    implementation("me.y9san9.aqueue:core:1.0.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.apache.kafka:kafka-clients:4.2.0")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.01")

    api(project(":presentation:integration:updateSubscriptionsStore"))
    api(project(":presentation:integration:updateProducer"))
    api(project(":presentation:integration:presenter"))
    api(project(":presentation:types"))
    api(project(":backend:types"))
    api(project(":backend:event"))
    api(project(":backend:domain:service"))
}
