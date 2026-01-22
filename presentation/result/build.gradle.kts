plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.commonMain.get().kotlin.srcDir("src")

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":presentation:input"))
        api(project(":presentation:types"))
        api(project(":common:types"))

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "api-results"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
