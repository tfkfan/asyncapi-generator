package dev.banking.asyncapi.generator.core.generator.plan

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpringKafkaClientTypeTest {
    @Test
    fun `fromConfigurationValue defaults to simple when value is not configured`() {
        assertEquals(
            SpringKafkaClientType.SIMPLE,
            SpringKafkaClientType.fromConfigurationValue(
                value = null,
                path = "clients.springKafka.mode",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue parses supported configuration values`() {
        assertEquals(
            SpringKafkaClientType.FULL,
            SpringKafkaClientType.fromConfigurationValue(
                value = "full",
                path = "clients.springKafka.mode",
            ),
        )
        assertEquals(
            SpringKafkaClientType.SIMPLE,
            SpringKafkaClientType.fromConfigurationValue(
                value = "simple",
                path = "clients.springKafka.mode",
            ),
        )
    }

    @Test
    fun `fromConfigurationValue rejects unsupported configuration values`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                SpringKafkaClientType.fromConfigurationValue(
                    value = "basic",
                    path = "clients.springKafka.mode",
                )
            }

        assertEquals(
            "Invalid clients.springKafka.mode 'basic'. Supported values: full, simple",
            exception.message,
        )
    }
}
