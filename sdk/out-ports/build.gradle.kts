plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":presentation:types"))
        api(project(":presentation:result"))
        api(project(":presentation:api:krpc"))
        api(project(":presentation:api:endpoint"))
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "sdk-out-ports"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
