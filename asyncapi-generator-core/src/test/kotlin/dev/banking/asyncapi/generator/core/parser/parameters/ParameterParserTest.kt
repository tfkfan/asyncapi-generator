package dev.banking.asyncapi.generator.core.parser.parameters

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.ParserTestSupport
import org.junit.jupiter.api.Test

class ParameterParserTest : ParserTestSupport() {

    private val parser = ParameterParser(asyncApiContext)

    @Test
    fun `parse parameter with invalid structure throws UnexpectedValue`() {
        val parametersNode = readNode(
            "parser/parameters/asyncapi_parser_parameter_invalid.yaml",
            "components",
            "parameterCases",
            "InvalidParameterStructure",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected Map",
            "asyncapi_parser_parameter_invalid.yaml",
            "asyncapi_parser_parameter_invalid.root.components.parameterCases.InvalidParameterStructure.badParameter",
        ) {
            parser.parseMap(parametersNode)
        }
    }

    @Test
    fun `parse parameter with boolean location throws UnexpectedValue`() {
        val parametersNode = readNode(
            "parser/parameters/asyncapi_parser_parameter_invalid.yaml",
            "components",
            "parameterCases",
            "BooleanLocation",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected String",
            "found Boolean true",
            "quote the value",
            "asyncapi_parser_parameter_invalid.yaml",
            "asyncapi_parser_parameter_invalid.root.components.parameterCases.BooleanLocation.badParameter.location",
        ) {
            parser.parseMap(parametersNode)
        }
    }
}
