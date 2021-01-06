package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.VIPaths.build
import com.pablisco.gradle.auto.include.VIPaths.buildSrc
import com.pablisco.gradle.auto.include.VIPaths.groovyBuildScript
import com.pablisco.gradle.auto.include.VIPaths.kotlinBuildScript
import com.pablisco.gradle.auto.include.VIPaths.out
import com.pablisco.gradle.auto.include.VIPaths.src
import com.pablisco.gradle.auto.include.utils.isHidden
import com.pablisco.gradle.auto.include.utils.isReadable
import com.pablisco.gradle.auto.include.utils.isSymbolicLink
import com.pablisco.gradle.auto.include.utils.log
import com.pablisco.gradle.auto.include.utils.walk
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.buildscript
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.nio.file.Path
import java.nio.file.Paths

public class AutoIncludePlugin : Plugin<Settings> {

    private val autoInclude = AutoInclude()

    override fun apply(target: Settings) {
        target.extensions.add("autoInclude", autoInclude)
        target.gradle.settingsEvaluated {
            includeModules(autoInclude)
            addDebugArtifactRepository(autoInclude)
        }
    }
}

private fun Settings.includeModules(autoInclude: AutoInclude) {
    val root = rootDir.toPath()
    val buildModulesRoot = root.resolve(autoInclude.buildModulesRoot)
    root.walk(
        onEachFile = { file ->
            if (file.isBuildScript() && file.parent != root) {
                val module = file.parent
                val coordinates = root.relativize(module).toGradleNotation()
                when {
                    autoInclude.shouldIgnore(coordinates) -> log("Ignoring module: $coordinates")
                    module.startsWith(buildModulesRoot) -> includeBuildModule(module)
                    else -> include(coordinates)
                }
            }
        },
        continueWhen = { dir -> !dir.shouldStop() }
    )
}

private fun Settings.includeBuildModule(dir: Path) {
    log("Including build module: $dir")
    val notation = "gradle:${dir.fileName}:local"
    includeBuild(dir) {
        dependencySubstitution {
            substitute(module(notation)).with(project(":"))
        }
    }
    gradle.rootProject {
        buildscript {
            dependencies {
                classpath(notation)
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

private fun Path.shouldStop() = stopFolders.any { shouldStop -> shouldStop(this) }

private fun AutoInclude.shouldIgnore(coordinates: String) =
    ignored.any { ignore -> ignore(coordinates) }

private val stopFolders = listOf<(Path) -> Boolean>(
    { it.fileName == buildSrc },
    { it.fileName.nameCount == 0 },
    { it.isHidden() },
    { it.isSymbolicLink() },
    { !it.isReadable() },
    { it.fileName == build },
    { it.fileName == src },
    { it.fileName == out }
)

private object VIPaths {
    val buildSrc: Path = Paths.get("buildSrc")
    val build: Path = Paths.get("build")
    val src: Path = Paths.get("src")
    val out: Path = Paths.get("out")
    val kotlinBuildScript: Path = Paths.get("build.gradle.kts")
    val groovyBuildScript: Path = Paths.get("build.gradle")
}

private fun Path.isBuildScript() = when {
    fileName == kotlinBuildScript -> true
    fileName == groovyBuildScript -> true
    endsWith(parent.resolve("${parent.fileName}.gradle.kts")) -> true
    endsWith(parent.resolve("${parent.fileName}.gradle")) -> true
    else -> false
}

internal fun Path.toGradleNotation(): String = joinToString(separator = ":", prefix = ":")