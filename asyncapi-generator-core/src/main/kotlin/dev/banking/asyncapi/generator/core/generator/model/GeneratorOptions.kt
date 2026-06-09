package dev.banking.asyncapi.generator.core.generator.model

import dev.banking.asyncapi.generator.core.generator.configuration.ClientGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorOutputConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.ModelGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.SchemaGeneration
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
import java.io.File

data class GeneratorOptions(
    val generatorName: GeneratorName,
    val modelPackage: String,
    val clientPackage: String,
    val schemaPackage: String,
    val codegenOutputDirectory: File,
    val resourceOutputDirectory: File,
    val kafkaTopicsPropertyPrefix: String = "kafka.topics",
    // Feature Flags
    val generateModels: Boolean = true,
    val generateSpringKafkaClient: Boolean = false,
    val generateQuarkusKafkaClient: Boolean = false,
    val generateAvroSchema: Boolean = false,
    // Flat config options (for future use)
    val configOptions: Map<String, String> = emptyMap(),
) {
    fun toGeneratorConfiguration(): GeneratorConfiguration =
        GeneratorConfiguration(
            language = generatorName,
            output =
                GeneratorOutputConfiguration(
                    sourceOutputDirectory = codegenOutputDirectory,
                    resourceOutputDirectory = resourceOutputDirectory,
                ),
            models =
                if (generateModels) {
                    ModelGeneration.Enabled(
                        packageName = modelPackage,
                        annotation = configOptions["model.annotation"],
                    )
                } else {
                    ModelGeneration.Disabled
                },
            schemas =
                buildList {
                    if (generateAvroSchema) {
                        add(SchemaGeneration.AvroProjection(schemaPackage))
                    }
                },
            clients =
                buildList {
                    if (generateSpringKafkaClient) {
                        add(
                            ClientGeneration.SpringKafka(
                                packageName = clientPackage,
                                modelPackageName = modelPackage,
                                clientType = SpringKafkaClientType.fromConfigValue(configOptions["client.type"]),
                                topicPropertyPrefix = kafkaTopicsPropertyPrefix,
                            ),
                        )
                    }
                    if (generateQuarkusKafkaClient) {
                        add(
                            ClientGeneration.QuarkusKafka(
                                packageName = clientPackage,
                                modelPackageName = modelPackage,
                            ),
                        )
                    }
                },
        )
}
