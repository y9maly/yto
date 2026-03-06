plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":sdk:core"))
        api(project(":sdk:extensions:gracefulScope"))
        implementation(project(":sdk:extensions:endpoints"))

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "sdk-features"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
