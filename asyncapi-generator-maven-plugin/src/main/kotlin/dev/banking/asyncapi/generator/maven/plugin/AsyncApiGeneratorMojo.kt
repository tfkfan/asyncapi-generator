package dev.banking.asyncapi.generator.maven.plugin

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.ClientGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorClientType
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorOutputConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorSchemaMode
import dev.banking.asyncapi.generator.core.generator.configuration.ModelGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.SchemaGeneration
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.parser.AsyncApiParser
import dev.banking.asyncapi.generator.core.registry.AsyncApiRegistry
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.util.Locale

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class AsyncApiGeneratorMojo : AbstractMojo() {
    @Parameter(defaultValue = "\${project}", readonly = true)
    private lateinit var project: MavenProject

    @Parameter(property = "generatorName", defaultValue = "kotlin")
    private lateinit var generatorName: String

    @Parameter(property = "inputFile", required = true)
    private lateinit var inputFile: File

    @Parameter(property = "outputFile")
    private var outputFile: File? = null

    @Parameter(
        property = "codegenOutputDirectory",
        defaultValue = "\${project.build.directory}/generated-sources/asyncapi",
    )
    private lateinit var codegenOutputDirectory: File

    @Parameter(
        property = "resourceOutputDirectory",
        defaultValue = "\${project.build.directory}/generated-resources/asyncapi",
    )
    private lateinit var resourceOutputDirectory: File

    @Parameter(property = "modelPackage")
    private var modelPackage: String? = null

    @Parameter(property = "clientPackage")
    private var clientPackage: String? = null

    @Parameter(property = "schemaPackage")
    private var schemaPackage: String? = null

    @Parameter(property = "clientType")
    private var clientType: String? = null

    @Parameter(property = "schemaMode")
    private var schemaMode: String? = null

    @Parameter(property = "modelAnnotation")
    private var modelAnnotation: String? = null

    @Parameter(property = "kafkaTopicsPropertyPrefix", defaultValue = "kafka.topics")
    private var kafkaTopicsPropertyPrefix: String = "kafka.topics"

    private val context = AsyncApiContext()
    private val parser = AsyncApiParser(context)
    private val validator = AsyncApiValidator(context)
    private val bundler = AsyncApiBundler()
    private val generator = AsyncApiGenerator()

    override fun execute() {
        try {
            log.info("asyncapi-generator-maven-plugin started")
            if (!inputFile.exists()) {
                throw MojoExecutionException("Input file not found: $inputFile")
            }
            val root = AsyncApiRegistry.readYaml(inputFile, context)
            val asyncApiParsed = parser.parse(root)
            val validationErrors = validator.validate(asyncApiParsed)
            validationErrors.logWarnings()
            validationErrors.throwErrors()
            val bundled = bundler.bundle(asyncApiParsed)
            outputFile?.let { file ->
                log.info("Writing bundled AsyncAPI specification to: ${file.absolutePath}")
                AsyncApiRegistry.writeYaml(file, bundled)
            }
            val targetLanguage =
                try {
                    GeneratorName.valueOf(generatorName.uppercase(Locale.getDefault()))
                } catch (_: IllegalArgumentException) {
                    throw MojoExecutionException(
                        "Invalid generatorName '$generatorName'. Supported values: ${
                            GeneratorName.entries.joinToString(
                                ", "
                            )
                        }",
                    )
                }
            val selectedClientType =
                try {
                    GeneratorClientType.fromConfigValue(clientType)
                } catch (exception: IllegalArgumentException) {
                    throw MojoExecutionException(exception.message, exception)
                }
            val selectedSchemaMode =
                try {
                    GeneratorSchemaMode.fromConfigValue(schemaMode)
                } catch (exception: IllegalArgumentException) {
                    throw MojoExecutionException(exception.message, exception)
                }
            val hasModelPackage = modelPackage != null
            val hasClientPackage = clientPackage != null
            val hasSchemaPackage = schemaPackage != null
            if (selectedClientType != null && selectedClientType != GeneratorClientType.NONE && !hasClientPackage) {
                throw MojoExecutionException("clientType requires clientPackage")
            }
            if (selectedSchemaMode != null && selectedSchemaMode != GeneratorSchemaMode.NONE && !hasSchemaPackage) {
                throw MojoExecutionException("schemaMode requires schemaPackage")
            }
            if (modelAnnotation != null && !hasModelPackage) {
                throw MojoExecutionException("modelAnnotation requires modelPackage")
            }
            if (kafkaTopicsPropertyPrefix.isBlank()) {
                throw MojoExecutionException("kafkaTopicsPropertyPrefix cannot be empty")
            }
            if (hasModelPackage || hasClientPackage || hasSchemaPackage) {
                val effectiveModelPackage = modelPackage ?: "unused"
                val effectiveClientPackage = clientPackage ?: "unused"
                val effectiveSchemaPackage = schemaPackage ?: "unused"
                val generatorConfiguration =
                    GeneratorConfiguration(
                        language = targetLanguage,
                        output =
                            GeneratorOutputConfiguration(
                                sourceOutputDirectory = codegenOutputDirectory,
                                resourceOutputDirectory = resourceOutputDirectory,
                            ),
                        models =
                            if (hasModelPackage) {
                                ModelGeneration.Enabled(
                                    packageName = effectiveModelPackage,
                                    annotation = modelAnnotation,
                                )
                            } else {
                                ModelGeneration.Disabled
                            },
                        schemas =
                            buildList {
                                if (hasSchemaPackage && selectedSchemaMode == GeneratorSchemaMode.AVRO_PROJECTION) {
                                    add(SchemaGeneration.AvroProjection(effectiveSchemaPackage))
                                }
                            },
                        clients =
                            buildList {
                                val springKafkaClientType = selectedClientType?.springKafkaClientType
                                if (hasClientPackage && springKafkaClientType != null) {
                                    add(
                                        ClientGeneration.SpringKafka(
                                            packageName = effectiveClientPackage,
                                            modelPackageName = effectiveModelPackage,
                                            clientType = springKafkaClientType,
                                            topicPropertyPrefix = kafkaTopicsPropertyPrefix,
                                        ),
                                    )
                                }
                                if (hasClientPackage && selectedClientType == GeneratorClientType.QUARKUS_KAFKA) {
                                    add(
                                        ClientGeneration.QuarkusKafka(
                                            packageName = effectiveClientPackage,
                                            modelPackageName = effectiveModelPackage,
                                        ),
                                    )
                                }
                            },
                    )
                generator.generate(bundled, generatorConfiguration)
            }
            project.addCompileSourceRoot(codegenOutputDirectory.absolutePath)
            log.info("asyncapi-generator-maven-plugin completed successfully")
        } catch (e: Exception) {
            throw MojoExecutionException(e)
        }
    }
}
