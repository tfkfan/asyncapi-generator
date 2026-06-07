package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.references.Reference
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BundlingContextTest {

    @Test
    fun `empty context has no visited references`() {
        val reference = Reference("#/components/schemas/User")

        val context = BundlingContext.empty()

        assertThat(context.hasVisited(reference)).isFalse()
    }

    @Test
    fun `enter returns a context with the visited reference`() {
        val original = BundlingContext.empty()

        val next = original.enter("#/components/schemas/User")

        assertThat(original.hasVisited("#/components/schemas/User")).isFalse()
        assertThat(next.hasVisited("#/components/schemas/User")).isTrue()
    }
}
