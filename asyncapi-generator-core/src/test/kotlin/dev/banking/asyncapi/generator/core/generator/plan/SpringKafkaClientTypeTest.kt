package dev.banking.asyncapi.generator.core.generator.plan

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpringKafkaClientTypeTest {
    @Test
    fun `from config value defaults to full Spring Kafka client type`() {
        assertEquals(SpringKafkaClientType.FULL, SpringKafkaClientType.fromConfigValue(null))
    }

    @Test
    fun `from config value maps supported Spring Kafka client types`() {
        assertEquals(SpringKafkaClientType.FULL, SpringKafkaClientType.fromConfigValue("spring-kafka"))
        assertEquals(SpringKafkaClientType.SIMPLE, SpringKafkaClientType.fromConfigValue("spring-kafka-simple"))
    }

    @Test
    fun `from config value rejects unsupported Spring Kafka client type`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                SpringKafkaClientType.fromConfigValue("springkafka")
            }

        assertEquals(
            "Unsupported client.type 'springkafka'. Supported values: spring-kafka, spring-kafka-simple",
            exception.message,
        )
    }
}
