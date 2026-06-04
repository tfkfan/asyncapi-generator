@file:Suppress("UNCHECKED_CAST")

package dev.banking.asyncapi.generator.core.registry

import dev.banking.asyncapi.generator.core.model.exceptions.AsyncApiParseException
import dev.banking.asyncapi.generator.core.parser.node.ParsedYamlData
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.NodeId
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import org.yaml.snakeyaml.nodes.Tag

/**
 * Parses YAML input into the map structure consumed by the AsyncAPI parser.
 *
 * Scalar values are interpreted as semantic YAML values. Presentation details
 * such as quote style and block-scalar style are not included in parsed strings.
 *
 * Expected behavior is covered by:
 * - `YamlParserRegistryTest`
 * - parser package tests
 */
object YamlParserRegistry {

    private val yaml = Yaml(LoaderOptions().apply { isProcessComments = true })

    fun parse(fileName: String, content: String, rootPath: String): ParsedYamlData {
        val rootNode = yaml.compose(content.reader())
            ?: throw AsyncApiParseException.EmptyYamlFile(fileName)
        val lineMappings = mutableMapOf<String, Int>()
        val result = parseNode(rootNode, rootPath, lineMappings)
        return ParsedYamlData(result as Map<String, Any?>, lineMappings)
    }

    private fun parseNode(node: Node, path: String, lineMappings: MutableMap<String, Int>): Any? =
        when (node.nodeId) {
            NodeId.scalar -> parseScalar(node as ScalarNode)
            NodeId.sequence -> parseSequence(node as SequenceNode, path, lineMappings)
            NodeId.mapping -> parseMapping(node as MappingNode, path, lineMappings)
            else -> null
        }

    private fun parseSequence(node: SequenceNode, path: String, lineMappings: MutableMap<String, Int>): List<Any?> =
        node.value.mapIndexed { index, child ->
            parseNode(child, "$path.$index", lineMappings)
        }

    private fun parseMapping(node: MappingNode, path: String, lineMappings: MutableMap<String, Int>): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        node.value.forEach { tuple ->
            val key = (tuple.keyNode as ScalarNode).value
            val keyPath = "$path.$key"
            lineMappings[keyPath] = tuple.keyNode.startMark.line + 1
            result[key] = parseNode(tuple.valueNode, keyPath, lineMappings)
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
}
