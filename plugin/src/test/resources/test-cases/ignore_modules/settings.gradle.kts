plugins {
    id("com.pablisco.gradle.auto.include")
}

autoInclude {
    ignore(":settingsKtIgnored")
    ignore { it.endsWith("IgnoreMe") }
}
