import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("io.qameta.allure") version "2.8.1"
}

group = "ru.vladimirsofin"
version = "1.0-SNAPSHOT"
val versionKotest = "5.2.3"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.kotest:kotest-runner-junit5:$versionKotest")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

//allure {
//    autoconfigure = false
//    version = "2.13.1"
//}