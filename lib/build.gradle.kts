import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    id("maven-publish")
}

group = "com.gitlab.silmeth"
version = "0.1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

kotlin {
    withSourcesJar(publish = true)

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    linuxX64()

    fun kotlinx(name: String, version: String): String = "org.jetbrains.kotlinx:kotlinx-$name:$version"
    fun kotlinxSerialization(name: String) = kotlinx("serialization-$name", "1.6.2")

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }

        val commonMain by getting {
            dependencies {
                api(kotlinxSerialization("core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        if (name == "kotlinMultiplatform") {
            setArtifactId("pocztowka")
        } else {
            setArtifactId("pocztowka-$name")
        }
    }
}
