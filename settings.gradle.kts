dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs")
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}



rootProject.name = "cbor"
include(":lib")

