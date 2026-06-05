package dev.banking.asyncapi.generator.core.generator

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.BundlerFixtures
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.model.GeneratorOptions
import java.io.File

abstract class AbstractAvroGeneratorClass {

    protected val asyncApiContext = AsyncApiContext()
    private val bundlerFixtures = BundlerFixtures(asyncApiContext)
    protected val generator = AsyncApiGenerator()

    protected fun generateAvro(
        yaml: File,
        codegenOutputDirectory: File = File("target/generated-sources/asyncapi"),
        resourceOutputDirectory: File = File("target/generated-resources/asyncapi"),
        packageName: String,
        schema: String? = null,
    ): String {
        val bundled = bundlerFixtures.bundledDocument(yaml)

        val generatorOptions = GeneratorOptions(
            generatorName = GeneratorName.KOTLIN,
            modelPackage = packageName,
            clientPackage = packageName,
            schemaPackage = packageName,
            codegenOutputDirectory = codegenOutputDirectory,
            resourceOutputDirectory = resourceOutputDirectory,
            generateModels = false,
            generateSpringKafkaClient = false,
            generateAvroSchema = true,
        )

        generator.generate(
            asyncApiDocument = bundled,
            generatorOptions = generatorOptions,
        )

        if (schema != null) {
            val packagePath = packageName.replace('.', '/')
            val output = resourceOutputDirectory
                .resolve(packagePath)
                .resolve(schema)
            if (output.exists()) return output.readText()
        }
        return ""
    }
}
