package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.bundler.AsyncApiBundler
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import java.io.File

internal class BundlerFixtures(
    context: AsyncApiContext = AsyncApiContext(),
) {
    private val validatorFixtures = ValidatorFixtures(context)
    private val bundler = AsyncApiBundler()

    fun validatedDocument(path: String): AsyncApiDocument =
        validatorFixtures.validatedDocument(path)

    fun validatedDocument(file: File): AsyncApiDocument =
        validatorFixtures.validatedDocument(file)

    fun bundledDocument(path: String): AsyncApiDocument =
        bundledDocument(TestResources.file(path))

    fun bundledDocument(file: File): AsyncApiDocument =
        bundler.bundle(validatedDocument(file))
}
