import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyTemplate
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinWithJavaTarget
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    kotlin("multiplatform")
}

@OptIn(ExperimentalWasmDsl::class, ExperimentalKotlinGradlePluginApi::class)
kotlin {
    jvm {
        compilerOptions.jvmTarget = JvmTarget.JVM_11
    }

    js(IR) {
        binaries.library()
        useEsModules()
        generateTypeScriptDefinitions()
        browser()
        nodejs()
    }

    wasmJs {
        browser()
        nodejs()
    }

    wasmWasi {
        nodejs()
        binaries.library()
    }

    linuxX64()
    linuxArm64()
//    macosX64()
    macosArm64()
    mingwX64()
//    androidNativeX86()
//    androidNativeX64()
//    androidNativeArm32()
//    androidNativeArm64()
//    iosX64()
    iosArm64()
    iosSimulatorArm64()
//    watchosX64()
//    watchosArm32()
//    watchosArm64()
//    watchosSimulatorArm64()
//    watchosDeviceArm64()
//    tvosX64()
//    tvosArm64()
//    tvosSimulatorArm64()

    sourceSets.applyHierarchy()
}

fun NamedDomainObjectContainer<KotlinSourceSet>.applyHierarchy() {
    applyHierarchy("Main")
    applyHierarchy("Test")
}

fun NamedDomainObjectContainer<KotlinSourceSet>.applyHierarchy(
    suffix: String,
    common: KotlinSourceSet = named("common$suffix").get(),
    jvm: KotlinSourceSet = named("jvm$suffix").get(),
    js: KotlinSourceSet = named("js$suffix").get(),
    wasmJs: KotlinSourceSet = named("wasmJs$suffix").get(),
    wasmWasi: KotlinSourceSet = named("wasmWasi$suffix").get(),
    linuxX64: KotlinSourceSet = named("linuxX64$suffix").get(),
    linuxArm64: KotlinSourceSet = named("linuxArm64$suffix").get(),
    macosArm64: KotlinSourceSet = named("macosArm64$suffix").get(),
    mingwX64: KotlinSourceSet = named("mingwX64$suffix").get(),
    iosArm64: KotlinSourceSet = named("iosArm64$suffix").get(),
    iosSimulatorArm64: KotlinSourceSet = named("iosSimulatorArm64$suffix").get(),
) {
    val web = create("web$suffix") {
        dependsOn(common)
    }

    val wasm = create("wasm$suffix") {
        dependsOn(common)
    }

    val linux = create("linux$suffix") {
        dependsOn(common)
    }

    val macos = create("macos$suffix") {
        dependsOn(common)
    }

    val ios = create("ios$suffix") {
        dependsOn(common)
    }

    val native = create("native$suffix") {
        dependsOn(common)
    }

    val apple = create("apple$suffix") {
        dependsOn(common)
    }

    val nonJvm = create("nonJvm$suffix") {
        dependsOn(common)
    }

    val nonJs = create("nonJs$suffix") {
        dependsOn(common)
    }

    val nonWasmJs = create("nonWasmJs$suffix") {
        dependsOn(common)
    }

    val nonWasmWasi = create("nonWasmWasi$suffix") {
        dependsOn(common)
    }

    val nonWasm = create("nonWasm$suffix") {
        dependsOn(common)
    }

    val nonWeb = create("nonWeb$suffix") {
        dependsOn(common)
    }

    val nonMacos = create("nonMacos$suffix") {
        dependsOn(common)
    }

    val nonMingw = create("nonMingw$suffix") {
        dependsOn(common)
    }

    val nonIos = create("nonIos$suffix") {
        dependsOn(common)
    }

    val nonLinux = create("nonLinux$suffix") {
        dependsOn(common)
    }

    val nonNative = create("nonNative$suffix") {
        dependsOn(common)
    }

    // dependencies

    js.dependsOn(web)
    wasm.dependsOn(web)
    wasmJs.dependsOn(wasm)
    wasmWasi.dependsOn(wasm)

    apple.dependsOn(native)
    macos.dependsOn(apple)
    macosArm64.dependsOn(macos)
    ios.dependsOn(apple)
    iosArm64.dependsOn(ios)
    iosSimulatorArm64.dependsOn(ios)

    mingwX64.dependsOn(native)
    linux.dependsOn(native)
    linuxX64.dependsOn(linux)
    linuxArm64.dependsOn(linux)

    // non-dependencies

    with(nonJvm) {
        web.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonJs) {
        jvm.dependsOn(this)
        wasm.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonWasmJs) {
        jvm.dependsOn(this)
        js.dependsOn(this)
        wasmWasi.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonWasmWasi) {
        jvm.dependsOn(this)
        js.dependsOn(this)
        wasmJs.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonWasm) {
        jvm.dependsOn(this)
        js.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonWeb) {
        jvm.dependsOn(this)
        native.dependsOn(this)
    }

    with(nonMacos) {
        jvm.dependsOn(this)
        web.dependsOn(this)
        linux.dependsOn(this)
        mingwX64.dependsOn(this)
        ios.dependsOn(this)
    }

    with(nonMingw) {
        jvm.dependsOn(this)
        web.dependsOn(this)
        linux.dependsOn(this)
        apple.dependsOn(this)
    }

    with(nonIos) {
        jvm.dependsOn(this)
        web.dependsOn(this)
        macos.dependsOn(this)
        mingwX64.dependsOn(this)
        ios.dependsOn(this)
    }

    with(nonLinux) {
        jvm.dependsOn(this)
        web.dependsOn(this)
        apple.dependsOn(this)
        mingwX64.dependsOn(this)
    }

    with(nonNative) {
        web.dependsOn(this)
        jvm.dependsOn(this)
    }
}
