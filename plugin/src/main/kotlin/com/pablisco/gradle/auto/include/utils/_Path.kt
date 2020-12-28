package com.pablisco.gradle.auto.include.utils

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.FileAttribute

internal fun Path.directoryStream(filter: (Path) -> Boolean): Sequence<Path> =
    Files.newDirectoryStream(this, filter).asSequence()

internal fun Path.walk(
    onEachFile: (Path) -> Unit,
    continueWhen: (Path) -> Boolean
) = Files.walkFileTree(this, object : SimpleFileVisitor<Path>() {
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = when {
        continueWhen(dir) -> super.preVisitDirectory(dir, attrs)
        else -> FileVisitResult.SKIP_SUBTREE
    }

    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        onEachFile(file)
        return super.visitFile(file, attrs)
    }
})

internal fun Path.isDirectory() = Files.isDirectory(this)

internal fun Path.readText(): String = Files.readAllLines(this).joinToString("\n")

internal fun Path.write(bytes: ByteArray, vararg openOption: OpenOption): Path =
    Files.write(this, bytes, *openOption)

internal fun Path.write(string: String, vararg openOption: OpenOption): Path =
    write(string.toByteArray(), *openOption)

internal fun Path.createDirectories(
    vararg fileAttributes: FileAttribute<*> = emptyArray()
): Path = Files.createDirectories(this, *fileAttributes)

internal fun Path.exists() = Files.exists(this)
