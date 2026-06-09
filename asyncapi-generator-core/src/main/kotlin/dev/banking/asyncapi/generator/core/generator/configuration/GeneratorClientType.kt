package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType

/**
 * Public client generation mode selected by CLI, Maven, or Gradle configuration.
 *
 * Expected behavior is covered by:
 * - `GeneratorClientTypeTest`
 */
enum class GeneratorClientType(
    val configValue: String,
    val springKafkaClientType: SpringKafkaClientType? = null,
) {
    NONE("none"),
    SPRING_KAFKA("spring-kafka", SpringKafkaClientType.FULL),
    SPRING_KAFKA_SIMPLE("spring-kafka-simple", SpringKafkaClientType.SIMPLE),
    QUARKUS_KAFKA("quarkus-kafka");

    companion object {
        fun fromConfigValue(value: String?): GeneratorClientType? =
            value?.let { candidate ->
                entries.firstOrNull { it.configValue == candidate }
                    ?: throw IllegalArgumentException(
                        "Invalid clientType '$candidate'. Supported values: ${supportedValues()}",
                    )
            }

        fun supportedValues(): String =
            entries.joinToString { it.configValue }
    }
}
