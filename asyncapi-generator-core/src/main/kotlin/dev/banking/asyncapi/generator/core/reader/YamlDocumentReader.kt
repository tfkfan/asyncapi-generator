package dev.banking.asyncapi.generator.core.reader

import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.error.MarkedYAMLException
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.NodeId
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Tag
import java.io.File

/**
 * Reads YAML input into an [InputDocument].
 *
 * YAML presentation details such as quote style and block-scalar style must not
 * leak into semantic values. Source locations are recorded for document paths
 * that can be mapped from the YAML node tree.
 *
 * Expected behavior is covered by:
 * - `YamlDocumentReaderTest`
 * - `DocumentReaderContractTest`
 * - `SourceMapTest`
 */
class YamlDocumentReader : DocumentReader {
    private val yaml =
        Yaml(
            LoaderOptions().apply {
                isProcessComments = true
                isAllowDuplicateKeys = false
            },
        )

    override fun read(source: DocumentSource): InputDocument {
        if (source.content.isBlank()) {
            throw DocumentReadException.EmptyDocument(source.file)
        }

        val rootNode =
            try {
                yaml.compose(source.content.reader())
            } catch (ex: MarkedYAMLException) {
                throw DocumentReadException.MalformedDocument(source.file, ex)
            } ?: throw DocumentReadException.EmptyDocument(source.file)

        val locations = linkedMapOf<String, SourceLocation>()
        val rootValue = parseNode(
            node = rootNode,
            path = ROOT_PATH,
            source = source,
            locations = locations,
        )
        @Suppress("UNCHECKED_CAST")
        val root = rootValue as? Map<String, Any?>
            ?: throw DocumentReadException.InvalidRoot(source.file, typeName(rootValue))

        return InputDocument(
            source = source,
            root = root,
            sourceMap = SourceMap(locations),
        )
    }

    private fun parseNode(
        node: Node,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): Any? {
        locations.putIfAbsent(path, locationOf(source, path, node.startMark))
        return when (node.nodeId) {
            NodeId.scalar -> parseScalar(node as ScalarNode)
            NodeId.sequence -> parseSequence(node as SequenceNode, path, source, locations)
            NodeId.mapping -> parseMapping(node as MappingNode, path, source, locations)
            else -> null
        }
    }

    private fun parseSequence(
        node: SequenceNode,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): List<Any?> =
        node.value.mapIndexed { index, child ->
            parseNode(child, "$path[$index]", source, locations)
        }

    private fun parseMapping(
        node: MappingNode,
        path: String,
        source: DocumentSource,
        locations: MutableMap<String, SourceLocation>,
    ): Map<String, Any?> {
        val result = linkedMapOf<String, Any?>()
        node.value.forEach { tuple ->
            val keyNode = tuple.keyNode as? ScalarNode
                ?: throw invalidMappingKey(source.file, tuple.keyNode.startMark)
            val key = keyNode.value
            val keyLocation = locationOf(source, "$path.$key", keyNode.startMark)
            if (result.containsKey(key)) {
                throw DocumentReadException.DuplicateKey(source.file, key, keyLocation.line, keyLocation.column)
            }
            val keyPath = "$path.$key"
            locations[keyPath] = keyLocation
            result[key] = parseNode(tuple.valueNode, keyPath, source, locations)
        }
        return result
    }

    private fun parseScalar(node: ScalarNode): Any? =
        when (node.tag) {
            Tag.NULL -> null
            Tag.BOOL -> parseBoolean(node.value)
            Tag.INT -> parseInteger(node.value) ?: node.value
            Tag.FLOAT -> parseFloat(node.value) ?: node.value
            else -> node.value
        }

    private fun parseBoolean(value: String): Any =
        when (value.lowercase()) {
            "true" -> true
            "false" -> false
            else -> value
        }

    private fun parseInteger(value: String): Number? {
        val normalized = value.replace("_", "")
        return normalized.toLongOrNull()?.let {
            if (it in Int.MIN_VALUE..Int.MAX_VALUE) it.toInt() else it
        }
    }

    private fun parseFloat(value: String): Double? =
        value.replace("_", "").toDoubleOrNull()

    private fun locationOf(
        source: DocumentSource,
        path: String,
        mark: Mark,
    ): SourceLocation =
        SourceLocation.from(
            source = source,
            path = path,
            line = mark.line + 1,
            column = mark.column + 1,
        )

    private fun invalidMappingKey(
        file: File,
        mark: Mark,
    ): DocumentReadException.InvalidMappingKey =
        DocumentReadException.InvalidMappingKey(
            file = file,
            line = mark.line + 1,
            column = mark.column + 1,
        )

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
