package dev.banking.asyncapi.generator.core.generator.plan

/**
 * Spring Kafka client generation mode selected during planning.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
enum class SpringKafkaClientType {
    FULL,
    SIMPLE,
}
