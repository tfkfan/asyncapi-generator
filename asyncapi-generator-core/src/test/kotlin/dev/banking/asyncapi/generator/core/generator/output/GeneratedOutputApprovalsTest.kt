package dev.banking.asyncapi.generator.core.generator.output

import dev.banking.asyncapi.generator.core.fixtures.GeneratedOutputApprovals
import org.junit.jupiter.api.Test

class GeneratedOutputApprovalsTest {
    @Test
    fun `approval fixture verifies generated artifact metadata and content`() {
        GeneratedOutputApprovals.verifyArtifact(
            approvalName = "generated-output-approval-fixture",
            artifact =
                GeneratedArtifact(
                    relativePath = "com/example/User.kt",
                    content = "data class User(val id: String)\n",
                    kind = GeneratedArtifactKind.SOURCE,
                ),
        )
    }

    @Test
    fun `approval fixture verifies generation result metadata and content`() {
        GeneratedOutputApprovals.verifyResult(
            approvalName = "generated-output-result-approval-fixture",
            result =
                GenerationResult.of(
                    GeneratedArtifact(
                        relativePath = "com/example/User.kt",
                        content = "data class User(val id: String)\n",
                        kind = GeneratedArtifactKind.SOURCE,
                    ),
                    GeneratedArtifact(
                        relativePath = "com/example/schema/User.avsc",
                        content = """{"type":"record","name":"User"}""",
                        kind = GeneratedArtifactKind.SCHEMA,
                    ),
                ),
        )
    }
}
