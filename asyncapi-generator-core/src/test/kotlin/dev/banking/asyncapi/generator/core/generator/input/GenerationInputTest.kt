package dev.banking.asyncapi.generator.core.generator.input

import dev.banking.asyncapi.generator.core.model.schemas.Schema
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class GenerationInputTest {

    @Test
    fun `schema context exposes analyzed schemas`() {
        val userSchema = Schema(type = "object")
        val input =
            GenerationInput(
                schemas = mapOf("User" to userSchema),
                polymorphicRelationships = emptyMap(),
                channels = emptyList(),
            )

        assertSame(userSchema, input.schemaContext.findSchemaByName("User"))
    }

    @Test
    fun `schema context with additional schemas includes original and additional schemas`() {
        val userSchema = Schema(type = "object")
        val headerSchema = Schema(type = "object")
        val input =
            GenerationInput(
                schemas = mapOf("User" to userSchema),
                polymorphicRelationships = emptyMap(),
                channels = emptyList(),
            )

        val context = input.schemaContextWith(mapOf("UserHeader" to headerSchema))

        assertSame(userSchema, context.findSchemaByName("User"))
        assertSame(headerSchema, context.findSchemaByName("UserHeader"))
    }
}
