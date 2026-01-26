plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
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
        implementation(libs.kotlinx.rpc.core)
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "api-krpc"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
