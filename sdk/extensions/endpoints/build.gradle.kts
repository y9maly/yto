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
        api(project(":presentation:api:endpoint"))
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "sdk-extension-endpoints"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
