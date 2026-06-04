package dev.banking.asyncapi.generator.core.registry

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class YamlParserRegistryTest {

    @Test
    fun `parses yaml scalars as semantic values without style markers`() {
        val document = YamlParserRegistry.parse(
            fileName = "semantic-scalars.yaml",
            content = """
                plain: plain value
                singleQuoted: 'quoted value'
                doubleQuoted: "quoted value"
                literal: |
                  literal
                  text
                folded: >
                  folded
                  text
                plainBoolean: true
                quotedBoolean: "true"
                yaml11BooleanWord: yes
                plainNumber: 12
                quotedNumber: "12"
                ref:
                  ${'$'}ref: '#/components/schemas/User'
            """.trimIndent(),
            rootPath = "semantic.root",
        ).data

        assertEquals("plain value", document["plain"])
        assertEquals("quoted value", document["singleQuoted"])
        assertEquals("quoted value", document["doubleQuoted"])
        assertEquals("literal\ntext\n", document["literal"])
        assertEquals("folded text\n", document["folded"])
        assertEquals(true, document["plainBoolean"])
        assertEquals("true", document["quotedBoolean"])
        assertEquals("yes", document["yaml11BooleanWord"])
        assertEquals(12, document["plainNumber"])
        assertEquals("12", document["quotedNumber"])

        val reference = assertIs<Map<*, *>>(document["ref"])
        assertEquals("#/components/schemas/User", reference["${'$'}ref"])
    }
}
