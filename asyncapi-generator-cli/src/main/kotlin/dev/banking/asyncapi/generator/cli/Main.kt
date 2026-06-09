package dev.banking.asyncapi.generator.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorClientType
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationFactory
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorSchemaMode
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.JAVA
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.KOTLIN
import dev.banking.asyncapi.generator.core.parser.AsyncApiParser
import dev.banking.asyncapi.generator.core.registry.AsyncApiRegistry
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import java.io.File

fun main(args: Array<String>) = AsyncApiGeneratorCli().main(args)

class AsyncApiGeneratorCli : CliktCommand(name = "asyncapi-generator") {
    private val input by option("--input", "-i", help = "Path to AsyncAPI YAML file")
        .file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .required()
    private val codegenOutputDirectory by option("--codegen-output", help = "Codegen output directory")
        .file(canBeFile = false)
        .default(File("./generated-sources/asyncapi"))
    private val resourceOutputDirectory by option("--resource-output", help = "Resource output directory")
        .file(canBeFile = false)
        .default(File("./generated-resources/asyncapi"))
    private val outputFile by option("--output-file", help = "Write bundled AsyncAPI YAML to file")
        .file(canBeDir = false)

    private val generator by option("--generator", "-g", help = "Target language (KOTLIN, JAVA)")
        .choice(
            "kotlin" to KOTLIN,
            "java" to JAVA,
        ).default(KOTLIN)

    private val modelPackage by option("--model-package", help = "Package for generated models")

    private val clientPackage by option(
        "--client-package",
        help = "Package for generated clients (defaults to model-package)",
    )

    private val schemaPackage by option(
        "--schema-package",
        help = "Namespace for Avro schemas (defaults to model-package)",
    )
    private val kafkaTopicsPropertyPrefix by option(
        "--kafka-topics-property-prefix",
        help = "Kafka topic property prefix (default: kafka.topics)",
    )
    private val clientType by option(
        "--client-type",
        help = "Client generation mode",
    ).choice(
        "none" to GeneratorClientType.NONE,
        "spring-kafka" to GeneratorClientType.SPRING_KAFKA,
        "spring-kafka-simple" to GeneratorClientType.SPRING_KAFKA_SIMPLE,
        "quarkus-kafka" to GeneratorClientType.QUARKUS_KAFKA,
    )
    private val schemaMode by option(
        "--schema-mode",
        help = "Schema generation mode",
    ).choice(
        "none" to GeneratorSchemaMode.NONE,
        "avro-projection" to GeneratorSchemaMode.AVRO_PROJECTION,
    )
    private val modelAnnotation by option(
        "--model-annotation",
        help = "Fully qualified annotation added to generated model classes",
    )

    override fun run() {
        echo("Generating AsyncAPI code from $input...")

        val context = AsyncApiContext()
        val root = AsyncApiRegistry.readYaml(input, context)
        val parser = AsyncApiParser(context)
        val document = parser.parse(root)

        val validator = AsyncApiValidator(context)
        val results = validator.validate(document)

        if (results.hasWarnings()) {
            results.warnings.forEach { echo("WARN: ${it.message}") }
        }

        if (results.hasErrors()) {
            results.errors.forEach { echo("ERROR: ${it.message}") }
            throw RuntimeException("Validation failed with ${results.errors.size} errors.")
        }

        val bundler = AsyncApiBundler()
        val bundledDoc = bundler.bundle(document)
        outputFile?.let { file ->
            AsyncApiRegistry.writeYaml(file, bundledDoc)
        }
        val sourceRootName =
            if (generator == KOTLIN) {
                "src/main/kotlin"
            } else {
                "src/main/java"
            }
        val sourceRoot = codegenOutputDirectory.resolve(sourceRootName)
        val generatorConfiguration =
            GeneratorConfigurationFactory.create(
                GeneratorConfigurationRequest(
                    language = generator,
                    sourceOutputDirectory = sourceRoot,
                    resourceOutputDirectory = resourceOutputDirectory,
                    models = modelRequest(),
                    schemas = schemaRequest(),
                    clients = clientRequest(),
                ),
            )
        if (generatorConfiguration.hasConfiguredOutputs()) {
            AsyncApiGenerator().generate(bundledDoc, generatorConfiguration)
        }
        echo("Generation complete.")
    }

    private fun modelRequest(): GeneratorConfigurationRequest.Models? =
        if (modelPackage != null || modelAnnotation != null) {
            GeneratorConfigurationRequest.Models(
                packageName = modelPackage,
                annotation = modelAnnotation,
            )
        } else {
            null
        }

    private fun schemaRequest(): GeneratorConfigurationRequest.Schemas =
        GeneratorConfigurationRequest.Schemas(
            avroProjection =
                if (schemaMode == GeneratorSchemaMode.AVRO_PROJECTION) {
                    GeneratorConfigurationRequest.AvroProjection(packageName = schemaPackage)
                } else {
                    null
                },
        )

    private fun clientRequest(): GeneratorConfigurationRequest.Clients =
        GeneratorConfigurationRequest.Clients(
            springKafka =
                clientType?.springKafkaClientType?.let { springKafkaClientType ->
                    GeneratorConfigurationRequest.SpringKafka(
                        packageName = clientPackage,
                        modelPackageName = modelPackage,
                        clientType = springKafkaClientType,
                        topicPropertyPrefix =
                            kafkaTopicsPropertyPrefix
                                ?: GeneratorConfigurationRequest.DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
                    )
                },
            quarkusKafka =
                if (clientType == GeneratorClientType.QUARKUS_KAFKA) {
                    GeneratorConfigurationRequest.QuarkusKafka(
                        packageName = clientPackage,
                        modelPackageName = modelPackage,
                    )
                } else {
                    null
                },
        )
}
