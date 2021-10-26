import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import proguard.gradle.ProGuardTask

val title = "Robocode Tank Royale Bot Booter"
description = "Utility app for booting up bots from locale storage onto websocket"

group = "dev.robocode.tankroyale"
version = "0.8.3"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-booter-$version.jar"


buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.1.1")
    }
}

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    idea
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

idea {
    module {
        outputDir = file("$buildDir/classes/kotlin/main")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
    implementation("info.picocli:picocli:4.6.1")
}

tasks.processResources {
    with(copySpec {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("/src/main/resources")
        include("version.txt")
        filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
    })
}

val fatJar = task<Jar>("fatJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
        attributes["Main-Class"] = "dev.robocode.tankroyale.booter.BooterKt"
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    exclude("*.kotlin_metadata")
    with(tasks["jar"] as CopySpec)
    archiveFileName.set("fat.jar")
}

val proguard = task<ProGuardTask>("proguard") {
    dependsOn(fatJar)
    injars("$buildDir/libs/fat.jar")
    outjars(archiveFileName)
    configuration("proguard-rules.pro")
}

tasks.named("build") {
    dependsOn(proguard)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId
            version
        }
    }
}
