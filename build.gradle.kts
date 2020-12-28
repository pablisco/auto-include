buildscript {
    repositories {
        maven(url = rootDir.resolve("repo"))
        jcenter()
        gradlePluginPortal()
    }
}

plugins {
    id("com.github.blueboxware.tocme")
    id("de.fayard.refreshVersions")
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
    }
    group = "com.pablisco.gradle.auto.include"
    version = "1.1"
}

tasks {
    create<Delete>("clean") {
        delete(allprojects.map { it.buildDir })
    }
}

tocme {
    doc(file("readme.md"))
}
