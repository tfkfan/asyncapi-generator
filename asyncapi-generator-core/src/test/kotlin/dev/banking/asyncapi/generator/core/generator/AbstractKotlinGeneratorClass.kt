package dev.banking.asyncapi.generator.core.generator

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.BundlerFixtures
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.model.GeneratorOptions
import java.io.File

abstract class AbstractKotlinGeneratorClass {
    val asyncApiContext = AsyncApiContext()
    private val bundlerFixtures = BundlerFixtures(asyncApiContext)
    val generator = AsyncApiGenerator()

    fun generateElement(
        yaml: File,
        generated: String? = null,
        codegenOutputDirectory: File = File("target/generated-sources/asyncapi"),
        resourceOutputDirectory: File = File("target/generated-resources/asyncapi"),
        modelPackage: String,
        clientPackage: String? = null,
        schemaPackage: String? = null,
        generateModels: Boolean = true,
        generateSpringKafkaClient: Boolean = false,
        generateQuarkusKafkaClient: Boolean = false,
        kafkaTopicsPropertyPrefix: String = "kafka.topics",
        configOptions: Map<String, String> = emptyMap(),
    ): String {
        val bundled = bundlerFixtures.bundledDocument(yaml)
        val generatorOptions =
            GeneratorOptions(
                generatorName = GeneratorName.KOTLIN,
                modelPackage = modelPackage,
                clientPackage = clientPackage ?: modelPackage,
                schemaPackage = schemaPackage ?: modelPackage,
                codegenOutputDirectory = codegenOutputDirectory,
                resourceOutputDirectory = resourceOutputDirectory,
                kafkaTopicsPropertyPrefix = kafkaTopicsPropertyPrefix,
                generateModels = generateModels,
                generateSpringKafkaClient = generateSpringKafkaClient,
                generateQuarkusKafkaClient = generateQuarkusKafkaClient,
                configOptions = configOptions,
            )
        generator.generate(
            asyncApiDocument = bundled,
            generatorOptions = generatorOptions,
        )
        if (generated != null) {
            val modelPath = modelPackage.replace('.', '/')
            val output =
                codegenOutputDirectory
                    .resolve(modelPath)
                    .resolve(generated)
            return output.readText()
        }
        return ""
    }

    protected fun extractElement(source: String): String {
        val dataIdx = source.indexOf("data class")
        val sealedIdx = source.indexOf("sealed interface")
        val enumIdx = source.indexOf("enum class")
        val candidates = listOf(dataIdx, sealedIdx, enumIdx).filter { it >= 0 }
        if (candidates.isEmpty()) {
            return source
                .trim()
                .trimIndent()
        }
        val startIdx = candidates.minOrNull()!!
        return source
            .substring(startIdx)
            .trim()
            .trimIndent()
    }
}
