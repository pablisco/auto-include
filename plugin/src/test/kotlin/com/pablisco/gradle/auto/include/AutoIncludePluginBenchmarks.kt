package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.filetree.FileTreeScope
import com.pablisco.gradle.auto.include.filetree.fileTree
import com.pablisco.gradle.auto.include.gradle.defaultSettingsGradleScript
import com.pablisco.gradle.auto.include.gradle.runGradle
import com.pablisco.gradle.auto.include.utils.createDirectories
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class AutoIncludePluginBenchmarks {

    @Test
    fun `measure manual vs AutoInclude`() = measure {
        testCase("benchmark") {
            val manualProjectDir = workingDir.resolve("manual").createDirectories()
            val autoModuleProjectDir = workingDir.resolve("auto-include").createDirectories()

            manualProjectDir.fileTree {
                val modules = generateModules()
                manualSettings(modules)
                "build.gradle.kts" += """
                plugins { kotlin("jvm") version "1.4.21" }
                dependencies {
                    ${dependencies(modules.asManualNotation())}
                }
            """.trimIndent()
                createModules(modules)
            }

            autoModuleProjectDir.fileTree {
                val modules = generateModules()
                "settings.gradle.kts" += defaultSettingsGradleScript
                "build.gradle.kts" += """
                buildscript {
                    repositories {
                        jcenter()
                        mavenLocal()
                    }
                    dependencies {
                        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
                    }
                }
                
                allprojects {
                    repositories {
                        mavenCentral()
                        jcenter()
                    }
                }
            """.trimIndent()
                "consumer/build.gradle.kts" += """
                plugins { kotlin("jvm") }
                dependencies {
                    ${dependencies(modules.asManualNotation())}
                }
            """.trimIndent()
                createModules(modules)
            }

            "Warm up - Manual build" { manualProjectDir.runGradle() }
            "Warm up - AutoInclude build" { autoModuleProjectDir.runGradle() }

            "Manual build"(runCount = 10) { manualProjectDir.runGradle() }
            "AutoInclude build"(runCount = 10) { autoModuleProjectDir.runGradle() }
        }
    }

}

private fun generateModules(): Sequence<String> =
    (0..1_000).asSequence().map { "module$it" }

private fun FileTreeScope.manualSettings(modules: Sequence<String>) {
    "settings.gradle.kts" += modules
        .asManualNotation()
        .joinToString("\n") { """include($it)""" }
}

private fun FileTreeScope.createModules(modules: Sequence<String>) {
    modules.forEach { moduleName ->
        moduleName {
            emptyFile("build.gradle.kts")
        }
    }
}

private fun dependencies(modules: Sequence<String>): String =
    modules.joinToString("\n") { """implementation(project($it))""" }

private fun Sequence<String>.asManualNotation(): Sequence<String> =
    map { "\":$it\"" }

private fun measure(block: MeasureScope.() -> Unit) {
    println(MeasureScope().apply(block).log)
}

private class MeasureScope {

    var log = "[Measurements]\n"

    operator fun String.invoke(block: () -> Unit) {
        val timeTaken = measureTimeMillis(block)
        log += "\t$this: ${timeTaken}ms\n"
    }

    operator fun String.invoke(runCount: Int, block: () -> Unit) {
        val times = (0 until runCount).map { measureTimeMillis(block) }
        val max = times.max() ?: -1
        val min = times.min() ?: -1
        val average = times.average()
        val error = max - min
        log += "\t$this: avg ${average}ms, max ${max}ms, min ${min}ms, error ${error}ms\n"
    }

}
