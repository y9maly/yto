plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.atomicfu") version "0.30.0"
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.9.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "stdlib"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
