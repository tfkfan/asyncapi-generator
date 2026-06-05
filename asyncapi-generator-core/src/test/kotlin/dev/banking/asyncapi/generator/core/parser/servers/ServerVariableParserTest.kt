package dev.banking.asyncapi.generator.core.parser.servers

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.ParserTestSupport
import org.junit.jupiter.api.Test

class ServerVariableParserTest : ParserTestSupport() {

    private val parser = ServerVariableParser(asyncApiContext)

    @Test
    fun `parse server variable with invalid structure throws UnexpectedValue`() {
        val variablesNode = readNode(
            "parser/servers/asyncapi_parser_server_variable_invalid.yaml",
            "components",
            "serverVariableCases",
            "InvalidVariableStructure",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected Map",
            "asyncapi_parser_server_variable_invalid.yaml",
            "asyncapi_parser_server_variable_invalid.root.components.serverVariableCases.InvalidVariableStructure.badVariable",
        ) {
            parser.parseMap(variablesNode)
        }
    }

    @Test
    fun `parse server variable with boolean default throws UnexpectedValue`() {
        val variablesNode = readNode(
            "parser/servers/asyncapi_parser_server_variable_invalid.yaml",
            "components",
            "serverVariableCases",
            "BooleanDefault",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected String",
            "found Boolean false",
            "quote the value",
            "asyncapi_parser_server_variable_invalid.yaml",
            "asyncapi_parser_server_variable_invalid.root.components.serverVariableCases.BooleanDefault.badVariable.default",
        ) {
            parser.parseMap(variablesNode)
        }
    }
}
