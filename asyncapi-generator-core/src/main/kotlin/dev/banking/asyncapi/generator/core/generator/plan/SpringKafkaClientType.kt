package dev.banking.asyncapi.generator.core.generator.plan

/**
 * Spring Kafka client generation mode selected during planning.
 *
 * The full client remains the default when a caller enables Spring Kafka
 * generation without supplying a `client.type` config option.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
enum class SpringKafkaClientType(
    val configValue: String,
) {
    FULL("spring-kafka"),
    SIMPLE("spring-kafka-simple"),
    ;

    companion object {
        fun fromConfigValue(value: String?): SpringKafkaClientType =
            when (value) {
                null -> FULL
                FULL.configValue -> FULL
                SIMPLE.configValue -> SIMPLE
                else ->
                    throw IllegalArgumentException(
                        "Unsupported client.type '$value'. Supported values: ${entries.joinToString { it.configValue }}",
                    )
            }
    }
}
