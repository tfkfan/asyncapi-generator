package dev.banking.asyncapi.generator.core.parser

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.ParserFixtures
import dev.banking.asyncapi.generator.core.parser.node.ParserNode

abstract class AbstractParserTest {

    protected val asyncApiContext = AsyncApiContext()
    private val parserFixtures = ParserFixtures(asyncApiContext)

    protected fun readYaml(path: String): ParserNode {
        return parserFixtures.root(path)
    }
}
