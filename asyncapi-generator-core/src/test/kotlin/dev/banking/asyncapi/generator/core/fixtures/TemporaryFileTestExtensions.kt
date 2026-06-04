package dev.banking.asyncapi.generator.core.fixtures

import java.io.File
import java.nio.file.Path

internal fun Path.writeTestFile(
    name: String,
    content: String,
): File {
    val file = resolve(name).toFile()
    file.parentFile?.mkdirs()
    file.writeText(content)
    return file
}
