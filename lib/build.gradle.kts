plugins {
    kotlin("multiplatform") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"

}

kotlin {

    jvm {
        jvmToolchain(17)
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                enableLanguageFeature("ContextReceivers") // language feature name
                optIn("kotlin.io.encoding.ExperimentalEncodingApi")
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.contracts.ExperimentalContracts")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("com.squareup.okio:okio:3.6.0")
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting{
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter:5.10.1")
            }
        }


    }
}