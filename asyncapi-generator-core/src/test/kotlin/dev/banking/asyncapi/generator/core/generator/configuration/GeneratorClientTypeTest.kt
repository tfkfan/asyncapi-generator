package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class GeneratorClientTypeTest {
    @Test
    fun `from config value returns null when client type is not configured`() {
        assertNull(GeneratorClientType.fromConfigValue(null))
    }

    @Test
    fun `from config value maps supported client types`() {
        assertEquals(GeneratorClientType.NONE, GeneratorClientType.fromConfigValue("none"))
        assertEquals(GeneratorClientType.SPRING_KAFKA, GeneratorClientType.fromConfigValue("spring-kafka"))
        assertEquals(GeneratorClientType.SPRING_KAFKA_SIMPLE, GeneratorClientType.fromConfigValue("spring-kafka-simple"))
        assertEquals(GeneratorClientType.QUARKUS_KAFKA, GeneratorClientType.fromConfigValue("quarkus-kafka"))
    }

    @Test
    fun `spring kafka client types expose planner client type`() {
        assertEquals(SpringKafkaClientType.FULL, GeneratorClientType.SPRING_KAFKA.springKafkaClientType)
        assertEquals(SpringKafkaClientType.SIMPLE, GeneratorClientType.SPRING_KAFKA_SIMPLE.springKafkaClientType)
        assertNull(GeneratorClientType.NONE.springKafkaClientType)
        assertNull(GeneratorClientType.QUARKUS_KAFKA.springKafkaClientType)
    }

    @Test
    fun `from config value rejects unsupported client type`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorClientType.fromConfigValue("springkafka")
            }

        assertEquals(
            "Invalid clientType 'springkafka'. Supported values: none, spring-kafka, spring-kafka-simple, quarkus-kafka",
            exception.message,
        )
    }
}
