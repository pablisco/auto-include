plugins {
    id("de.fayard.refreshVersions")
    kotlin("jvm")
    idea
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.palantir.idea-test-fix")
    id("com.gradle.plugin-publish")
}

dependencies {
    implementation(libs.kotlin.jdk8)
    implementation(libs.kotlin.io)
    implementation(libs.kotlinPoet)

    testImplementation(tests.junit5.jupiter)
    testImplementation(tests.junit5.jupiterApi)
    testImplementation(tests.junit5.jupiterParams)
    testImplementation(tests.kluent)
    testImplementation(gradleTestKit())
}

kotlin {
    explicitApi()
}

tasks {
    test {
        useJUnitPlatform()
    }
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    val copyTestResources by registering(Copy::class) {
        from("${projectDir}/src/test/resources")
        into("${buildDir}/classes/kotlin/test")
    }

    processTestResources.configure {
        dependsOn(copyTestResources)
    }

    "publishPlugins" {
        onlyIf { version !in AutoModuleMavenMetadata.versions }
    }
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.map { it.allSource })
}

pluginBundle {
    website = "https://github.com/pablisco/auto-include/"
    vcsUrl = "https://github.com/pablisco/auto-include/"
    tags = listOf("auto.include")
}

gradlePlugin {
    plugins {
        create("auto-include-plugin") {
            id = "com.pablisco.gradle.auto.include"
            displayName = "Auto Include"
            description = "A Gradle plugin to include modules into gradle automatically"
            implementationClass = "com.pablisco.gradle.auto.include.AutoIncludePlugin"
        }
    }
}

publishing {
    repositories {
        maven(url = rootDir.resolve("repo"))
    }
}

afterEvaluate {
    publishing.publications.withType<MavenPublication>()
        .configureEach {
            versionMapping {
                allVariants {
                    fromResolutionResult()
                }
            }
        }
}

idea {
    module {
        // hides test kts files so they are not parsed by the IDE
        excludeDirs = setOf(file("src/test/resources/test-cases"))
    }
}
