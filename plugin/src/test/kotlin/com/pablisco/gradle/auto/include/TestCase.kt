package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.utils.createDirectories
import com.pablisco.gradle.auto.include.utils.readText
import com.pablisco.gradle.auto.include.utils.write
import org.jetbrains.kotlin.konan.file.recursiveCopyTo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.streams.asSequence

class TestCase(
    path: String,
    workingPath: String,
    projectDir: Path = Paths.get(".").toAbsolutePath(),
    val testCaseDir: Path = projectDir.resolve("src/test/resources/test-cases/$path"),
    val workingDir: Path = projectDir.resolve("build/test-workspace/$workingPath")
)

internal fun testCase(
    path: String,
    workingPath: String = path,
    block: TestCase.() -> Unit
) {
    TestCase(path, workingPath).apply {
        workingDir.deleteRecursively()
        workingDir.createDirectories()
        testCaseDir.recursiveCopyTo(workingDir)
        workingDir.addLocalRepository()
    }.run(block)
}

internal fun Path.addLocalRepository() {
    with(resolve("settings.gradle.kts")) {
        write(
            """
                pluginManagement {
                    plugins {
                        kotlin("jvm") version "1.4.21"
                    }
                }
                
                ${readText()}
                
                autoInclude { pluginRepository = file("${Paths.get("../repo").toRealPath()}") }
            """.trimIndent(),
            StandardOpenOption.WRITE
        )
    }
}

internal fun Path.deleteRecursively() {
    if (exists()) {
        walk().sortedDescending().forEach(Path::delete)
    }
}

internal fun Path.exists(): Boolean = Files.exists(this)

internal fun Path.delete() = Files.delete(this)

internal fun Path.walk(): Sequence<Path> =
    sequence { yieldAll(Files.walk(this@walk).asSequence()) }
