package dev.banking.asyncapi.generator.core.generator.output

import java.io.File

/**
 * Filesystem-backed writer for generated artifacts.
 *
 * Source artifacts are written under [sourceOutputDirectory]. Java source
 * artifacts are written under [javaSourceOutputDirectory]. Resource and schema
 * artifacts are written under [resourceOutputDirectory].
 *
 * Expected behavior is covered by:
 * - `GeneratedArtifactWriterTest`
 */
class FileSystemGeneratedArtifactWriter(
    private val sourceOutputDirectory: File,
    private val resourceOutputDirectory: File,
    private val javaSourceOutputDirectory: File = sourceOutputDirectory,
) : GeneratedArtifactWriter {
    override fun write(result: GenerationResult) {
        result.artifacts.forEach(::writeArtifact)
    }

    private fun writeArtifact(artifact: GeneratedArtifact) {
        val outputRoot =
            when (artifact.kind) {
                GeneratedArtifactKind.SOURCE -> sourceOutputDirectory
                GeneratedArtifactKind.JAVA_SOURCE -> javaSourceOutputDirectory
                GeneratedArtifactKind.RESOURCE -> resourceOutputDirectory
                GeneratedArtifactKind.SCHEMA -> resourceOutputDirectory
            }
        val outputFile = outputRoot.toPath().resolve(artifact.relativePath).toFile()
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(artifact.content)
    }
}
