package dev.banking.asyncapi.generator.core.model.exceptions

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.validator.ValidationFinding
import dev.banking.asyncapi.generator.core.validator.util.ValidationFindingFormatter.format

sealed class AsyncApiValidateException(message: String) : Exception(message) {

    class ValidateError(
        val errors: List<ValidationFinding>,
        val context: AsyncApiContext
    ) : AsyncApiValidateException(
        format(
            title = "Validation failed with ${errors.size} error(s):",
            findings = errors,
            asyncApiContext = context,
        )
    )
}
