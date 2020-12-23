

rootProject.name = "auto-include"

pluginManagement {
    repositories {
        // load self from local repo
        maven(url = rootDir.resolve("repo"))
        gradlePluginPortal()
    }
    @Suppress("UnstableApiUsage")
    plugins {
        id("de.fayard.refreshVersions") version "0.9.7"
    }
}

plugins {
    // TODO: Change for this plugin instead
    id("com.pablisco.gradle.automodule") version "0.15"
}

autoModule {
    // Ignore tests cases and build folder
    ignore(":plugin:build")
    ignore(":demos")
    ignore(":gradle")
    ignore(":plugin:src:test:resources")
    ignore(":plugin:out")
    ignore(":plugin:src")

    pluginRepository(rootDir.resolve("repo"))
}
