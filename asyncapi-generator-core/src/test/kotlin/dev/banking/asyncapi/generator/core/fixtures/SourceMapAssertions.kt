package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.reader.InputDocument
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Assertion helper for source-map expectations produced by document readers.
 *
 * Use this when a test needs to verify that an [InputDocument] maps a document
 * path back to the expected source identity, file, line, and non-empty column.
 */
internal fun InputDocument.assertSourceLocation(
    path: String,
    line: Int,
) {
    val location = assertNotNull(sourceMap[path], "Expected source location for $path")
    assertEquals(source.id, location.sourceId)
    assertEquals(source.file, location.file)
    assertEquals(path, location.path)
    assertEquals(line, location.line)
    assertTrue(location.column >= 1)
}
