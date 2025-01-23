plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.com.service"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.json:json:20210307")
}

tasks.test {
    useJUnitPlatform()
}