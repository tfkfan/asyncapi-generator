package dev.banking.asyncapi.generator.core.validator.schemas

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiValidateException
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.ERROR
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.WARNING
import dev.banking.asyncapi.generator.core.validator.AbstractValidatorTest
import dev.banking.asyncapi.generator.core.validator.AsyncApiValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SchemaValidatorTest : AbstractValidatorTest() {

    private val asyncApiValidator = AsyncApiValidator(asyncApiContext)

    @Test
    fun `valid simple schema passes validation`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_valid_simple.yaml")
        val results = asyncApiValidator.validate(document)

        assertNoFindings(results)
    }

    @Test
    fun `schema with invalid type field throws validation error`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_invalid_type.yaml")
        val validationResults = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            validationResults.throwErrors()
        }

        assertEquals(1, exception.errors.size, "Expected exactly one validation error for invalid schema type.")
    }

    @Test
    fun `schema with invalid numeric and string constraints throws validation errors`() {
        val document =
            parse("validator/schemas/asyncapi_validator_schema_invalid_constraints.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(3, exception.errors.size, "Expected 3 validation errors for invalid constraints.")
    }

    @Test
    fun `schema validation findings include source locations for nested schema errors`() {
        val results = validate("validator/schemas/asyncapi_validator_schema_invalid_constraints.yaml")

        assertEquals(3, results.errors.size)
        assertEquals(3, results.findings.size)

        assertFinding(
            results,
            severity = ERROR,
            messageContains = "'minimum' (10.0) cannot be greater than 'maximum' (5.0)",
            sourceFile = "asyncapi_validator_schema_invalid_constraints.yaml",
            path = "asyncapi_validator_schema_invalid_constraints.root.components.schemas.InvalidNumericRange.minimum",
            line = 9,
        )
        assertFinding(
            results,
            severity = ERROR,
            messageContains = "'multipleOf' must be greater than zero",
            sourceFile = "asyncapi_validator_schema_invalid_constraints.yaml",
            path = "asyncapi_validator_schema_invalid_constraints.root.components.schemas.InvalidMultipleOf.multipleOf",
            line = 14,
        )
        assertFinding(
            results,
            severity = ERROR,
            messageContains = "'minLength' (10) cannot be greater than 'maxLength' (5)",
            sourceFile = "asyncapi_validator_schema_invalid_constraints.yaml",
            path = "asyncapi_validator_schema_invalid_constraints.root.components.schemas.InvalidStringLength.minLength",
            line = 18,
        )
    }

    @Test
    fun `schema with invalid discriminator definition throws validation errors`() {
        val document =
            parse("validator/schemas/asyncapi_validator_schema_invalid_discriminator.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(2, exception.errors.size, "Expected 2 errors for invalid discriminator definitions.")
    }

    @Test
    fun `schema with incompatible default value throws validation errors`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_invalid_default.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(3, exception.errors.size, "Expected 3 validation errors for incompatible default values.")
    }

    @Test
    fun `schema with incompatible const value throws validation errors`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_invalid_const.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(3, exception.errors.size, "Expected 3 validation errors for incompatible const values.")
    }

    @Test
    fun `schema with ambiguous composition or empty fields triggers warnings`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_warnings.yaml")
        val results = asyncApiValidator.validate(document)
        assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertTrue(results.hasErrors(), "Should have errors.")
        assertTrue(results.hasWarnings(), "Should have warnings.")

        val warnings = results.warnings
        assertEquals(2, warnings.size, "Expected 2 warnings for incompatible composition values.")
    }

    @Test
    fun `schema validation findings include source locations for nested schema warnings`() {
        val results = validate("validator/schemas/asyncapi_validator_schema_warnings.yaml")

        assertEquals(2, results.warnings.size)

        assertFinding(
            results,
            severity = WARNING,
            messageContains = "uses multiple composition keywords",
            sourceFile = "asyncapi_validator_schema_warnings.yaml",
            path = "asyncapi_validator_schema_warnings.root.components.schemas.AmbiguousComposition.allOf",
            line = 9,
        )
        assertFinding(
            results,
            severity = WARNING,
            messageContains = "defines an empty 'required' list",
            sourceFile = "asyncapi_validator_schema_warnings.yaml",
            path = "asyncapi_validator_schema_warnings.root.components.schemas.EmptyRequiredObject.required",
            line = 17,
        )
    }

    @Test
    fun `schema with invalid array or object structure throws validation errors`() {
        val document = parse("validator/schemas/asyncapi_validator_schema_invalid_structure.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(2, exception.errors.size)
    }

    @Test
    fun `schema with required property default null throws validation error`() {
        val document =
            parse("validator/schemas/asyncapi_validator_schema_default_null_required.yaml")
        val results = asyncApiValidator.validate(document)
        val exception = assertFailsWith<AsyncApiValidateException.ValidateError> {
            results.throwErrors()
        }
        assertEquals(1, exception.errors.size)
    }
}
