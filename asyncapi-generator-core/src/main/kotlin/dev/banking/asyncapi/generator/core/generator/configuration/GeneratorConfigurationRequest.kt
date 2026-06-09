package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
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
    val models: Models? = null,
    val schemas: Schemas = Schemas(),
    val clients: Clients = Clients(),
) {
    data class Models(
        val packageName: String? = null,
        val annotation: String? = null,
    )

    data class Schemas(
        val avroProjection: AvroProjection? = null,
    )

    data class AvroProjection(
        val packageName: String? = null,
    )

    data class Clients(
        val springKafka: SpringKafka? = null,
        val quarkusKafka: QuarkusKafka? = null,
    )

    data class SpringKafka(
        val packageName: String? = null,
        val modelPackageName: String? = null,
        val clientType: SpringKafkaClientType = SpringKafkaClientType.SIMPLE,
        val topicPropertyPrefix: String = DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
    )

    data class QuarkusKafka(
        val packageName: String? = null,
        val modelPackageName: String? = null,
    )

    companion object {
        const val DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX = "kafka.topics"
    }
}
