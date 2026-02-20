plugins {
    id("kmp-conventions")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
    `maven-publish`
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.commonMain.dependencies {
        api(project(":presentation:types"))
        api(project(":presentation:input"))
        api(project(":presentation:result"))
        api(project(":presentation:api:krpc"))

        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        api("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
        api(project(":libs:paging:core"))
        implementation(project(":libs:io"))
    }

    sourceSets.nonWasmWasiMain.dependencies {
        // Implementation

        val ktorVersion = "3.4.0"
        implementation("io.ktor:ktor-client-core:${ktorVersion}")
        implementation("io.ktor:ktor-client-cio:${ktorVersion}")
        implementation("io.ktor:ktor-client-websockets:${ktorVersion}")
        implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
        implementation(libs.kotlinx.rpc.krpc.client)
        implementation(libs.kotlinx.rpc.krpc.ktor.client)
        implementation(libs.kotlinx.rpc.krpc.serialization.json)
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
