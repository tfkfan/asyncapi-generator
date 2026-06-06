package dev.banking.asyncapi.generator.core.validator.asyncapi

import dev.banking.asyncapi.generator.core.validator.AbstractValidatorTest
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import dev.banking.asyncapi.generator.core.validator.ValidationStage
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class AsyncApiValidatorTest : AbstractValidatorTest() {

    private val asyncApiValidator = AsyncApiValidator(asyncApiContext)

    @Test
    fun validateAsyncApiDocument() {
        val asyncApiDocument = parse("asyncapi_kafka_single_file_example.yaml")
        val validationResults = asyncApiValidator.validate(asyncApiDocument)
        validationResults.logWarnings()
        validationResults.throwErrors()
    }

    @Test
    fun `validates parsed document through validator stage contract`() {
        val asyncApiDocument = parse("validator/info/asyncapi_validator_info_valid_simple.yaml")
        val validationStage: ValidationStage = asyncApiValidator

        val validationResults = validationStage.validate(asyncApiDocument)

        assertFalse(validationResults.hasErrors(), "Expected no validation errors.")
        assertFalse(validationResults.hasWarnings(), "Expected no validation warnings.")
    }
}
