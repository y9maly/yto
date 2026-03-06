plugins {
    kotlin("jvm") version "2.3.10"
    `java-gradle-plugin`
    `maven-publish`
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.3.10")
}

gradlePlugin {
    plugins {
        create("EndpointControllerPlugin") {
            id = "y9to.gradlePlugins.endpointController"
            displayName = "EndpointControllerPlugin"
            description = "EndpointControllerPlugin"
            implementationClass = "y9to.gradlePlugins.endpointController.EndpointControllerGradlePlugin"
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "gradle-plugin-endpoint-controller"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
