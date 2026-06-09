package dev.banking.asyncapi.generator.gradle.plugin.tasks

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorClientType
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationFactory
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorSchemaMode
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.JAVA
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.KOTLIN
import dev.banking.asyncapi.generator.core.parser.AsyncApiParser
import dev.banking.asyncapi.generator.core.registry.AsyncApiRegistry
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault
import java.util.Locale
import kotlin.text.get

@DisableCachingByDefault(because = "Codegen output is cheap to reproduce and not worth caching")
abstract class GenerateAsyncApiTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputFile: RegularFileProperty

    @get:OutputFile
    @get:Optional
    abstract val outputFile: RegularFileProperty

    @get:OutputDirectory
    abstract val codegenOutputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val resourceOutputDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val modelPackage: Property<String>

    @get:Input
    @get:Optional
    abstract val clientPackage: Property<String>

    @get:Input
    @get:Optional
    abstract val schemaPackage: Property<String>

    @get:Input
    abstract val generatorName: Property<String>

    @get:Input
    @get:Optional
    abstract val clientType: Property<String>

    @get:Input
    @get:Optional
    abstract val schemaMode: Property<String>

    @get:Input
    @get:Optional
    abstract val modelAnnotation: Property<String>

    @get:Input
    abstract val kafkaTopicsPropertyPrefix: Property<String>

    @TaskAction
    fun generate() {
        logger.lifecycle("asyncapi-generator-gradle-plugin started")

        val context = AsyncApiContext()
        val parser = AsyncApiParser(context)
        val validator = AsyncApiValidator(context)
        val bundler = AsyncApiBundler()
        val generator = AsyncApiGenerator()

        val root = AsyncApiRegistry.readYaml(inputFile.get().asFile, context)
        val asyncApiDocument = parser.parse(root)
        val validationErrors = validator.validate(asyncApiDocument)

        validationErrors.logWarnings()
        validationErrors.throwErrors()

        val bundled = bundler.bundle(asyncApiDocument)

        if (outputFile.isPresent) {
            val file = outputFile.get().asFile
            logger.lifecycle("Writing bundled AsyncAPI specification to: ${file.absolutePath}")
            AsyncApiRegistry.writeYaml(file, bundled)
        }

        val genNameString = generatorName.get()
        val targetLanguage =
            try {
                GeneratorName.valueOf(genNameString.uppercase(Locale.getDefault()))
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid generatorName '$genNameString'. Supported values: ${GeneratorName.entries}")
            }

        // Calculate Source Root
        val sourceRootName =
            when (targetLanguage) {
                KOTLIN -> "src/main/kotlin"
                JAVA -> "src/main/java"
        }
        val codegenSourceRoot = codegenOutputDirectory.get().asFile.resolve(sourceRootName)

        val selectedClientType = GeneratorClientType.fromConfigValue(clientType.orNull)
        val selectedSchemaMode = GeneratorSchemaMode.fromConfigValue(schemaMode.orNull)
        val selectedModelAnnotation = modelAnnotation.orNull
        val topicPropertyPrefix = kafkaTopicsPropertyPrefix.get()

        val generatorConfiguration =
            GeneratorConfigurationFactory.create(
                GeneratorConfigurationRequest(
                    language = targetLanguage,
                    sourceOutputDirectory = codegenSourceRoot,
                    resourceOutputDirectory = resourceOutputDirectory.get().asFile,
                    modelPackageName = modelPackage.orNull,
                    clientPackageName = clientPackage.orNull,
                    schemaPackageName = schemaPackage.orNull,
                    clientType = selectedClientType,
                    schemaMode = selectedSchemaMode,
                    modelAnnotation = selectedModelAnnotation,
                    kafkaTopicsPropertyPrefix = topicPropertyPrefix,
                ),
            )
        if (generatorConfiguration.hasConfiguredOutputs()) {
            generator.generate(bundled, generatorConfiguration)
        }
        logger.lifecycle("asyncapi-generator-gradle-plugin completed")
    }
}
