package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import dev.banking.asyncapi.generator.core.validator.util.ValidationResults
import java.io.File

/**
 * Fixture facade for validator-stage tests.
 *
 * It builds parsed documents through [ParserFixtures] and exposes validation
 * helpers that keep validator tests focused on expected validation results.
 */
internal class ValidatorFixtures(
    context: AsyncApiContext = AsyncApiContext(),
) {
    private val parserFixtures = ParserFixtures(context)
    private val validator = AsyncApiValidator(context)

    fun document(path: String): AsyncApiDocument =
        parserFixtures.document(path)

    fun document(file: File): AsyncApiDocument =
        parserFixtures.document(file)

    fun validate(document: AsyncApiDocument): ValidationResults =
        validator.validate(document)

    fun validate(path: String): ValidationResults =
        validate(document(path))

    fun validate(file: File): ValidationResults =
        validate(document(file))

    fun validatedDocument(path: String): AsyncApiDocument =
        validatedDocument(TestResources.file(path))

    fun validatedDocument(file: File): AsyncApiDocument {
        val document = document(file)
        validate(document).apply {
            logWarnings()
            throwErrors()
        }
        return document
    }
}
