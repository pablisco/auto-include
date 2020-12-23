package com.pablisco.gradle.auto.include

import java.io.File

public data class AutoInclude internal constructor(
    var buildModulesRoot: String = "gradle",
    /**
     * **absolute** paths to any module that we should be to ignore.
     */
    internal val ignored: MutableList<(String) -> Boolean> = mutableListOf(),
    /**
     * Used, in development, to add a repository to the generated code to look for itself.
     */
    public var pluginRepository: File? = null
) {

    @Suppress("unused") // Api
    public fun ignore(path: String) {
        ignore { it == path }
    }

    @Suppress("unused") // Api
    public fun ignore(condition: (String) -> Boolean) {
        ignored += condition
    }

    @Suppress("unused") // Api
    public fun ignore(condition: Regex) {
        ignore(condition::matches)
    }

}
