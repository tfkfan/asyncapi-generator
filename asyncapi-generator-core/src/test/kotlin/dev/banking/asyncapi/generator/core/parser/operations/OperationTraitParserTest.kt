package dev.banking.asyncapi.generator.core.parser.operations

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.ParserTestSupport
import org.junit.jupiter.api.Test

class OperationTraitParserTest : ParserTestSupport() {

    private val parser = OperationTraitParser(asyncApiContext)

    @Test
    fun `parse operation trait with invalid structure throws UnexpectedValue`() {
        val traitsNode = readNode(
            "parser/operations/asyncapi_parser_operation_trait_invalid.yaml",
            "components",
            "operationTraitCases",
            "InvalidTraitStructure",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected Map",
            "asyncapi_parser_operation_trait_invalid.yaml",
            "asyncapi_parser_operation_trait_invalid.root.components.operationTraitCases.InvalidTraitStructure.badTrait",
        ) {
            parser.parseMap(traitsNode)
        }
    }

    @Test
    fun `parse operation trait with boolean title throws UnexpectedValue`() {
        val traitsNode = readNode(
            "parser/operations/asyncapi_parser_operation_trait_invalid.yaml",
            "components",
            "operationTraitCases",
            "BooleanTitle",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected String",
            "found Boolean true",
            "quote the value",
            "asyncapi_parser_operation_trait_invalid.yaml",
            "asyncapi_parser_operation_trait_invalid.root.components.operationTraitCases.BooleanTitle.badTrait.title",
        ) {
            parser.parseMap(traitsNode)
        }
    }
}
