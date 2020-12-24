package com.pablisco.gradle.auto.include.utils

import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.attribute.FileAttribute

internal fun Path.readText(): String = Files.readAllLines(this).joinToString("\n")

internal fun Path.write(bytes: ByteArray, vararg openOption: OpenOption): Path =
    Files.write(this, bytes, *openOption)

internal fun Path.write(string: String, vararg openOption: OpenOption): Path =
    write(string.toByteArray(), *openOption)

internal fun Path.createDirectories(
    vararg fileAttributes: FileAttribute<*> = emptyArray()
): Path = Files.createDirectories(this, *fileAttributes)



