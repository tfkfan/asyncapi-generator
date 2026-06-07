package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.references.Reference
import dev.banking.asyncapi.generator.core.model.servers.Server
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

    @Test
    fun `bundleReferencedModel bundles and inlines an unvisited referenced model`() {
        val reference = Reference(
            ref = "#/components/servers/production",
            model = Server(host = "kafka.example.com", protocol = "kafka"),
        )

        ReferenceBundler.bundleReferencedModel<Server>(reference, BundlingContext.empty()) { server, context ->
            assertThat(context.hasVisited(reference)).isTrue()
            server.copy(description = "Bundled server")
        }

        assertThat(reference.inline).isTrue()
        assertThat(reference.model)
            .isEqualTo(Server(host = "kafka.example.com", protocol = "kafka", description = "Bundled server"))
    }

    @Test
    fun `bundleReferencedModel keeps a visited referenced model unchanged`() {
        val model = Server(host = "kafka.example.com", protocol = "kafka")
        val reference = Reference(
            ref = "#/components/servers/production",
            model = model,
        )

        ReferenceBundler.bundleReferencedModel<Server>(reference, BundlingContext.empty().enter(reference)) { server, _ ->
            server.copy(description = "Should not be applied")
        }

        assertThat(reference.inline).isFalse()
        assertThat(reference.model).isSameAs(model)
    }
}
