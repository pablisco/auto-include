rootProject.name = "simple_module"

// Enable for local testing
//pluginManagement {
//    repositories {
//        maven(url = rootDir.resolve("../../repo"))
//        mavenCentral()
//    }
//}

plugins {
    id("com.pablisco.gradle.auto.include") version "0.15"
}

// Enable for local testing
//auto.include {
//    pluginRepository(rootDir.resolve("../../repo"))
//}
