package dev.banking.asyncapi.generator.core.validator.util

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiValidateException
import dev.banking.asyncapi.generator.core.model.validator.ValidationError
import dev.banking.asyncapi.generator.core.model.validator.ValidationFinding
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.ERROR
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.WARNING
import dev.banking.asyncapi.generator.core.model.validator.ValidationWarning
import dev.banking.asyncapi.generator.core.reader.SourceLocation
import org.slf4j.LoggerFactory

/**
 * Collects validation findings produced by the validator stage.
 *
 * Expected behavior is covered by:
 * - `ValidationResultsTest`
 * - validator package tests
 */
class ValidationResults(
    val asyncApiContext: AsyncApiContext,
) {
    private val logger = LoggerFactory.getLogger(ValidationResults::class.java)

    private val _errors = mutableListOf<ValidationError>()
    private val _warnings = mutableListOf<ValidationWarning>()
    private val _findings = mutableListOf<ValidationFinding>()

    val errors: List<ValidationError> get() = _errors
    val warnings: List<ValidationWarning> get() = _warnings
    val findings: List<ValidationFinding> get() = _findings

    fun error(
        message: String,
        line: Int? = null,
        doc: String? = null,
        sourceLocation: SourceLocation? = null,
        path: String? = sourceLocation?.path,
    ) {
        val finding = ValidationFinding(
            severity = ERROR,
            message = message,
            sourceLocation = sourceLocation,
            path = path,
            line = line ?: sourceLocation?.line,
            doc = doc,
        )
        _findings += finding
        _errors += ValidationError(message, finding.line, doc)
    }

    fun warn(
        message: String,
        line: Int? = null,
        doc: String? = null,
        sourceLocation: SourceLocation? = null,
        path: String? = sourceLocation?.path,
    ) {
        val finding = ValidationFinding(
            severity = WARNING,
            message = message,
            sourceLocation = sourceLocation,
            path = path,
            line = line ?: sourceLocation?.line,
            doc = doc,
        )
        _findings += finding
        _warnings += ValidationWarning(message, finding.line, doc)
    }

    fun hasErrors() = _errors.isNotEmpty()

    fun hasWarnings() = _warnings.isNotEmpty()

    fun throwErrors() {
        if (_errors.isNotEmpty()) {
            throw AsyncApiValidateException.ValidateError(_errors, asyncApiContext)
        }
    }

    fun logWarnings() {
        if (_warnings.isNotEmpty()) {
            logger.warn(
                buildString {
                    appendLine("Validation found ${_warnings.size} warning(s):")
                    appendLine()
                    _warnings.forEach { warning ->
                        appendLine(">> ${warning.message}")
                        appendLine()
                        appendLine(asyncApiContext.validatorSnippet(warning.line ?: -1))
                        appendLine()
                        if (warning.doc != null) appendLine("See documentation: ${warning.doc}")
                        appendLine("---------------------------------------------------------------------------------------------------------------------")
                        appendLine()
                    }
                }
            )
        }
    }
}
