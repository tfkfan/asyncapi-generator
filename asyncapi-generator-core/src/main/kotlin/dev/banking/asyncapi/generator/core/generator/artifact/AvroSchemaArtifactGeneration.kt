package dev.banking.asyncapi.generator.core.generator.artifact

import dev.banking.asyncapi.generator.core.generator.avro.AvroGenerator
import dev.banking.asyncapi.generator.core.generator.input.GenerationInput
import dev.banking.asyncapi.generator.core.generator.output.GeneratedArtifactWriter
import dev.banking.asyncapi.generator.core.generator.plan.GenerationTask
import java.io.File

/**
 * Renders planned Avro schema artifacts before writing them.
 *
 * Expected behavior is covered by:
 * - `AvroSchemaArtifactGenerationTest`
 */
class AvroSchemaArtifactGeneration {
    fun generate(
        task: GenerationTask.AvroSchemaArtifacts,
        generationInput: GenerationInput,
        resourceOutputDirectory: File,
        artifactWriter: GeneratedArtifactWriter,
    ) {
        val avroGenerator =
            AvroGenerator(
                outputDir = resourceOutputDirectory,
                packageName = task.packageName,
            )
        artifactWriter.write(avroGenerator.render(generationInput.schemas))
    }
}
