package dev.banking.asyncapi.generator.core.reader

import dev.banking.asyncapi.generator.core.fixtures.ReaderFixtures
import dev.banking.asyncapi.generator.core.fixtures.assertSourceLocation
import dev.banking.asyncapi.generator.core.fixtures.childObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonDocumentReaderTest {

    private val reader = JsonDocumentReader()

    @Test
    fun `reads semantic scalar values`() {
        val document = reader.read(ReaderFixtures.jsonSource("semantic-scalars.json"))
        val info = document.root.childObject("info")
        val example = document.root.childObject("components")
            .childObject("schemas")
            .childObject("Example")
        assertEquals("3.0.0", document.root["asyncapi"])
        assertEquals("Demo API", info["title"])
        assertEquals("folded text", info["summary"])
        assertEquals("literal\ntext", info["description"])
        assertEquals(true, example["enabled"])
        assertEquals("true", example["quotedEnabled"])
        assertEquals(12, example["count"])
        assertEquals("12", example["quotedCount"])
        assertEquals(12.5, example["price"])
        assertEquals(null, example["nullable"])
    }

    @Test
    fun `fails when json is malformed`() {
        val source = ReaderFixtures.jsonSource("malformed.json")
        assertFailsWith<DocumentReadException.MalformedDocument> {
            reader.read(source)
        }
    }

    @Test
    fun `fails when root is not an object`() {
        val source = ReaderFixtures.jsonSource("invalid-root.json")
        assertFailsWith<DocumentReadException.InvalidRoot> {
            reader.read(source)
        }
    }

    @Test
    fun `fails when document is empty`() {
        val source = ReaderFixtures.jsonSource("empty.json")
        assertFailsWith<DocumentReadException.EmptyDocument> {
            reader.read(source)
        }
    }

    @Test
    fun `fails when object contains duplicate keys`() {
        val source = ReaderFixtures.jsonSource("duplicate-key.json")
        val error = assertFailsWith<DocumentReadException.DuplicateKey> {
            reader.read(source)
        }
        assertTrue(error.message.orEmpty().contains("title"))
        assertTrue(error.message.orEmpty().contains(source.file.absolutePath))
    }

    @Test
    fun `records source locations for root object fields and array items`() {
        val document = reader.read(ReaderFixtures.jsonSource("source-map.json"))
        document.assertSourceLocation("root", 1)
        document.assertSourceLocation("root.asyncapi", 2)
        document.assertSourceLocation("root.info", 3)
        document.assertSourceLocation("root.info.title", 4)
        document.assertSourceLocation("root.info.tags", 5)
        document.assertSourceLocation("root.info.tags[0]", 6)
    }
}
