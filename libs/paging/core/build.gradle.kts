import java.util.Properties

plugins {
    id("kmp-conventions")
    kotlin("plugin.serialization")
    `maven-publish`
}

data class GithubCredentials(val username: String?, val token: String?)

fun GithubCredentials?.exists() = this != null && username != null && token != null

val githubCredentials = run {
    val file = project.file("credentials/credentials.properties")
    if (!file.exists()) {
        return@run GithubCredentials(
            username = System.getenv("GITHUB_USERNAME") ?: return@run null,
            token = System.getenv("GITHUB_TOKEN") ?: return@run null,
        )
    }
    val properties = Properties().apply { load(file.inputStream()) }
    GithubCredentials(
        username = properties.getProperty("github.username"),
        token = properties.getProperty("github.token"),
    )
}

if (!githubCredentials.exists()) {
    println("Warning: Github credentials not found. Some tests may not work.")
    println("To fix it, create a 'credentials/credentials.properties' file.")
    println("See 'credentials/credentials.properties.example' for more details.")
}

repositories {
    mavenCentral()

    if (githubCredentials.exists()) {
        maven {
            url = uri("https://maven.pkg.github.com/zhelenskiy/kotlinx-serialization-builder")
            credentials {
                username = githubCredentials!!.username!!
                password = githubCredentials.token!!
            }
        }
    }
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
    }

    sourceSets.jvmTest.dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.10.0")
        implementation("net.orandja.obor:obor:2.1.3")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.10.0")
        implementation("com.akuleshov7:ktoml-core:0.7.1")
        implementation("net.peanuuutz.tomlkt:tomlkt:0.5.0")
        implementation("com.github.avro-kotlin.avro4k:avro4k-core:2.8.0")
        implementation("net.benwoodworth.knbt:knbt:0.11.9")
        implementation("com.ensarsarajcic.kotlinx:serialization-msgpack:0.6.0")
        implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
        implementation("app.softwork:kotlinx-serialization-csv:0.0.23")
        implementation("com.jsoizo:kotlinx-serialization-php:0.1.0")
        implementation("li.songe:json5:0.5.0")
        implementation("io.github.pdvrieze.xmlutil:core-jdk:1.0.0-rc2")

        if (githubCredentials.exists()) {
            implementation("com.zhelenskiy:serialization:1.0")
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        groupId = "me.maly.y9to"
        artifactId = "paging"
        version = "1.0-SNAPSHOT"
        if (name != "kotlinMultiplatform")
            artifactId += "-$name"
    }

    repositories {
        mavenLocal()
    }
}
