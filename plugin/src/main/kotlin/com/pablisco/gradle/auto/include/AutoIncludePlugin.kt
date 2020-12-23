package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.utils.log
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.io.File

private const val version = "1.0"

public class AutoIncludePlugin : Plugin<Settings> {

    private val autoInclude = AutoInclude()

    override fun apply(target: Settings) {
        target.extensions.add("autoInclude", autoInclude)
        SettingsScope(autoInclude, target).whenEvaluated {
            notifyIgnoredModules()
            includeModulesToSettings()
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

private fun SettingsScope.notifyIgnoredModules() {
    if (autoInclude.ignored.isNotEmpty()) {
        log("Ignoring modules: ${autoInclude.ignored}")
    }
}

private fun SettingsScope.includeModulesToSettings() {
    rootModule.walk().mapNotNull { it.path }
        .filterNot { path -> autoInclude.ignored.any { shouldIgnore -> shouldIgnore(path) } }
        .forEach { path ->
            log("include($path)")
            include(path)
        }
}
