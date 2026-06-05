package dev.banking.asyncapi.generator.core.fixtures

import java.io.File
import java.nio.file.Path

/**
 * Creates a temporary test file under this directory.
 *
 * This is intended for IO-bound tests where a real filesystem path matters,
 * such as format detection through file extensions.
 */
internal fun Path.writeTestFile(
    name: String,
    content: String,
): File {
    val file = resolve(name).toFile()
    file.parentFile?.mkdirs()
    file.writeText(content)
    return file
}
