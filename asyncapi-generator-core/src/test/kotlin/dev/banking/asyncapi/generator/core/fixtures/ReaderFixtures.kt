package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.reader.DocumentFormat
import dev.banking.asyncapi.generator.core.reader.DocumentSource
import java.io.File

internal object ReaderFixtures {

    fun yamlSource(name: String): DocumentSource {
        val file = yamlFile(name)
        return DocumentSource(
            id = file.nameWithoutExtension,
            file = file,
            content = file.readText(),
            format = DocumentFormat.YAML,
        )
    }

    fun yamlFile(name: String): File =
        TestResources.file("reader/yaml/$name")
}
