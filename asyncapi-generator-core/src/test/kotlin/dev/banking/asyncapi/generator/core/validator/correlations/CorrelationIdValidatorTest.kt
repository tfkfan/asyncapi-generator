package dev.banking.asyncapi.generator.core.validator.correlations

import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.WARNING
import dev.banking.asyncapi.generator.core.validator.AbstractValidatorTest
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CorrelationIdValidatorTest : AbstractValidatorTest() {

    private val asyncApiValidator = AsyncApiValidator(asyncApiContext)

    @Test
    fun `invalid correlation IDs trigger warnings`() {
        val document = parse("validator/correlations/asyncapi_validator_correlation_invalid.yaml")
        val results = asyncApiValidator.validate(document)
        assertFalse(results.hasErrors(), "Expected no errors, but found: ${results.errors}")

        val warnings = results.warnings
        assertEquals(1, warnings.size, "Expected 1 warning.")
        assertFinding(
            results,
            severity = WARNING,
            messageContains = "does not follow valid runtime expression",
            sourceFile = "asyncapi_validator_correlation_invalid.yaml",
            path = "asyncapi_validator_correlation_invalid.root.components.correlationIds.InvalidLocationRegex.location",
            line = 10,
        )
    }

    @Test
    fun `valid correlation ID passes validation`() {
        val document = parse("validator/correlations/asyncapi_validator_correlation_valid.yaml")
        val results = asyncApiValidator.validate(document)
        assertNoFindings(results)
    }
}
