package dev.banking.asyncapi.generator.core.generator.configuration

/**
 * Assembles core generator configuration from frontend-neutral requests.
 *
 * Expected behavior is covered by:
 * - `GeneratorConfigurationFactoryTest`
 */
object GeneratorConfigurationFactory {
    fun create(request: GeneratorConfigurationRequest): GeneratorConfiguration {
        validate(request)

        val effectiveModelPackage = request.modelPackageName ?: "unused"
        val effectiveClientPackage = request.clientPackageName ?: "unused"
        val effectiveSchemaPackage = request.schemaPackageName ?: "unused"

        return GeneratorConfiguration(
            language = request.language,
            output =
                GeneratorOutputConfiguration(
                    sourceOutputDirectory = request.sourceOutputDirectory,
                    resourceOutputDirectory = request.resourceOutputDirectory,
                ),
            models =
                request.modelPackageName?.let { packageName ->
                    ModelGeneration.Enabled(
                        packageName = packageName,
                        annotation = request.modelAnnotation,
                    )
                } ?: ModelGeneration.Disabled,
            schemas =
                buildList {
                    if (
                        request.schemaPackageName != null &&
                        request.schemaMode == GeneratorSchemaMode.AVRO_PROJECTION
                    ) {
                        add(SchemaGeneration.AvroProjection(effectiveSchemaPackage))
                    }
                },
            clients =
                buildList {
                    val springKafkaClientType = request.clientType?.springKafkaClientType
                    if (request.clientPackageName != null && springKafkaClientType != null) {
                        add(
                            ClientGeneration.SpringKafka(
                                packageName = effectiveClientPackage,
                                modelPackageName = effectiveModelPackage,
                                clientType = springKafkaClientType,
                                topicPropertyPrefix = request.kafkaTopicsPropertyPrefix,
                            ),
                        )
                    }

                    if (
                        request.clientPackageName != null &&
                        request.clientType == GeneratorClientType.QUARKUS_KAFKA
                    ) {
                        add(
                            ClientGeneration.QuarkusKafka(
                                packageName = effectiveClientPackage,
                                modelPackageName = effectiveModelPackage,
                            ),
                        )
                    }
                },
        )
    }

    private fun validate(request: GeneratorConfigurationRequest) {
        if (request.clientType != null && request.clientType != GeneratorClientType.NONE && request.clientPackageName == null) {
            throw IllegalArgumentException("clientType requires clientPackage")
        }

        if (request.schemaMode != null && request.schemaMode != GeneratorSchemaMode.NONE && request.schemaPackageName == null) {
            throw IllegalArgumentException("schemaMode requires schemaPackage")
        }

        if (request.modelAnnotation != null && request.modelPackageName == null) {
            throw IllegalArgumentException("modelAnnotation requires modelPackage")
        }

        if (request.kafkaTopicsPropertyPrefix.isBlank()) {
            throw IllegalArgumentException("kafkaTopicsPropertyPrefix cannot be empty")
        }
    }
}
