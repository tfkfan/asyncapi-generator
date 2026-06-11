package dev.banking.asyncapi.generator.core.generator.artifact

import dev.banking.asyncapi.generator.core.fixtures.GenerationInputFixtures
import dev.banking.asyncapi.generator.core.generator.output.FileSystemGeneratedArtifactWriter
import dev.banking.asyncapi.generator.core.generator.plan.GenerationTask
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeAvroArtifactGenerationTest {
    private val generation = NativeAvroArtifactGeneration()
    private val fixtures = GenerationInputFixtures()

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `generate writes native Avro schema artifacts through writer`() {
        val sourceOutputDirectory = tempDir.resolve("sources").toFile()
        val resourceOutputDirectory = tempDir.resolve("resources").toFile()
        val artifactWriter =
            FileSystemGeneratedArtifactWriter(
                sourceOutputDirectory = sourceOutputDirectory,
                resourceOutputDirectory = resourceOutputDirectory,
            )

        generation.generate(
            task = GenerationTask.NativeAvroArtifacts(generateSpecificRecords = true),
            generationInput = fixtures.generationInputWithNativeAvroSchema(),
            artifactWriter = artifactWriter,
        )

        assertTrue(resourceOutputDirectory.resolve("com/example/avro/UserCreated.avsc").exists())
        assertTrue(sourceOutputDirectory.resolve("com/example/avro/UserCreated.java").exists())
        assertFalse(sourceOutputDirectory.resolve("com/example/avro/UserCreated.avsc").exists())
        assertFalse(resourceOutputDirectory.resolve("com/example/avro/UserCreated.java").exists())
    }
}
