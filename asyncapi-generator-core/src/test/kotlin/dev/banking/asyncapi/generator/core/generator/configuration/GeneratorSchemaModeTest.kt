package dev.banking.asyncapi.generator.core.generator.configuration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GeneratorSchemaModeTest {
    @Test
    fun `from config value returns null when schema mode is not configured`() {
        assertNull(GeneratorSchemaMode.fromConfigValue(null))
    }

    @Test
    fun `from config value maps supported schema modes`() {
        assertEquals(GeneratorSchemaMode.NONE, GeneratorSchemaMode.fromConfigValue("none"))
        assertEquals(GeneratorSchemaMode.AVRO_PROJECTION, GeneratorSchemaMode.fromConfigValue("avro-projection"))
    }

    @Test
    fun `from config value rejects unsupported schema mode`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorSchemaMode.fromConfigValue("avro")
            }

        assertEquals(
            "Invalid schemaMode 'avro'. Supported values: none, avro-projection",
            exception.message,
        )
    }
}
