package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import java.io.File

/**
 * Frontend-neutral generator configuration request.
 *
 * CLI, Maven, and Gradle map their public configuration surfaces into this
 * request before core generator configuration is assembled.
 *
 * Expected behavior is covered by:
 * - `GeneratorConfigurationFactoryTest`
 */
data class GeneratorConfigurationRequest(
    val language: GeneratorName,
    val sourceOutputDirectory: File,
    val resourceOutputDirectory: File,
    val modelPackageName: String? = null,
    val clientPackageName: String? = null,
    val schemaPackageName: String? = null,
    val clientType: GeneratorClientType? = null,
    val schemaMode: GeneratorSchemaMode? = null,
    val modelAnnotation: String? = null,
    val kafkaTopicsPropertyPrefix: String = DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
) {
    companion object {
        const val DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX = "kafka.topics"
    }
}
