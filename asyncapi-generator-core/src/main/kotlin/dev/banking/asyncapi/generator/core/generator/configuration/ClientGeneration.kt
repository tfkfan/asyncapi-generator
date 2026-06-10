package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType

/**
 * Typed client generation capabilities requested by generator configuration.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
sealed interface ClientGeneration {
    data class SpringKafka(
        val packageName: String,
        val modelPackageName: String,
        val clientType: SpringKafkaClientType = SpringKafkaClientType.SIMPLE,
        val topicPropertyPrefix: String = "kafka.topics",
    ) : ClientGeneration

    data class QuarkusKafka(
        val packageName: String,
        val modelPackageName: String,
    ) : ClientGeneration
}
