package dev.banking.asyncapi.generator.core.reader

import dev.banking.asyncapi.generator.core.fixtures.ReaderFixtures
import dev.banking.asyncapi.generator.core.fixtures.childObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class YamlDocumentReaderTest {

    private val reader = YamlDocumentReader()

    @Test
    fun `reads semantic scalar values without yaml style markers`() {
        val document = reader.read(ReaderFixtures.yamlSource("semantic-scalars.yaml"))
        val info = document.root.childObject("info")
        val example = document.root.childObject("components")
            .childObject("schemas")
            .childObject("Example")
        assertEquals("3.0.0", document.root["asyncapi"])
        assertEquals("Demo API", info["title"])
        assertTrue((info["summary"] as String).startsWith("folded text"))
        assertTrue((info["description"] as String).startsWith("literal\ntext"))
        assertEquals(true, example["enabled"])
        assertEquals("true", example["quotedEnabled"])
        assertEquals(12, example["count"])
        assertEquals("12", example["quotedCount"])
        assertEquals(12.5, example["price"])
        assertEquals(null, example["nullable"])
    }

    @Test
    fun `fails when yaml is malformed`() {
        val source = ReaderFixtures.yamlSource("malformed.yaml")
        assertFailsWith<DocumentReadException.MalformedDocument> {
            reader.read(source)
        }
    }

    @Test
    fun `fails when root is not an object`() {
        val source = ReaderFixtures.yamlSource("invalid-root.yaml")
        assertFailsWith<DocumentReadException.InvalidRoot> {
            reader.read(source)
        }
    }

    @Test
    fun `fails when mapping key is not scalar`() {
        val source = ReaderFixtures.yamlSource("non-scalar-key.yaml")
        assertFailsWith<DocumentReadException.InvalidMappingKey> {
            reader.read(source)
        }
    }

    @Test
    fun `returns linked map preserving input order`() {
        val document = reader.read(ReaderFixtures.yamlSource("order-preservation.yaml"))
        assertIs<LinkedHashMap<String, Any?>>(document.root)
        assertEquals(listOf("asyncapi", "info", "channels"), document.root.keys.toList())
    }
}
