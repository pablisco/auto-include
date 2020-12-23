package com.pablisco.gradle.include

import com.pablisco.gradle.auto.include.filetree.fileTree
import com.pablisco.gradle.include.gradle.kotlinModule
import com.pablisco.gradle.include.gradle.runGradleProjects
import com.pablisco.gradle.include.gradle.shouldBeSuccess
import com.pablisco.gradle.include.utils.createDirectories
import com.pablisco.gradle.include.utils.deleteRecursively
import com.pablisco.gradle.include.utils.readText
import com.pablisco.gradle.include.utils.write
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.jetbrains.kotlin.konan.file.recursiveCopyTo
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime

class AutoIncludePluginTest {

    @Test
    fun `generates modules code WITH simple module`() = testCase("simple_module") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH multiple modules`() = testCase("multiple_modules") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `generates modules code WITH nested modules`() = testCase("nested_modules") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `ignores modules`() = testCase("ignore_modules") {
        val result = workingDir.runGradleProjects()

        result.output shouldNotContain "Project ':settingsKtIgnored'"
        result.output shouldNotContain "Project ':extensionIgnored'"
        result.output shouldContain "Project ':included'"
    }

    @Test
    fun `can use from groovy gradle script`() = testCase("groovy_support") {
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
    }

    @Test
    fun `files are generated after changing script with cache enabled`() = testCase(
        path = "simple_module",
        workingPath = "files_are_generated_after_script_changes"
    ) {
        workingDir.runGradleProjects()

        workingDir.fileTree().kotlinModule("newModule")

        val output = workingDir.runGradleProjects().output

        output shouldContain "Project ':newModule'"
    }

    @Test
    fun `classpath includes build modules`() = testCase("build_modules") {
        workingDir.resolve("gradle/buildStuff").addLocalRepository()
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
        result.output shouldContain "Hello Gradle!"
    }

}
