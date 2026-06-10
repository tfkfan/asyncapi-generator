package dev.banking.asyncapi.generator.core.generator.plan

/**
 * Spring Kafka client generation mode selected during planning.
 *
 * The simple client remains the default when a caller enables Spring Kafka generation
 * without selecting another mode.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 * - `SpringKafkaClientTypeTest`
 */
enum class SpringKafkaClientType(
    val configurationValue: String,
) {
    FULL("full"),
    SIMPLE("simple"),
    ;

    companion object {
        val supportedConfigurationValues: List<String> = entries.map { it.configurationValue }

        fun fromConfigurationValue(
            value: String?,
            path: String,
        ): SpringKafkaClientType {
            if (value == null) {
                return SIMPLE
            }

            return entries.firstOrNull { it.configurationValue == value }
                ?: throw IllegalArgumentException(
                    "Invalid $path '$value'. Supported values: ${supportedConfigurationValues.joinToString(", ")}",
                )
        }
    }
}
