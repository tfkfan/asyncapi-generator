package dev.banking.asyncapi.generator.core.reader

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonLocation
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

/**
 * Reads JSON input into an [InputDocument].
 *
 * JSON input must produce the same document tree shape as equivalent YAML
 * input so later stages remain format-independent.
 *
 * Expected behavior is covered by:
 * - `JsonDocumentReaderTest`
 * - `DocumentReaderContractTest`
 * - `SourceMapTest`
 */
class JsonDocumentReader : DocumentReader {
    private val jsonFactory = JsonFactory()

    override fun read(source: DocumentSource): InputDocument {
        if (source.content.isBlank()) {
            throw DocumentReadException.EmptyDocument(source.file)
        }

        return try {
            jsonFactory.createParser(source.content).use { parser ->
                val locations = linkedMapOf<String, SourceLocation>()
                val rootToken = parser.nextToken()
                    ?: throw DocumentReadException.EmptyDocument(source.file)
                val rootValue = parseNode(parser, rootToken, ROOT_PATH, source, locations)
                @Suppress("UNCHECKED_CAST")
                val root = rootValue as? Map<String, Any?>
                    ?: throw DocumentReadException.InvalidRoot(source.file, typeName(rootValue))
                val trailingToken = parser.nextToken()
                if (trailingToken != null) {
                    throw malformed(source, parser, "Unexpected content after JSON root")
                }
                InputDocument(
                    source = source,
                    root = root,
                    sourceMap = SourceMap(locations),
                )
            }
        } catch (ex: DocumentReadException) {
            throw ex
        } catch (ex: JsonParseException) {
            throw DocumentReadException.MalformedDocument(source.file, ex)
        }
    }

    private fun parseNode(
        parser: JsonParser,
        token: JsonToken,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): Any? {
        locations.putIfAbsent(path, locationOf(source, path, parser.currentTokenLocation()))
        return when (token) {
            JsonToken.START_OBJECT -> parseObject(parser, path, source, locations)
            JsonToken.START_ARRAY -> parseArray(parser, path, source, locations)
            JsonToken.VALUE_STRING -> parser.valueAsString
            JsonToken.VALUE_TRUE -> true
            JsonToken.VALUE_FALSE -> false
            JsonToken.VALUE_NUMBER_INT -> parser.numberValue
            JsonToken.VALUE_NUMBER_FLOAT -> parser.doubleValue
            JsonToken.VALUE_NULL -> null
            else -> throw malformed(source, parser, "Unexpected JSON token: $token")
        }
    }

    private fun parseObject(
        parser: JsonParser,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        while (true) {
            val token = parser.nextToken()
                ?: throw malformed(source, parser, "Unexpected end of JSON object")
            if (token == JsonToken.END_OBJECT) {
                return result
            }
            if (token != JsonToken.FIELD_NAME) {
                throw malformed(source, parser, "Expected JSON field name, found $token")
            }

            val key = parser.currentName()
            val keyPath = "$path.$key"
            val keyLocation = locationOf(source, keyPath, parser.currentTokenLocation())
            if (result.containsKey(key)) {
                throw DocumentReadException.DuplicateKey(
                    file = source.file,
                    key = key,
                    line = keyLocation.line,
                    column = keyLocation.column,
                )
            }

            val valueToken = parser.nextToken()
                ?: throw malformed(source, parser, "Missing value for JSON field '$key'")
            locations[keyPath] = keyLocation
            result[key] = parseNode(parser, valueToken, keyPath, source, locations)
        }
    }

    private fun parseArray(
        parser: JsonParser,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): List<Any?> {
        val result = mutableListOf<Any?>()
        while (true) {
            val token = parser.nextToken()
                ?: throw malformed(source, parser, "Unexpected end of JSON array")
            if (token == JsonToken.END_ARRAY) {
                return result
            }
            result += parseNode(parser, token, "$path[${result.size}]", source, locations)
        }
    }

    private fun locationOf(
        source: DocumentSource,
        path: String,
        location: JsonLocation,
    ): SourceLocation =
        SourceLocation.from(
            source = source,
            path = path,
            line = location.lineNr.coerceAtLeast(1),
            column = location.columnNr.coerceAtLeast(1),
        )

    private fun malformed(
        source: DocumentSource,
        parser: JsonParser,
        message: String,
    ): DocumentReadException.MalformedDocument =
        DocumentReadException.MalformedDocument(source.file, JsonParseException(parser, message))

    private fun typeName(value: Any?): String =
        when (value) {
            null -> "null"
            is Map<*, *> -> "object"
            is List<*> -> "array"
            else -> value::class.simpleName ?: value.javaClass.simpleName
        }

    private companion object {
        const val ROOT_PATH = "root"
    }
}
