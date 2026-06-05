package dev.banking.asyncapi.generator.core.parser.references

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.ParserTestSupport
import org.junit.jupiter.api.Test

class ReferenceParserTest : ParserTestSupport() {

    private val parser = ReferenceParser(asyncApiContext)

    @Test
    fun `parse reference missing ref throws Mandatory`() {
        val referenceNode = readNode(
            "parser/references/asyncapi_parser_reference_invalid.yaml",
            "components",
            "references",
            "MissingReference",
        )
        assertParseFailure<AsyncApiParseException.Mandatory>(
            "Missing mandatory '\$ref'",
            "asyncapi_parser_reference_invalid.yaml",
            "asyncapi_parser_reference_invalid.root.components.references.MissingReference.\$ref",
        ) {
            parser.parseElement(referenceNode)
        }
    }

    @Test
    fun `parse reference with non-string ref throws UnexpectedValue`() {
        val referenceNode = readNode(
            "parser/references/asyncapi_parser_reference_invalid.yaml",
            "components",
            "references",
            "NumericReference",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected String",
            "12345",
            "quote the value",
            "asyncapi_parser_reference_invalid.yaml",
            "asyncapi_parser_reference_invalid.root.components.references.NumericReference.\$ref",
        ) {
            parser.parseElement(referenceNode)
        }
    }

    @Test
    fun `parse reference list missing ref throws Mandatory with indexed path`() {
        val referencesNode = readNode(
            "parser/references/asyncapi_parser_reference_invalid.yaml",
            "components",
            "references",
            "ReferenceList",
        )
        assertParseFailure<AsyncApiParseException.Mandatory>(
            "Missing mandatory '\$ref'",
            "asyncapi_parser_reference_invalid.yaml",
            "asyncapi_parser_reference_invalid.root.components.references.ReferenceList[0].\$ref",
        ) {
            parser.parseList(referencesNode)
        }
    }
}
