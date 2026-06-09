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

        return GeneratorConfiguration(
            language = request.language,
            output =
                GeneratorOutputConfiguration(
                    sourceOutputDirectory = request.sourceOutputDirectory,
                    resourceOutputDirectory = request.resourceOutputDirectory,
                ),
            models =
                request.models?.packageName?.let { packageName ->
                    ModelGeneration.Enabled(
                        packageName = packageName,
                        annotation = request.models.annotation,
                    )
                } ?: ModelGeneration.Disabled,
            schemas =
                buildList {
                    request.schemas.avroProjection?.packageName?.let { packageName ->
                        add(SchemaGeneration.AvroProjection(packageName))
                    }
                },
            clients =
                buildList {
                    request.clients.springKafka?.let { springKafka ->
                        add(
                            ClientGeneration.SpringKafka(
                                packageName = requiredPackageName(
                                    path = "clients.springKafka.packageName",
                                    value = springKafka.packageName,
                                ),
                                modelPackageName = requiredClientModelPackageName(
                                    path = "clients.springKafka.modelPackageName",
                                    configuredModelPackageName = springKafka.modelPackageName,
                                    modelsPackageName = request.models?.packageName,
                                ),
                                clientType = springKafka.clientType,
                                topicPropertyPrefix = springKafka.topicPropertyPrefix,
                            ),
                        )
                    }

                    request.clients.quarkusKafka?.let { quarkusKafka ->
                        add(
                            ClientGeneration.QuarkusKafka(
                                packageName = requiredPackageName(
                                    path = "clients.quarkusKafka.packageName",
                                    value = quarkusKafka.packageName,
                                ),
                                modelPackageName = requiredClientModelPackageName(
                                    path = "clients.quarkusKafka.modelPackageName",
                                    configuredModelPackageName = quarkusKafka.modelPackageName,
                                    modelsPackageName = request.models?.packageName,
                                ),
                            ),
                        )
                    }
                },
        )
    }

    private fun validate(request: GeneratorConfigurationRequest) {
        if (request.models?.annotation != null && request.models.packageName == null) {
            throw IllegalArgumentException("models.packageName is required when models.annotation is configured")
        }

        if (request.schemas.avroProjection != null && request.schemas.avroProjection.packageName == null) {
            throw IllegalArgumentException(
                "schemas.avroProjection.packageName is required when schemas.avroProjection is configured",
            )
        }

        if (request.clients.springKafka != null && request.clients.springKafka.packageName == null) {
            throw IllegalArgumentException(
                "clients.springKafka.packageName is required when clients.springKafka is configured",
            )
        }

        if (request.clients.quarkusKafka != null && request.clients.quarkusKafka.packageName == null) {
            throw IllegalArgumentException(
                "clients.quarkusKafka.packageName is required when clients.quarkusKafka is configured",
            )
        }

        if (request.clients.springKafka?.topicPropertyPrefix?.isBlank() == true) {
            throw IllegalArgumentException("clients.springKafka.topicPropertyPrefix cannot be empty")
        }
    }

    private fun requiredPackageName(
        path: String,
        value: String?,
    ): String =
        value ?: throw IllegalArgumentException("$path is required")

    private fun requiredClientModelPackageName(
        path: String,
        configuredModelPackageName: String?,
        modelsPackageName: String?,
    ): String =
        configuredModelPackageName ?: modelsPackageName
            ?: throw IllegalArgumentException(
                "$path is required when models.packageName is not configured",
            )
}
