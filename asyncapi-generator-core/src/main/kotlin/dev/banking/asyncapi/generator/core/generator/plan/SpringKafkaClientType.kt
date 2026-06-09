package dev.banking.asyncapi.generator.core.generator.plan

/**
 * Spring Kafka client generation mode selected during planning.
 *
 * The full client remains the default when a caller enables Spring Kafka generation
 * without selecting the simple-client variant.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
enum class SpringKafkaClientType {
    FULL,
    SIMPLE,
}
