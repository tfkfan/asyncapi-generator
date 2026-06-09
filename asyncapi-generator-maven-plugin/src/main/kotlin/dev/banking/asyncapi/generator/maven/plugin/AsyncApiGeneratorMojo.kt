package dev.banking.asyncapi.generator.maven.plugin

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.ClientGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorOutputConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.ModelGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.SchemaGeneration
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
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

    @Parameter
    private var configOptions: Map<String, String> = emptyMap()

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
            val clientType = configOptions["client.type"]
            val schemaType = configOptions["schema.type"]
            val modelAnnotation = configOptions["model.annotation"]
            val prefixOverride = configOptions["kafka.topics.property.prefix"]
            val hasModelPackage = modelPackage != null
            val hasClientPackage = clientPackage != null
            val hasSchemaPackage = schemaPackage != null
            if (clientType != null && !hasClientPackage) {
                throw MojoExecutionException("client.type requires clientPackage")
            }
            if (schemaType != null && !hasSchemaPackage) {
                throw MojoExecutionException("schema.type requires schemaPackage")
            }
            if (modelAnnotation != null && !hasModelPackage) {
                throw MojoExecutionException("model.annotation requires modelPackage")
            }
            if (prefixOverride != null && prefixOverride.isBlank()) {
                throw MojoExecutionException("kafka.topics.property.prefix cannot be empty")
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
                                if (hasSchemaPackage && schemaType == "avro") {
                                    add(SchemaGeneration.AvroProjection(effectiveSchemaPackage))
                                }
                            },
                        clients =
                            buildList {
                                if (hasClientPackage && (clientType == "spring-kafka" || clientType == "spring-kafka-simple")) {
                                    add(
                                        ClientGeneration.SpringKafka(
                                            packageName = effectiveClientPackage,
                                            modelPackageName = effectiveModelPackage,
                                            clientType = SpringKafkaClientType.fromConfigValue(clientType),
                                            topicPropertyPrefix = prefixOverride ?: "kafka.topics",
                                        ),
                                    )
                                }
                                if (hasClientPackage && clientType == "quarkus-kafka") {
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
