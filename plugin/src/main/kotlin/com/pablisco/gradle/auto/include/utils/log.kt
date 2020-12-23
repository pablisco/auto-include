package com.pablisco.gradle.auto.include.utils

import com.pablisco.gradle.auto.include.AutoInclude
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

internal fun log(message: String, logLevel: LogLevel = LogLevel.INFO) {
    logger.log(logLevel, "[Auto-Module] $message")
}

private val logger: Logger by lazy { Logging.getLogger(AutoInclude::class.java) }
