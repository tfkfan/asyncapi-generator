package dev.banking.asyncapi.generator.core.generator.avro

import com.fasterxml.jackson.databind.ObjectMapper
import dev.banking.asyncapi.generator.core.generator.output.GeneratedArtifact
import dev.banking.asyncapi.generator.core.generator.output.GeneratedArtifactKind
import dev.banking.asyncapi.generator.core.generator.output.GeneratedArtifactPaths
import dev.banking.asyncapi.generator.core.generator.output.GenerationResult
import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiGeneratorException.InvalidNativeAvroSchema
import dev.banking.asyncapi.generator.core.model.schemas.MultiFormatSchema
import org.apache.avro.Schema

/**
 * Renders native Avro `schemaFormat` payloads into `.avsc` schema artifacts.
 *
 * Expected behavior is covered by:
 * - `NativeAvroGeneratorTest`
 */
class NativeAvroGenerator(
    private val objectMapper: ObjectMapper = ObjectMapper(),
) {
    fun render(schemas: Map<String, MultiFormatSchema>): GenerationResult =
        GenerationResult(
            schemas
                .filter { (_, schema) -> schema.format.isNativeAvro }
                .map { (payloadName, schema) -> renderSchema(payloadName, schema) },
        )

    private fun renderSchema(
        payloadName: String,
        schema: MultiFormatSchema,
    ): GeneratedArtifact {
        val avroSchema = parseSchema(payloadName, schema)
        val fileName = "${artifactName(payloadName, avroSchema)}.avsc"
        val relativePath =
            GeneratedArtifactPaths.fromNamespace(
                namespace = avroSchema.namespace.orEmpty(),
                fileName = fileName,
            )

        return GeneratedArtifact(
            relativePath = relativePath,
            content = prettySchemaJson(avroSchema) + System.lineSeparator(),
            kind = GeneratedArtifactKind.SCHEMA,
        )
    }

    private fun parseSchema(
        payloadName: String,
        schema: MultiFormatSchema,
    ): Schema =
        try {
            Schema.Parser().parse(schemaJson(schema.schema))
        } catch (ex: RuntimeException) {
            throw InvalidNativeAvroSchema(
                payloadName = payloadName,
                schemaFormat = schema.schemaFormat,
                reason = ex.message ?: ex::class.simpleName.orEmpty(),
            )
        }

    private fun schemaJson(schema: Any?): String {
        if (schema == null) {
            throw IllegalArgumentException("Missing Avro schema content")
        }

        if (schema is String && schema.trim().startsWithJsonContainer()) {
            return schema.trim()
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
    }

    private fun prettySchemaJson(schema: Schema): String =
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(objectMapper.readValue(schema.toString(), Any::class.java))

    private fun artifactName(
        payloadName: String,
        schema: Schema,
    ): String =
        when (schema.type) {
            Schema.Type.RECORD,
            Schema.Type.ENUM,
            Schema.Type.FIXED,
            -> schema.name
            else -> payloadName
        }

    private fun String.startsWithJsonContainer(): Boolean =
        startsWith("{") || startsWith("[") || startsWith("\"")
}
