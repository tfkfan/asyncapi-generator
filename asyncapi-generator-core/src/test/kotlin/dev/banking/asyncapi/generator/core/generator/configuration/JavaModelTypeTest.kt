package dev.banking.asyncapi.generator.core.generator.configuration

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JavaModelTypeTest {
    @Test
    fun `fromConfigurationValue defaults to class when value is not configured`() {
        assertEquals(
            JavaModelType.CLASS,
            JavaModelType.fromConfigurationValue(
                value = null,
                path = "models.javaModelType",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue parses supported configuration values`() {
        assertEquals(
            JavaModelType.CLASS,
            JavaModelType.fromConfigurationValue(
                value = "class",
                path = "models.javaModelType",
            ),
        )
        assertEquals(
            JavaModelType.RECORD,
            JavaModelType.fromConfigurationValue(
                value = "record",
                path = "models.javaModelType",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue rejects unsupported configuration values`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                JavaModelType.fromConfigurationValue(
                    value = "data",
                    path = "models.javaModelType",
                )
            }

        assertEquals(
            "Invalid models.javaModelType 'data'. Supported values: class, record",
            exception.message,
        )
    }
}
