package dev.banking.asyncapi.generator.core.generator.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeneratorNameTest {
    @Test
    fun `fromConfigurationValue defaults to kotlin when value is not configured`() {
        assertEquals(
            GeneratorName.KOTLIN,
            GeneratorName.fromConfigurationValue(
                value = null,
                path = "generatorName",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue parses supported configuration values`() {
        assertEquals(
            GeneratorName.KOTLIN,
            GeneratorName.fromConfigurationValue(
                value = "kotlin",
                path = "generatorName",
            ),
        )
        assertEquals(
            GeneratorName.JAVA,
            GeneratorName.fromConfigurationValue(
                value = "java",
                path = "generatorName",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue rejects unsupported configuration values`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorName.fromConfigurationValue(
                    value = "python",
                    path = "generatorName",
                )
            }

        assertEquals(
            "Invalid generatorName 'python'. Supported values: kotlin, java",
            exception.message,
        )
    }
}
