package dev.banking.asyncapi.generator.maven.plugin

import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType

/**
 * Maven model generation configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenModelGenerationConfiguration {
    var enabled: Boolean? = null
    var packageName: String? = null
    var annotation: String? = null

    fun toRequest(): GeneratorConfigurationRequest.Models? =
        if (enabled == false) {
            null
        } else if (enabled == true || packageName != null || annotation != null) {
            GeneratorConfigurationRequest.Models(
                packageName = packageName,
                annotation = annotation,
            )
        } else {
            null
        }
}

/**
 * Maven schema generation configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenSchemaGenerationConfiguration {
    var avroProjection: MavenAvroProjectionConfiguration? = null

    fun toRequest(): GeneratorConfigurationRequest.Schemas =
        GeneratorConfigurationRequest.Schemas(
            avroProjection = avroProjection?.toRequest(),
        )
}

/**
 * Maven Avro projection configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenAvroProjectionConfiguration {
    var enabled: Boolean? = null
    var packageName: String? = null

    fun toRequest(): GeneratorConfigurationRequest.AvroProjection? =
        if (enabled == false) {
            null
        } else if (enabled == true || packageName != null) {
            GeneratorConfigurationRequest.AvroProjection(packageName = packageName)
        } else {
            null
        }
}

/**
 * Maven client generation configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenClientGenerationConfiguration {
    var springKafka: MavenSpringKafkaConfiguration? = null
    var quarkusKafka: MavenQuarkusKafkaConfiguration? = null

    fun toRequest(modelPackageName: String?): GeneratorConfigurationRequest.Clients =
        GeneratorConfigurationRequest.Clients(
            springKafka = springKafka?.toRequest(modelPackageName),
            quarkusKafka = quarkusKafka?.toRequest(modelPackageName),
        )
}

/**
 * Maven Spring Kafka client configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenSpringKafkaConfiguration {
    var enabled: Boolean? = null
    var packageName: String? = null
    var mode: String? = null
    var topicPropertyPrefix: String? = null

    fun toRequest(modelPackageName: String?): GeneratorConfigurationRequest.SpringKafka? =
        if (enabled == false) {
            null
        } else if (enabled == true || packageName != null || mode != null || topicPropertyPrefix != null) {
            GeneratorConfigurationRequest.SpringKafka(
                packageName = packageName,
                modelPackageName = modelPackageName,
                clientType = springKafkaClientType(),
                topicPropertyPrefix =
                    topicPropertyPrefix
                        ?: GeneratorConfigurationRequest.DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
            )
        } else {
            null
        }

    private fun springKafkaClientType(): SpringKafkaClientType =
        when (mode ?: FULL_MODE) {
            FULL_MODE -> SpringKafkaClientType.FULL
            SIMPLE_MODE -> SpringKafkaClientType.SIMPLE
            else ->
                throw IllegalArgumentException(
                    "Invalid springKafka mode '$mode'. Supported values: $FULL_MODE, $SIMPLE_MODE",
                )
        }

    companion object {
        private const val FULL_MODE = "full"
        private const val SIMPLE_MODE = "simple"
    }
}

/**
 * Maven Quarkus Kafka client configuration.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorMojoTest`
 */
class MavenQuarkusKafkaConfiguration {
    var enabled: Boolean? = null
    var packageName: String? = null

    fun toRequest(modelPackageName: String?): GeneratorConfigurationRequest.QuarkusKafka? =
        if (enabled == false) {
            null
        } else if (enabled == true || packageName != null) {
            GeneratorConfigurationRequest.QuarkusKafka(
                packageName = packageName,
                modelPackageName = modelPackageName,
            )
        } else {
            null
        }
}
