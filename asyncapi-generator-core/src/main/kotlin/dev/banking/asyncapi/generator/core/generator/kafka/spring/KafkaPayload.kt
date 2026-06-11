package dev.banking.asyncapi.generator.core.generator.kafka.spring

/**
 * Payload signature model used by Spring Kafka client generators.
 */
data class KafkaPayload(
    val messageName: String,
    val payloadType: String,
    val importName: String? = null,
)
