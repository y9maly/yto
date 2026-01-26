plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("plugin.serialization") version "2.3.0" apply false
    id("io.ktor.plugin") version "3.4.0" apply false
    id("com.dorongold.task-tree") version "4.0.1" apply false
    id("com.gradleup.shadow") version "9.0.1" apply false
    idea
    `project-report`
    `maven-publish`
}

allprojects {
    this.afterEvaluate {
        val kotlin = try {
            (this as ExtensionAware).extensions.getByName("kotlin")
        } catch (_: UnknownDomainObjectException) {
            return@afterEvaluate
        }

        when (kotlin) {
            is org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension -> {
                kotlin.compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
            }

            is org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension -> {
                kotlin.compilerOptions.optIn.add("kotlin.time.ExperimentalTime")
            }
        }
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}
