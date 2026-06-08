package dev.banking.asyncapi.generator.core.generator

import dev.banking.asyncapi.generator.core.generator.avro.AvroGenerator
import dev.banking.asyncapi.generator.core.generator.input.GenerationInputFactory
import dev.banking.asyncapi.generator.core.generator.java.JavaGenerator
import dev.banking.asyncapi.generator.core.generator.java.JavaModelPreparer
import dev.banking.asyncapi.generator.core.generator.java.factory.JavaGeneratorModelFactory
import dev.banking.asyncapi.generator.core.generator.java.kafka.spring.JavaSpringKafkaGenerator
import dev.banking.asyncapi.generator.core.generator.java.kafka.spring.JavaSpringKafkaSimpleGenerator
import dev.banking.asyncapi.generator.core.generator.kotlin.KotlinGenerator
import dev.banking.asyncapi.generator.core.generator.kotlin.KotlinModelPreparer
import dev.banking.asyncapi.generator.core.generator.kotlin.factory.KotlinGeneratorModelFactory
import dev.banking.asyncapi.generator.core.generator.kotlin.kafka.spring.KotlinSpringKafkaGenerator
import dev.banking.asyncapi.generator.core.generator.kotlin.kafka.spring.KotlinSpringKafkaSimpleGenerator
import dev.banking.asyncapi.generator.core.generator.loader.HeaderSchemaCollector
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.JAVA
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.KOTLIN
import dev.banking.asyncapi.generator.core.generator.model.GeneratorOptions
import dev.banking.asyncapi.generator.core.generator.output.FileSystemGeneratedArtifactWriter
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import org.slf4j.LoggerFactory

/**
 * Coordinates generator analysis and writes rendered artifacts to configured outputs.
 *
 * Expected behavior is covered by:
 * - `AsyncApiGeneratorOutputContractTest`
 */
class AsyncApiGenerator {
    private val log = LoggerFactory.getLogger(AsyncApiGenerator::class.java)

    private val generationInputFactory = GenerationInputFactory()
    private val kotlinModelPreparer = KotlinModelPreparer()
    private val javaModelPreparer = JavaModelPreparer()

