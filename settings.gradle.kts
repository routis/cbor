pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/amper/amper")
    }
}

plugins {
    id("org.jetbrains.amper.settings.plugin").version("0.1.0")
}
// apply the plugin:
plugins.apply("org.jetbrains.amper.settings.plugin")

rootProject.name = "cbor-kmp"
