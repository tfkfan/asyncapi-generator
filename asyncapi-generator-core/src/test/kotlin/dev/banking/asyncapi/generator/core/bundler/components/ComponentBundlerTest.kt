package dev.banking.asyncapi.generator.core.bundler.components

import dev.banking.asyncapi.generator.core.bundler.BundlingContext
import dev.banking.asyncapi.generator.core.model.components.Component
import dev.banking.asyncapi.generator.core.model.components.ComponentInterface
import dev.banking.asyncapi.generator.core.model.references.Reference
import dev.banking.asyncapi.generator.core.model.schemas.Schema
import dev.banking.asyncapi.generator.core.model.schemas.SchemaInterface
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ComponentBundlerTest {

    private val bundler = ComponentBundler()

    @Test
    fun `bundleComponents bundles and inlines an unvisited component reference`() {
        val schemaReference = Reference("#/components/schemas/User", model = Schema(type = "object"))
        val component = Component(
            schemas = mapOf("User" to SchemaInterface.SchemaReference(schemaReference)),
        )
        val componentReference = Reference("#/components", model = component)
        val componentInterface = ComponentInterface.ComponentReference(componentReference)

        val bundled = bundler.bundleComponents(componentInterface, BundlingContext.empty())

        assertThat(bundled).isSameAs(componentInterface)
        assertThat(componentReference.inline).isTrue()
        assertThat(componentReference.model).isInstanceOf(Component::class.java)
        assertThat(schemaReference.inline).isTrue()
    }

    @Test
    fun `bundleComponents keeps a visited component reference unchanged`() {
        val component = Component()
        val componentReference = Reference("#/components", model = component)
        val componentInterface = ComponentInterface.ComponentReference(componentReference)

        val bundled = bundler.bundleComponents(componentInterface, BundlingContext.empty().enter(componentReference))

        assertThat(bundled).isSameAs(componentInterface)
        assertThat(componentReference.inline).isFalse()
        assertThat(componentReference.model).isSameAs(component)
    }
}
