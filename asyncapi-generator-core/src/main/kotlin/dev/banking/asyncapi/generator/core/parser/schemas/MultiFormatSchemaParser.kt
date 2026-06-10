package dev.banking.asyncapi.generator.core.parser.schemas

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException.UnexpectedSchemaFormat
import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException.UnsupportedSchemaFormat
import dev.banking.asyncapi.generator.core.model.schemas.SchemaFormat

/**
 * [MultiFormatSchemaParser] currently acts as a **validator** for schema format choices.
 *
 * Its primary responsibility is to check the `schemaFormat` string provided in an AsyncAPI document
 * against a predefined list of known formats.
 *
 * **Current State:**
 * - It fully supports and *allows* standard AsyncAPI schema formats (e.g., `application/vnd.aai.asyncapi+yaml`).
 *   When such a format is encountered, it passes validation, and the schema content is then parsed
 *   by the regular [SchemaParser].
 * - For other known formats (e.g., JSON Schema Draft-07, Avro, OpenAPI, RAML, Protobuf),
 *   it *recognizes* the format but **throws a [UnsupportedSchemaFormat]**, indicating that
 *   code generation for these specific formats is not yet implemented in the generator.
 * - For any unknown `schemaFormat` string, it throws an [UnexpectedSchemaFormat] error.
 *
 * **Future Expansion:**
 * This parser is designed to be expanded in the future to provide full parsing and
 * code generation support for the currently unimplemented schema formats.
 *
 * Expected behavior is covered by:
 * - `MultiFormatSchemaParserTest`
 * - `SchemaParserTest`
 */
class MultiFormatSchemaParser(
    val asyncApiContext: AsyncApiContext,
) {

    fun validateFormat(format: String, path: String) {
        val schemaFormat =
            SchemaFormat.fromValue(format)
                ?: throw UnexpectedSchemaFormat(format, path, asyncApiContext)
        if (schemaFormat.isAsyncApiSchemaObject) {
            return
        }
        throw UnsupportedSchemaFormat(format, path, asyncApiContext)
    }
}
