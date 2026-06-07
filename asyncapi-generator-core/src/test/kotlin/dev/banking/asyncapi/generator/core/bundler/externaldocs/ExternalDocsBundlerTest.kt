package dev.banking.asyncapi.generator.core.bundler.externaldocs

import dev.banking.asyncapi.generator.core.bundler.BundlingContext
import dev.banking.asyncapi.generator.core.model.externaldocs.ExternalDocInterface
import dev.banking.asyncapi.generator.core.model.references.Reference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ExternalDocsBundlerTest {

    private val bundler = ExternalDocsBundler()

    @Test
    fun `bundle marks an unvisited external documentation reference as inline`() {
        val reference = Reference("#/components/externalDocs/api")
        val externalDoc = ExternalDocInterface.ExternalDocReference(reference)

        val bundled = bundler.bundle(externalDoc, BundlingContext.empty())

        assertThat(bundled).isSameAs(externalDoc)
        assertThat(reference.inline).isTrue()
    }

    @Test
    fun `bundle keeps a visited external documentation reference unchanged`() {
        val reference = Reference("#/components/externalDocs/api")
        val externalDoc = ExternalDocInterface.ExternalDocReference(reference)

        val bundled = bundler.bundle(externalDoc, BundlingContext.empty().enter(reference))

        assertThat(bundled).isSameAs(externalDoc)
        assertThat(reference.inline).isFalse()
    }
}
