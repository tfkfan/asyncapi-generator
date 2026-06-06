package dev.banking.asyncapi.generator.core.validator.util

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.validator.ValidationFinding

/**
 * Formats validation findings for exceptions and logs.
 *
 * Expected behavior is covered by:
 * - `ValidationResultsTest`
 * - validator package tests
 */
object ValidationFindingFormatter {

    fun format(
        title: String,
        findings: List<ValidationFinding>,
        asyncApiContext: AsyncApiContext,
    ): String = buildString {
        appendLine(title)
        appendLine()
        findings.forEach { finding ->
            appendLine(">> ${finding.message}")
            appendLine()
            appendLine(snippet(finding, asyncApiContext))
            appendLine()
            if (finding.doc != null) appendLine("See documentation: ${finding.doc}")
            appendLine("---------------------------------------------------------------------------------------------------------------------")
            appendLine()
        }
    }

    private fun snippet(
        finding: ValidationFinding,
        asyncApiContext: AsyncApiContext,
    ): String {
        finding.sourceLocation?.let { return asyncApiContext.sourceSnippet(it) }
        finding.path?.let { return asyncApiContext.pathSnippet(it) }
        return "(no source location available)"
    }
}
