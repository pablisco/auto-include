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
        target.gradle.settingsEvaluated {
            val buildModulesRoot = rootDir.resolve(autoInclude.buildModulesRoot)
            candidateModules().forEach { dir ->
                val coordinates = dir.relativeTo(rootDir).toGradlePath()
                when {
                    autoInclude.shouldIgnore(coordinates) -> log("Ignoring module: $coordinates")
                    dir.startsWith(buildModulesRoot) -> includeBuildModule(dir)
                    else -> include(coordinates)
                }
            }
            addDebugArtifactRepository(autoInclude)
        }
    }
}

private fun Settings.includeBuildModule(dir: File) {
    log("Including build module: $dir")
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

private fun Settings.addDebugArtifactRepository(autoInclude: AutoInclude) = gradle.rootProject {
    buildscript {
        repositories {
            autoInclude.pluginRepository?.let { maven(url = it) }
            gradlePluginPortal()
        }
    }
}

private fun Settings.candidateModules(): Sequence<File> =
    rootDir.walkTopDown()
        .onEnter { !it.shouldStop() }
        .filterNot { it.isDirectory }
        .filter { it.isBuildScript() }
        .map { it.parentFile }
        .filterNot { it == rootDir }

private fun File.shouldStop() = stopFolders.any { shouldStop -> shouldStop(this) }

private fun AutoInclude.shouldIgnore(coordinates: String) =
    ignored.any { ignore -> ignore(coordinates) }

private val stopFolders = listOf<(File) -> Boolean>(
    { "buildSrc" == it.name },
    { it.name.isEmpty() },
    { it.name.startsWith(".") },
    { "build" == it.name },
    { "src" == it.name },
    { "out" == it.name }
)

private fun File.isBuildScript() = when (name) {
    "build.gradle.kts" -> true
    "build.gradle" -> true
    else -> false
}

internal fun File.toGradlePath(): String =
    ':' + path.replace(File.separatorChar, ':')