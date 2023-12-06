plugins {
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    kotlin("plugin.serialization").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.ktlint).apply(false)
}

allprojects {
    group = "io.github.routis"
    version = "0.0.1-SNAPSHOT"
}

subprojects {

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        outputToConsole.set(true)

    }
}
