package dev.banking.asyncapi.generator.core.reader

import dev.banking.asyncapi.generator.core.fixtures.ReaderFixtures
import dev.banking.asyncapi.generator.core.fixtures.assertSourceLocation
import org.junit.jupiter.api.Test

class SourceMapTest {

    private val reader = YamlDocumentReader()

    @Test
    fun `records source locations for root object fields and array items`() {
        val source = ReaderFixtures.yamlSource("source-map.yaml")
        val document = reader.read(source)
        document.assertSourceLocation("root", 1)
        document.assertSourceLocation("root.asyncapi", 1)
        document.assertSourceLocation("root.info", 2)
        document.assertSourceLocation("root.info.title", 3)
        document.assertSourceLocation("root.info.tags", 4)
        document.assertSourceLocation("root.info.tags[0]", 5)
    }
}
