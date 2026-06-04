package dev.banking.asyncapi.generator.core.reader

import java.io.File

/**
 * Location of a value in an input document.
 *
 * Expected behavior is covered by:
 * - `SourceMapTest`
 */
data class SourceLocation(
    val sourceId: String,
    val file: File,
    val path: String,
    val line: Int,
    val column: Int,
)
