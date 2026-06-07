package dev.banking.asyncapi.generator.core.bundler.schemas

import dev.banking.asyncapi.generator.core.bundler.BundlingContext
import dev.banking.asyncapi.generator.core.model.bindings.BindingInterface
import dev.banking.asyncapi.generator.core.model.references.Reference
import dev.banking.asyncapi.generator.core.model.schemas.Schema
import dev.banking.asyncapi.generator.core.model.schemas.SchemaInterface
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SchemaBundlerTest {

    private val bundler = SchemaBundler()

    @Test
    fun `bundle keeps an unvisited component schema reference and bundles its model`() {
        val bindingReference = Reference("#/components/schemaBindings/kafka")
        val schema = Schema(
            type = "object",
            bindings = mapOf("kafka" to BindingInterface.BindingReference(bindingReference)),
        )
        val schemaReference = Reference("#/components/schemas/User", model = schema)
        val schemaInterface = SchemaInterface.SchemaReference(schemaReference)

        val bundled = bundler.bundle(schemaInterface, BundlingContext.empty())

        assertThat(bundled).isSameAs(schemaInterface)
        assertThat(schemaReference.inline).isTrue()
        assertThat(schemaReference.model).isInstanceOf(Schema::class.java)
        assertThat((schemaReference.model as Schema).bindings).containsKey("kafka")
        assertThat(bindingReference.inline).isTrue()
    }

    @Test
    fun `bundle keeps a visited component schema reference unchanged`() {
        val schema = Schema(type = "object")
        val schemaReference = Reference("#/components/schemas/User", model = schema)
        val schemaInterface = SchemaInterface.SchemaReference(schemaReference)

        val bundled = bundler.bundle(schemaInterface, BundlingContext.empty().enter(schemaReference))

        assertThat(bundled).isSameAs(schemaInterface)
        assertThat(schemaReference.inline).isFalse()
        assertThat(schemaReference.model).isSameAs(schema)
    }

    @Test
    fun `bundle inlines an unvisited non-component schema reference`() {
        val bindingReference = Reference("#/components/schemaBindings/kafka")
        val schema = Schema(
            type = "object",
            bindings = mapOf("kafka" to BindingInterface.BindingReference(bindingReference)),
        )
        val schemaReference = Reference("schemas.yaml#/shared/User", model = schema)
        val schemaInterface = SchemaInterface.SchemaReference(schemaReference)

        val bundled = bundler.bundle(schemaInterface, BundlingContext.empty())

        assertThat(bundled).isInstanceOf(SchemaInterface.SchemaInline::class.java)
        assertThat((bundled as SchemaInterface.SchemaInline).schema.bindings).containsKey("kafka")
        assertThat(schemaReference.inline).isFalse()
        assertThat(bindingReference.inline).isTrue()
    }

    @Test
    fun `bundle returns the model for a visited non-component schema reference`() {
        val schema = Schema(type = "object")
        val schemaReference = Reference("schemas.yaml#/shared/User", model = schema)
        val schemaInterface = SchemaInterface.SchemaReference(schemaReference)

        val bundled = bundler.bundle(schemaInterface, BundlingContext.empty().enter(schemaReference))

        assertThat(bundled).isEqualTo(SchemaInterface.SchemaInline(schema))
        assertThat(schemaReference.inline).isFalse()
        assertThat(schemaReference.model).isSameAs(schema)
    }
}
