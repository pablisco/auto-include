package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.utils.isDirectory
import com.pablisco.gradle.auto.include.utils.log
import com.pablisco.gradle.auto.include.utils.name
import com.pablisco.gradle.auto.include.utils.toGradlePath
import com.pablisco.gradle.auto.include.utils.walk
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.io.File
import java.nio.file.Path

private const val version = "1.0"

public class AutoIncludePlugin : Plugin<Settings> {

    private val autoInclude = AutoInclude()

    override fun apply(target: Settings) {
        target.extensions.add("autoInclude", autoInclude)
        SettingsScope(autoInclude, target).whenEvaluated {
            includeModulesToSettings(autoInclude)
            includeBuildModules()
            addDebugArtifactRepository()
        }
    }

}

private fun SettingsScope.addDebugArtifactRepository() = gradle.rootProject {
    buildscript {
        repositories {
            autoInclude.pluginRepository?.let { maven(url = it) }
            gradlePluginPortal()
        }
    }
}

private fun SettingsScope.includeBuildModules() {
    val buildModulesRoot = rootDir.resolve(autoInclude.buildModulesRoot)

    val buildModules = buildModulesRoot.children()
        .filter { it.isDirectory }
        .filter { dir -> dir.children().any { it.name == "build.gradle.kts" } }
    buildModules.forEach { dir ->
        includeBuild(dir) {
            dependencySubstitution {
                substitute(module("gradle:${dir.name}:local")).with(project(":"))
            }
        }
        gradle.rootProject {
            buildscript {
                dependencies {
                    classpath("gradle:${dir.name}:local")
                }
            }
        }
    }
}

private fun File.children(): List<File> = listFiles()?.toList() ?: emptyList()

private fun SettingsScope.whenEvaluated(f: SettingsScope.() -> Unit) {
    gradle.settingsEvaluated { f() }
}

private fun Settings.includeModulesToSettings(autoInclude: AutoInclude) {
    val root = rootDir.toPath()
    root.walk()
        .filterNot { it.name == "buildSrc" }
        .filterNot { it.name.isEmpty() }
        .filterNot { it.name.startsWith(".") }
        .filterNot { it.isDirectory() }
        .filter { it.isBuildScript() }
        .map { root.relativize(it.parent).toGradlePath() }
        .forEach { coordinates ->
            if(autoInclude.ignored.any { ignore -> ignore(coordinates) }) {
                log("Ignoring module: $coordinates")
            } else {
                include(coordinates)
            }
        }
}

private fun Path.isBuildScript() = when(name) {
    "build.gradle.kts" -> true
    "build.gradle" -> true
    "$parent.gradle" -> true
    "$parent.gradle.kts" -> true
    else -> false
}
