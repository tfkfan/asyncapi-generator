package dev.banking.asyncapi.generator.core.validator

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.ValidatorFixtures
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument

abstract class AbstractValidatorTest {

    protected val asyncApiContext = AsyncApiContext()
    private val validatorFixtures = ValidatorFixtures(asyncApiContext)

    /**
     * Reads and parses any YAML fixture path.
     *
     * Paths can be relative to test resources, or use the legacy
     * `src/test/resources/...` prefix.
     */
    protected fun parse(path: String): AsyncApiDocument {
        return validatorFixtures.document(path)
    }
}
