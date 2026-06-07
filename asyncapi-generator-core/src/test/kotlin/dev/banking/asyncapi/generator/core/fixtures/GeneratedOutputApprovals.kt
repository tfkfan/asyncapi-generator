package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.generator.output.GeneratedArtifact
import dev.banking.asyncapi.generator.core.generator.output.GenerationResult
import org.approvaltests.Approvals
import org.approvaltests.core.Options
import org.approvaltests.namer.ApprovalNamer
import org.approvaltests.namer.GetApprovalName
import org.approvaltests.namer.GetSourceFilePath
import org.approvaltests.namer.NamerWrapper
import org.approvaltests.reporters.QuietReporter
import java.io.File

/**
 * Approval fixture for rendered generator output.
 *
 * This fixture is intended for tests where the full generated artifact is the
 * behavior under review. Focused generator tests should continue to use direct
 * assertions when they are validating one mapper, name, option, or factory
 * decision.
 */
internal object GeneratedOutputApprovals {
    private val approvalDirectory = File("src/test/resources/approvals/generator")

    fun verifyArtifact(
        approvalName: String,
        artifact: GeneratedArtifact,
    ) {
        verifyText(approvalName, artifact.toApprovalText())
    }

    fun verifyResult(
        approvalName: String,
        result: GenerationResult,
    ) {
        verifyText(approvalName, result.toApprovalText())
    }

    private fun verifyText(
        approvalName: String,
        content: String,
    ) {
        require(approvalName.matches(Regex("[A-Za-z0-9._-]+"))) {
            "Approval name can only contain letters, numbers, dots, underscores, and hyphens: $approvalName"
        }
        approvalDirectory.mkdirs()
        Approvals.verify(
            content.normalizeLineEndings(),
            Options(QuietReporter.INSTANCE).forFile().withNamer(approvalNamer(approvalName)),
        )
    }

    private fun approvalNamer(approvalName: String): ApprovalNamer =
        NamerWrapper(
            { approvalName },
            { approvalDirectory.absolutePath },
        )

    private fun GeneratedArtifact.toApprovalText(): String =
        buildString {
            appendLine("kind: $kind")
            appendLine("path: $relativePath")
            appendLine("---")
            append(content)
        }

    private fun GenerationResult.toApprovalText(): String =
        artifacts.joinToString(separator = "\n\n") { artifact ->
            artifact.toApprovalText()
        }

    private fun String.normalizeLineEndings(): String =
        replace("\r\n", "\n").replace("\r", "\n")
}
