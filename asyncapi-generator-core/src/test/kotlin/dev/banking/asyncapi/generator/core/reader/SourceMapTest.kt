package dev.banking.asyncapi.generator.core.reader

import dev.banking.asyncapi.generator.core.fixtures.ReaderFixtures
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SourceMapTest {

    private val reader = YamlDocumentReader()

    @Test
    fun `records source locations for root object fields and array items`() {
        val source = ReaderFixtures.yamlSource("source-map.yaml")
        val document = reader.read(source)
        assertLocation(document, "root", 1)
        assertLocation(document, "root.asyncapi", 1)
        assertLocation(document, "root.info", 2)
        assertLocation(document, "root.info.title", 3)
        assertLocation(document, "root.info.tags", 4)
        assertLocation(document, "root.info.tags[0]", 5)
    }

    private fun assertLocation(
        document: InputDocument,
        path: String,
        line: Int,
    ) {
        val location = assertNotNull(document.sourceMap[path], "Expected source location for $path")
        assertEquals(document.source.id, location.sourceId)
        assertEquals(document.source.file, location.file)
        assertEquals(path, location.path)
        assertEquals(line, location.line)
        assertTrue(location.column >= 1)
    }
}
