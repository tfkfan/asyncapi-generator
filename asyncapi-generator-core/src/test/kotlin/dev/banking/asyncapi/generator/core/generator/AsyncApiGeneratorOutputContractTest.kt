package dev.banking.asyncapi.generator.core.generator

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.BundlerFixtures
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.GeneratorOutputConfiguration
import dev.banking.asyncapi.generator.core.generator.configuration.ModelGeneration
import dev.banking.asyncapi.generator.core.generator.configuration.SchemaGeneration
import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AsyncApiGeneratorOutputContractTest {
    private val asyncApiContext = AsyncApiContext()
    private val bundlerFixtures = BundlerFixtures(asyncApiContext)
    private val generator = AsyncApiGenerator()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `generate writes model artifacts to source output directory`() {
        val sourceOutputDirectory = tempDir.resolve("sources").toFile()
        val resourceOutputDirectory = tempDir.resolve("resources").toFile()
        val bundled = bundledDocument()

        generator.generate(
            asyncApiDocument = bundled,
            generatorConfiguration =
                generatorConfiguration(
                    sourceOutputDirectory = sourceOutputDirectory,
                    resourceOutputDirectory = resourceOutputDirectory,
                    models = ModelGeneration.Enabled(packageName = "com.example.model"),
                ),
        )

        assertTrue(sourceOutputDirectory.resolve("com/example/model/Task.kt").exists())
        assertFalse(resourceOutputDirectory.resolve("com/example/model/Task.kt").exists())
    }

    @Test
    fun `generate writes schema artifacts to resource output directory`() {
        val sourceOutputDirectory = tempDir.resolve("sources").toFile()
        val resourceOutputDirectory = tempDir.resolve("resources").toFile()
        val bundled = bundledDocument()

        generator.generate(
            asyncApiDocument = bundled,
            generatorConfiguration =
                generatorConfiguration(
                    sourceOutputDirectory = sourceOutputDirectory,
                    resourceOutputDirectory = resourceOutputDirectory,
                    schemas = listOf(SchemaGeneration.AvroProjection(packageName = "com.example.avro")),
                ),
        )

        assertTrue(resourceOutputDirectory.resolve("com/example/avro/Task.avsc").exists())
        assertFalse(sourceOutputDirectory.resolve("com/example/avro/Task.avsc").exists())
    }

    private fun bundledDocument() =
        bundlerFixtures.bundledDocument(
            File("src/test/resources/generator/asyncapi_enum_default_value.yaml"),
        )

    private fun generatorConfiguration(
        sourceOutputDirectory: File,
        resourceOutputDirectory: File,
        models: ModelGeneration = ModelGeneration.Disabled,
        schemas: List<SchemaGeneration> = emptyList(),
    ): GeneratorConfiguration =
        GeneratorConfiguration(
            language = GeneratorName.KOTLIN,
            output =
                GeneratorOutputConfiguration(
                    sourceOutputDirectory = sourceOutputDirectory,
                    resourceOutputDirectory = resourceOutputDirectory,
                ),
            models = models,
            schemas = schemas,
        )
}
