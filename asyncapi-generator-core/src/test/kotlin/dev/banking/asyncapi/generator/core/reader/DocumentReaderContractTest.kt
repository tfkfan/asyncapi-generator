package dev.banking.asyncapi.generator.core.reader

import dev.banking.asyncapi.generator.core.fixtures.ReaderFixtures
import dev.banking.asyncapi.generator.core.fixtures.childObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentReaderContractTest {

    private val reader = YamlDocumentReader()

    @Test
    fun `reader preserves references without resolving them`() {
        val document = reader.read(ReaderFixtures.yamlSource("reference-preservation.yaml"))
        val userRef = document.root.childObject("components")
            .childObject("schemas")
            .childObject("UserRef")
        assertEquals("#/components/schemas/User", userRef["${'$'}ref"])
    }

    @Test
    fun `reader accepts object roots without validating AsyncAPI semantics`() {
        val document = reader.read(ReaderFixtures.yamlSource("non-asyncapi-object.yaml"))
        assertTrue(document.root.containsKey("notAsyncApi"))
        assertTrue(document.root.containsKey("stillReaderInput"))
    }
}
