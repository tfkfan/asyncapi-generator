package dev.banking.asyncapi.generator.core.parser

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.ParserFixtures
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import dev.banking.asyncapi.generator.core.parser.node.ParserNode

/**
 * Shared support for parser-stage tests.
 *
 * It exposes reader-backed parser roots so parser tests can focus on
 * `ParserNode` input and model output instead of file-format handling.
 */
abstract class ParserTestSupport {

    protected val asyncApiContext = AsyncApiContext()
    private val parserFixtures = ParserFixtures(asyncApiContext)

    protected fun readRoot(path: String): ParserNode {
        return parserFixtures.root(path)
    }

    protected fun parseDocument(path: String): AsyncApiDocument {
        return parserFixtures.document(path)
    }
}
