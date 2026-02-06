plugins {
    id("kmp-conventions")
    id("org.jetbrains.kotlinx.atomicfu") version "0.30.0"
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
        implementation(project(":libs:stdlib"))
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "io"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
