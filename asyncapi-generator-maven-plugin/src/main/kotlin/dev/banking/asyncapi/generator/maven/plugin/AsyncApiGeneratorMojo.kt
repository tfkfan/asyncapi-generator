package dev.banking.asyncapi.generator.maven.plugin

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.generator.AsyncApiGenerator
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorClientType
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationFactory
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfigurationRequest
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorSchemaMode
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

            val generatorConfiguration =
                GeneratorConfigurationFactory.create(
                    GeneratorConfigurationRequest(
                        language = targetLanguage,
                        sourceOutputDirectory = codegenOutputDirectory,
                        resourceOutputDirectory = resourceOutputDirectory,
                        modelPackageName = modelPackage,
                        clientPackageName = clientPackage,
                        schemaPackageName = schemaPackage,
                        clientType = selectedClientType,
                        schemaMode = selectedSchemaMode,
                        modelAnnotation = modelAnnotation,
                        kafkaTopicsPropertyPrefix = kafkaTopicsPropertyPrefix,
                    ),
                )
            if (generatorConfiguration.hasConfiguredOutputs()) {
                generator.generate(bundled, generatorConfiguration)
            }
            project.addCompileSourceRoot(codegenOutputDirectory.absolutePath)
            log.info("asyncapi-generator-maven-plugin completed successfully")
        } catch (e: Exception) {
            throw MojoExecutionException(e)
        }
    }
}
