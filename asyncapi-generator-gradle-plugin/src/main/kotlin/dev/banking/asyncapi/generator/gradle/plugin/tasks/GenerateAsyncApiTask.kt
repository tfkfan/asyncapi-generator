package dev.banking.asyncapi.generator.gradle.plugin.tasks

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationFactory
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.JAVA
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName.KOTLIN
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
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
    abstract val modelsEnabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val modelsPackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val modelsAnnotation: Property<String>

    @get:Input
    @get:Optional
    abstract val avroProjectionEnabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val avroProjectionPackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val springKafkaEnabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val springKafkaPackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val springKafkaModelPackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val springKafkaMode: Property<String>

    @get:Input
    @get:Optional
    abstract val springKafkaTopicPropertyPrefix: Property<String>

    @get:Input
    @get:Optional
    abstract val quarkusKafkaEnabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val quarkusKafkaPackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val quarkusKafkaModelPackageName: Property<String>

    @get:Input
    abstract val generatorName: Property<String>

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

        val generatorConfiguration =
            GeneratorConfigurationFactory.create(
                GeneratorConfigurationRequest(
                    language = targetLanguage,
                    sourceOutputDirectory = codegenSourceRoot,
                    resourceOutputDirectory = resourceOutputDirectory.get().asFile,
                    models =
                        modelRequest(
                            enabled = modelsEnabled.orNull,
                            packageName = modelsPackageName.orNull,
                            annotation = modelsAnnotation.orNull,
                        ),
                    schemas =
                        schemaRequest(
                            avroProjectionEnabled = avroProjectionEnabled.orNull,
                            avroProjectionPackageName = avroProjectionPackageName.orNull,
                        ),
                    clients =
                        clientRequest(
                            springKafkaEnabled = springKafkaEnabled.orNull,
                            springKafkaPackageName = springKafkaPackageName.orNull,
                            springKafkaModelPackageName = springKafkaModelPackageName.orNull,
                            springKafkaMode = springKafkaMode.orNull,
                            springKafkaTopicPropertyPrefix = springKafkaTopicPropertyPrefix.orNull,
                            quarkusKafkaEnabled = quarkusKafkaEnabled.orNull,
                            quarkusKafkaPackageName = quarkusKafkaPackageName.orNull,
                            quarkusKafkaModelPackageName = quarkusKafkaModelPackageName.orNull,
                        ),
                ),
            )
        if (generatorConfiguration.hasConfiguredOutputs()) {
            generator.generate(bundled, generatorConfiguration)
        }
        logger.lifecycle("asyncapi-generator-gradle-plugin completed")
    }

    private fun modelRequest(
        enabled: Boolean?,
        packageName: String?,
        annotation: String?,
    ): GeneratorConfigurationRequest.Models? =
        when {
            enabled == false -> null
            enabled == true || packageName != null || annotation != null ->
                GeneratorConfigurationRequest.Models(
                    packageName = packageName,
                    annotation = annotation,
                )
            else -> null
        }

    private fun schemaRequest(
        avroProjectionEnabled: Boolean?,
        avroProjectionPackageName: String?,
    ): GeneratorConfigurationRequest.Schemas =
        GeneratorConfigurationRequest.Schemas(
            avroProjection =
                when {
                    avroProjectionEnabled == false -> null
                    avroProjectionEnabled == true || avroProjectionPackageName != null ->
                        GeneratorConfigurationRequest.AvroProjection(packageName = avroProjectionPackageName)
                    else -> null
                },
        )

    private fun clientRequest(
        springKafkaEnabled: Boolean?,
        springKafkaPackageName: String?,
        springKafkaModelPackageName: String?,
        springKafkaMode: String?,
        springKafkaTopicPropertyPrefix: String?,
        quarkusKafkaEnabled: Boolean?,
        quarkusKafkaPackageName: String?,
        quarkusKafkaModelPackageName: String?,
    ): GeneratorConfigurationRequest.Clients =
        GeneratorConfigurationRequest.Clients(
            springKafka =
                springKafkaRequest(
                    enabled = springKafkaEnabled,
                    packageName = springKafkaPackageName,
                    modelPackageName = springKafkaModelPackageName,
                    mode = springKafkaMode,
                    topicPropertyPrefix = springKafkaTopicPropertyPrefix,
                ),
            quarkusKafka =
                when {
                    quarkusKafkaEnabled == false -> null
                    quarkusKafkaEnabled == true ||
                        quarkusKafkaPackageName != null ||
                        quarkusKafkaModelPackageName != null ->
                        GeneratorConfigurationRequest.QuarkusKafka(
                            packageName = quarkusKafkaPackageName,
                            modelPackageName = quarkusKafkaModelPackageName,
                        )
                    else -> null
                },
        )

    private fun springKafkaRequest(
        enabled: Boolean?,
        packageName: String?,
        modelPackageName: String?,
        mode: String?,
        topicPropertyPrefix: String?,
    ): GeneratorConfigurationRequest.SpringKafka? =
        when {
            enabled == false -> null
            enabled == true ||
                packageName != null ||
                modelPackageName != null ||
                mode != null ||
                topicPropertyPrefix != null ->
                GeneratorConfigurationRequest.SpringKafka(
                    packageName = packageName,
                    modelPackageName = modelPackageName,
                    clientType =
                        SpringKafkaClientType.fromConfigurationValue(
                            value = mode,
                            path = "clients.springKafka.mode",
                        ),
                    topicPropertyPrefix =
                        topicPropertyPrefix
                            ?: GeneratorConfigurationRequest.DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
                )
            else -> null
        }
}
