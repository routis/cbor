plugins {
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    kotlin("plugin.serialization").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.ktlint).apply(false)
//    alias(libs.plugins.detekt).apply(false)
}

allprojects {
    group = "io.github.routis"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}

subprojects {

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        ignoreFailures.set(true)
    }


}
