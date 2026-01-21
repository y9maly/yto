plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.main.get().kotlin.srcDir("src")

dependencies {
    api(project(":backend:domain:service-impl"))
    api(project(":backend:integration:selector-impl"))
    api(project(":backend:integration:data:repository-postgres"))
    api(project(":backend:infrastructure:postgres"))

    implementation("com.h2database:h2:2.4.240")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")
    implementation("com.zaxxer:HikariCP:5.0.1")
}
