plugins {
    id("kmp-conventions")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.commonMain.get().kotlin.srcDir("src")

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":presentation:types"))
        api(project(":presentation:input"))
        api(project(":presentation:result"))
        api(project(":presentation:api:krpc"))

        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "sdk"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
