plugins {
    kotlin("jvm")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler:${libs.versions.kotlin.get()}")
}

publishing {
    publications.create<MavenPublication>("publication") {
        from(components["java"])
        groupId = "me.maly.y9to"
        artifactId = "compiler-plugin-endpoint-controller"
        version = "1.0-SNAPSHOT"
    }

    repositories {
        mavenLocal()
    }
}