    fun generate(
        asyncApiDocument: AsyncApiDocument,
        generatorOptions: GeneratorOptions,
    ) {
        val generationInput = generationInputFactory.create(asyncApiDocument)
        val artifactWriter =
            FileSystemGeneratedArtifactWriter(
                sourceOutputDirectory = generatorOptions.codegenOutputDirectory,
                resourceOutputDirectory = generatorOptions.resourceOutputDirectory,
            )

        when (generatorOptions.generatorName) {
            KOTLIN -> {
                if (generatorOptions.generateModels) {
                    val kotlinGenerationModel =
                        kotlinModelPreparer.prepare(
                            input = generationInput,
                            packageName = generatorOptions.modelPackage,
                            annotation = generatorOptions.configOptions["model.annotation"],
                        )
                    val kotlinModelGenerator =
                        KotlinGenerator(
                            packageName = generatorOptions.modelPackage,
                            outputDir = generatorOptions.codegenOutputDirectory,
                            generationModel = kotlinGenerationModel,
                        )
                    artifactWriter.write(kotlinModelGenerator.render())
                }

                if (generatorOptions.generateSpringKafkaClient) {
                    if (generatorOptions.kafkaTopicsPropertyPrefix.isBlank()) {
                        throw IllegalArgumentException("kafka.topics.property.prefix cannot be empty")
                    }
                    val clientType = generatorOptions.configOptions["client.type"]
                    if (clientType != "spring-kafka-simple") {
                        val headerSchemas = HeaderSchemaCollector.collect(asyncApiDocument)
                        if (headerSchemas.isNotEmpty()) {
                            val headerFactory =
                                KotlinGeneratorModelFactory(
                                    packageName = "${generatorOptions.clientPackage}.header",
                                    context = generationInput.schemaContextWith(headerSchemas),
                                    polymorphicRelationships = generationInput.polymorphicRelationships,
                                    annotation = null,
                                )
                            val headerModels =
                                headerSchemas.mapNotNull { (name, schema) ->
                                    headerFactory.create(name, schema)
                                }
                            val headerGenerator =
                                KotlinGenerator(
                                    packageName = "${generatorOptions.clientPackage}.header",
                                    outputDir = generatorOptions.codegenOutputDirectory,
                                    generationModel = headerModels,
                                )
                            artifactWriter.write(headerGenerator.render())
                        }
                    }
                    if (clientType == "spring-kafka-simple") {
                        val kafkaGenerator =
                            KotlinSpringKafkaSimpleGenerator(
                                outputDir = generatorOptions.codegenOutputDirectory,
                                clientPackage = generatorOptions.clientPackage,
                                modelPackage = generatorOptions.modelPackage,
                            )
                        kafkaGenerator.generate(generationInput.channels)
                    } else {
                        val kafkaGenerator =
                            KotlinSpringKafkaGenerator(
                                outputDir = generatorOptions.codegenOutputDirectory,
                                clientPackage = generatorOptions.clientPackage,
                                modelPackage = generatorOptions.modelPackage,
                                topicPropertyPrefix = generatorOptions.kafkaTopicsPropertyPrefix,
                                resourceOutputDir = generatorOptions.resourceOutputDirectory,
                            )
                        kafkaGenerator.generate(generationInput.channels)
                    }
                }

                if (generatorOptions.generateQuarkusKafkaClient) {
                    log.info("Generate Kotlin Quarkus Kafka Client is not yet implemented. Skipping..")
                }
            }

            JAVA -> {
                if (generatorOptions.generateModels) {
                    val javaGenerationModel =
                        javaModelPreparer.prepare(
                            input = generationInput,
                            packageName = generatorOptions.modelPackage,
                        )
                    val javaGenerator =
                        JavaGenerator(
                            packageName = generatorOptions.modelPackage,
                            outputDir = generatorOptions.codegenOutputDirectory,
                            generationModel = javaGenerationModel,
                        )
                    artifactWriter.write(javaGenerator.render())
                }
                if (generatorOptions.generateSpringKafkaClient) {
                    if (generatorOptions.kafkaTopicsPropertyPrefix.isBlank()) {
                        throw IllegalArgumentException("kafka.topics.property.prefix cannot be empty")
                    }
                    val clientType = generatorOptions.configOptions["client.type"]
                    if (clientType != "spring-kafka-simple") {
                        val headerSchemas = HeaderSchemaCollector.collect(asyncApiDocument)
                        if (headerSchemas.isNotEmpty()) {
                            val headerFactory =
                                JavaGeneratorModelFactory(
                                    packageName = "${generatorOptions.clientPackage}.header",
                                    context = generationInput.schemaContextWith(headerSchemas),
                                    polymorphicRelationships = generationInput.polymorphicRelationships,
                                )
                            val headerModels =
                                headerSchemas.mapNotNull { (name, schema) ->
                                    headerFactory.create(name, schema)
                                }
                            val headerGenerator =
                                JavaGenerator(
                                    packageName = "${generatorOptions.clientPackage}.header",
                                    outputDir = generatorOptions.codegenOutputDirectory,
                                    generationModel = headerModels,
                                )
                            artifactWriter.write(headerGenerator.render())
                        }
                    }
                    if (clientType == "spring-kafka-simple") {
                        val kafkaGenerator =
                            JavaSpringKafkaSimpleGenerator(
                                outputDir = generatorOptions.codegenOutputDirectory,
                                clientPackage = generatorOptions.clientPackage,
                                modelPackage = generatorOptions.modelPackage,
                            )
                        kafkaGenerator.generate(generationInput.channels)
                    } else {
                        val kafkaGenerator =
                            JavaSpringKafkaGenerator(
                                outputDir = generatorOptions.codegenOutputDirectory,
                                clientPackage = generatorOptions.clientPackage,
                                modelPackage = generatorOptions.modelPackage,
                                topicPropertyPrefix = generatorOptions.kafkaTopicsPropertyPrefix,
                                resourceOutputDir = generatorOptions.resourceOutputDirectory,
                            )
                        kafkaGenerator.generate(generationInput.channels)
                    }
                }

                if (generatorOptions.generateQuarkusKafkaClient) {
                    log.info("Generate Java Quarkus Kafka Client is not yet implemented. Skipping..")
                }
            }
        }

        if (generatorOptions.generateAvroSchema) {
            val avroGenerator =
                AvroGenerator(
                    outputDir = generatorOptions.resourceOutputDirectory,
                    packageName = generatorOptions.schemaPackage,
                )
            artifactWriter.write(avroGenerator.render(generationInput.schemas))
        }
    }
}
