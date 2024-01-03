import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    `maven-publish`
}

apply {
    plugin("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
}

group = "com.gitlab.silmeth"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        apiVersion = "1.7"
        languageVersion = "1.7"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest()
        }
    }
}

val sourceJar = task<Jar>("sourceJar") {
    from(sourceSets["main"].allSource)
    archiveClassifier.set("sources")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "pocztowka"
            version = "0.1.1"
            from(components["java"])
            artifact(sourceJar)
        }
    }
}
