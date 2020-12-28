package com.pablisco.gradle.auto.include

import com.pablisco.gradle.auto.include.filetree.fileTree
import com.pablisco.gradle.auto.include.gradle.kotlinModule
import com.pablisco.gradle.auto.include.gradle.runGradleProjects
import com.pablisco.gradle.auto.include.gradle.shouldBeSuccess
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotContain
import org.junit.jupiter.api.Test

class AutoIncludePluginTest {

    @Test
    fun `includes modules code WITH simple module`() = testCase("simple_module") {
        val result = workingDir.runGradleProjects()
        result.shouldBeSuccess()
    }

    @Test
    fun `includes modules code WITH multiple modules`() = testCase("multiple_modules") {
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
        val output = workingDir.runGradleProjects().output
        output shouldContain "Project ':consumer'"
        output shouldContain "Project ':libraryOne'"
        output shouldContain "Project ':libraryTwo'"
    }

    @Test
    fun `includes modules code WITH module name scripts`() = testCase("custom_script_modules") {
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()
        val output = workingDir.runGradleProjects().output
        output shouldContain "Project ':consumer'"
        output shouldContain "Project ':libraryOne'"
        output shouldContain "Project ':libraryTwo'"
    }

    @Test
    fun `includes modules code WITH nested modules`() = testCase("nested_modules") {
        val result = workingDir.runGradleProjects()

        result.shouldBeSuccess()

        val output = workingDir.runGradleProjects().output
        output shouldContain "Project ':consumer'"
        output shouldContain "Project ':group'"
        output shouldContain "Project ':group:library'"
        output shouldContain "Project ':group:library:nested'"
        output shouldContain "Project ':library'"
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
    fun `new module is included`() = testCase(
        path = "simple_module",
        workingPath = "new_module_is_included"
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
