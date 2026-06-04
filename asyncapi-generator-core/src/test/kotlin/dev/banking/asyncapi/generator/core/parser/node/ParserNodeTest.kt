package dev.banking.asyncapi.generator.core.parser.node

import dev.banking.asyncapi.generator.core.fixtures.ParserNodeFixtures
import dev.banking.asyncapi.generator.core.fixtures.assertMessageContains
import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ParserNodeTest {

    @Test
    fun `coerce reports quoted boolean as yaml string when boolean is expected`() {
        val node = ParserNodeFixtures.scalar(
            value = "true",
            sourceLine = "deprecated: \"true\"",
        )
        val error = assertFailsWith<AsyncApiParseException.UnexpectedValue> {
            node.coerce<Boolean>()
        }
        error.assertMessageContains("expected Boolean")
        error.assertMessageContains("found String \"true\"")
        error.assertMessageContains("quoted booleans are strings in YAML")
        error.assertMessageContains("deprecated: \"true\"")
    }

    @Test
    fun `coerce reports quoted number as yaml string when number is expected`() {
        val node = ParserNodeFixtures.scalar(
            value = "12",
            sourceLine = "minLength: \"12\"",
        )
        val error = assertFailsWith<AsyncApiParseException.UnexpectedValue> {
            node.coerce<Number>()
        }
        error.assertMessageContains("expected Number")
        error.assertMessageContains("found String \"12\"")
        error.assertMessageContains("quoted numbers are strings in YAML")
        error.assertMessageContains("minLength: \"12\"")
    }

    @Test
    fun `coerce reports unquoted boolean when string is expected`() {
        val node = ParserNodeFixtures.scalar(
            value = true,
            sourceLine = "version: true",
        )
        val error = assertFailsWith<AsyncApiParseException.UnexpectedValue> {
            node.coerce<String>()
        }
        error.assertMessageContains("expected String")
        error.assertMessageContains("found Boolean true")
        error.assertMessageContains("quote the value")
        error.assertMessageContains("version: true")
    }
}
