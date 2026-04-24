plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.rpc.plugin") version libs.versions.kotlinx.rpc
    id("com.gradleup.shadow")
    application
}

application {
    mainClass = "container.monolith.MainKt"
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")
kotlin.compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")

dependencies {
    implementation(project(":presentation:api:controller-default"))
    implementation(project(":presentation:api:krpc-default"))
    implementation(project(":presentation:gateway:ktor-krpc"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation(project(":presentation:integration:updateProvider"))
    implementation(project(":presentation:integration:updateProvider-updateManager"))
    implementation(project(":presentation:integration:updateProducer"))
    implementation(project(":presentation:integration:updateProducer-updateManager"))
    implementation(project(":presentation:integration:updateSubscriptionsStore"))
    implementation(project(":presentation:integration:updateSubscriptionsStore-redis"))
    implementation(project(":presentation:integration:tokenProvider"))
    implementation(project(":presentation:integration:tokenProvider-jwtManager"))
    implementation(project(":presentation:integration:authenticator"))
    implementation(project(":presentation:integration:authenticator-jwtManager"))
    implementation(project(":presentation:integration:assembler-presenter-impl"))
    implementation(project(":presentation:infrastructure:updateManager"))
    implementation(project(":presentation:infrastructure:updateManager-impl"))
    implementation(project(":presentation:infrastructure:jwtManager"))
    implementation(project(":presentation:infrastructure:jwtManager-impl"))
    implementation(project(":presentation:workers:updatePublisher"))
    implementation(project(":backend:domain:service-impl"))
    implementation(project(":backend:integration:eventCollector"))
    implementation(project(":backend:integration:eventCollector-kafka"))
    implementation(project(":backend:integration:selector-impl"))
    implementation(project(":backend:integration:data:repository-postgres"))
    implementation(project(":backend:integration:data:fileStorage-local"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("ch.qos.logback:logback-classic:1.5.27")
    implementation("org.apache.kafka:kafka-clients:4.2.0")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("io.lettuce:lettuce-core:7.5.1.RELEASE")
    implementation("io.nats:jnats:2.25.2")

    val ktorVersion = "3.4.2"
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    val exposedVersion = "1.2.0"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-r2dbc:$exposedVersion")
    implementation("com.h2database:h2:2.4.240")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")
}

tasks.named<Tar>("distTar") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

tasks.named<Zip>("distZip") {
    duplicatesStrategy = DuplicatesStrategy.WARN
}
