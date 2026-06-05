package dev.banking.asyncapi.generator.core.parser.operations

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.ParserTestSupport
import org.junit.jupiter.api.Test

class OperationReplyAddressParserTest : ParserTestSupport() {

    private val parser = OperationReplyAddressParser(asyncApiContext)

    @Test
    fun `parse operation reply address missing location throws Mandatory`() {
        val addressNode = readNode(
            "parser/operations/asyncapi_parser_operation_reply_address_invalid.yaml",
            "components",
            "operationReplyAddressCases",
            "MissingLocation",
        )
        assertParseFailure<AsyncApiParseException.Mandatory>(
            "Missing mandatory 'location'",
            "asyncapi_parser_operation_reply_address_invalid.yaml",
            "asyncapi_parser_operation_reply_address_invalid.root.components.operationReplyAddressCases.MissingLocation.location",
        ) {
            parser.parseElement(addressNode)
        }
    }

    @Test
    fun `parse operation reply address with boolean location throws UnexpectedValue`() {
        val addressNode = readNode(
            "parser/operations/asyncapi_parser_operation_reply_address_invalid.yaml",
            "components",
            "operationReplyAddressCases",
            "BooleanLocation",
        )
        assertParseFailure<AsyncApiParseException.UnexpectedValue>(
            "Unexpected value: expected String",
            "found Boolean true",
            "quote the value",
            "asyncapi_parser_operation_reply_address_invalid.yaml",
            "asyncapi_parser_operation_reply_address_invalid.root.components.operationReplyAddressCases.BooleanLocation.location",
        ) {
            parser.parseElement(addressNode)
        }
    }
}
