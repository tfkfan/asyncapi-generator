package dev.banking.asyncapi.generator.maven.plugin

import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest

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
        GeneratorConfigurationRequest.models(
            enabled = enabled,
            packageName = packageName,
            annotation = annotation,
        )
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
        GeneratorConfigurationRequest.avroProjection(
            enabled = enabled,
            packageName = packageName,
        )
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

    fun toRequest(): GeneratorConfigurationRequest.Clients =
        GeneratorConfigurationRequest.Clients(
            springKafka = springKafka?.toRequest(),
            quarkusKafka = quarkusKafka?.toRequest(),
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
    var modelPackageName: String? = null
    var mode: String? = null
    var topicPropertyPrefix: String? = null

    fun toRequest(): GeneratorConfigurationRequest.SpringKafka? =
        GeneratorConfigurationRequest.springKafka(
            enabled = enabled,
            packageName = packageName,
            modelPackageName = modelPackageName,
            mode = mode,
            topicPropertyPrefix = topicPropertyPrefix,
        )
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
    var modelPackageName: String? = null

    fun toRequest(): GeneratorConfigurationRequest.QuarkusKafka? =
        GeneratorConfigurationRequest.quarkusKafka(
            enabled = enabled,
            packageName = packageName,
            modelPackageName = modelPackageName,
        )
}
