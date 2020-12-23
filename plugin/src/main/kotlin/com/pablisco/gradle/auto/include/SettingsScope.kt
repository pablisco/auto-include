package com.pablisco.gradle.auto.include

import org.gradle.api.initialization.Settings

internal class SettingsScope(
    val autoInclude: AutoInclude,
    settings: Settings
) : Settings by settings {

    val rootModule: ModuleNode by lazy {
        rootDir.toPath().rootModule(
            ignored = autoInclude.ignored,
            name = rootProject.name
        )
    }

}
