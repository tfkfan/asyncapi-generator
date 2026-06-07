package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.references.Reference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReferenceBundlerTest {

    @Test
    fun `inlineIfUnvisited marks an unvisited reference as inline`() {
        val reference = Reference("#/components/externalDocs/api")

        ReferenceBundler.inlineIfUnvisited(reference, BundlingContext.empty())

        assertThat(reference.inline).isTrue()
    }

    @Test
    fun `inlineIfUnvisited keeps a visited reference unchanged`() {
        val reference = Reference("#/components/externalDocs/api")

        ReferenceBundler.inlineIfUnvisited(reference, BundlingContext.empty().enter(reference))

        assertThat(reference.inline).isFalse()
    }
}
