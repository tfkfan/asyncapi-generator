package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.parser.node.ParserNode
import java.io.File

/**
 * Fixture helpers for tests that exercise [ParserNode] directly.
 *
 * These helpers create minimal parser nodes with source context attached, so
 * tests can verify parser-node behavior and error messages without loading a
 * complete AsyncAPI document.
 */
internal object ParserNodeFixtures {

    fun scalar(
        value: Any?,
        sourceLine: String,
        name: String = "value",
        path: String = "test.root.value",
        fileName: String = "asyncapi.yaml",
    ): ParserNode {
        val context = AsyncApiContext()
        context.registerSource(File(fileName), sourceLine)
        context.registerLine(path, 1)
        return ParserNode(
            name = name,
            node = value,
            path = path,
            context = context,
        )
    }
}
