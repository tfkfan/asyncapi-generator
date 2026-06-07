package dev.banking.asyncapi.generator.core.bundler.operations

import dev.banking.asyncapi.generator.core.bundler.BundlingContext
import dev.banking.asyncapi.generator.core.model.bindings.BindingInterface
import dev.banking.asyncapi.generator.core.model.operations.OperationTrait
import dev.banking.asyncapi.generator.core.model.operations.OperationTraitInterface
import dev.banking.asyncapi.generator.core.model.references.Reference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OperationTraitBundlerTest {

    private val bundler = OperationTraitBundler()

    @Test
    fun `bundle bundles and inlines an unvisited operation trait reference`() {
        val bindingReference = Reference("#/components/operationBindings/kafka")
        val trait = OperationTrait(
            bindings = mapOf("kafka" to BindingInterface.BindingReference(bindingReference)),
        )
        val traitReference = Reference("#/components/operationTraits/audit", model = trait)
        val traitInterface = OperationTraitInterface.OperationTraitReference(traitReference)

        val bundled = bundler.bundle(traitInterface, BundlingContext.empty())

        assertThat(bundled).isSameAs(traitInterface)
        assertThat(traitReference.inline).isTrue()
        assertThat(traitReference.model).isInstanceOf(OperationTrait::class.java)
        assertThat((traitReference.model as OperationTrait).bindings).containsKey("kafka")
        assertThat(bindingReference.inline).isTrue()
    }

    @Test
    fun `bundle keeps a visited operation trait reference unchanged`() {
        val trait = OperationTrait(title = "Audit")
        val traitReference = Reference("#/components/operationTraits/audit", model = trait)
        val traitInterface = OperationTraitInterface.OperationTraitReference(traitReference)

        val bundled = bundler.bundle(traitInterface, BundlingContext.empty().enter(traitReference))

        assertThat(bundled).isSameAs(traitInterface)
        assertThat(traitReference.inline).isFalse()
        assertThat(traitReference.model).isSameAs(trait)
    }
}
