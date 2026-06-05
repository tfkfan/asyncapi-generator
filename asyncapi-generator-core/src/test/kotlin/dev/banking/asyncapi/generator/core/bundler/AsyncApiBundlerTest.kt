package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.fixtures.BundlerFixtures
import dev.banking.asyncapi.generator.core.fixtures.TestResources
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import dev.banking.asyncapi.generator.core.model.channels.Channel
import dev.banking.asyncapi.generator.core.model.channels.ChannelInterface
import dev.banking.asyncapi.generator.core.model.messages.MessageInterface
import dev.banking.asyncapi.generator.core.model.schemas.SchemaInterface
import dev.banking.asyncapi.generator.core.registry.AsyncApiRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertNotNull

class AsyncApiBundlerTest {

    private val asyncApiContext = AsyncApiContext()
    private val bundler = AsyncApiBundler()
    private val bundlerFixtures = BundlerFixtures(asyncApiContext)

    @Test
    fun asyncApiSingleFile() {
        val file = TestResources.file("asyncapi_kafka_single_file_example.yaml")
        val parsed = bundlerFixtures.validatedDocument(file)
        val result = bundler.bundle(parsed)
        val expected = expectedSingleFileBundled(file)
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*sourceId", ".*inline")
            .isEqualTo(expected)
    }

    @Test
    fun asyncApiMultiFile() {
        val bundled = bundlerFixtures.bundledDocument("bundler/multi/asyncapi_multifile_example_main.yaml")
        AsyncApiRegistry.writeYaml(File("src/test/resources/bundler/bundled/asyncapi-bundled.yaml"), bundled)
    }

    @Test
    fun asyncApiMultiFileAssertions() {
        val result = bundlerFixtures.bundledDocument("bundler/multi/asyncapi_multifile_example_main.yaml")
        val expected = expectedMultiFileBundled()
        assertThat(result)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*sourceId", ".*inline")
            .isEqualTo(expected)
    }

    @Test
    fun `bundling circular references should not cause stack overflow`() {
        val bundled = bundlerFixtures.bundledDocument("bundler/circular/asyncapi_bundler_circular.yaml")
        assertNotNull(bundled, "Bundled document should not be null")
    }

    @Test
    fun `bundling marks references as inline`() {
        val bundled = bundlerFixtures.bundledDocument("bundler/multi/asyncapi_multifile_example_main.yaml")

        val channelRef = bundled.channels!!["testChannel"] as ChannelInterface.ChannelReference
        val bundledChannel = channelRef.reference.model as Channel
        val messageInline = bundledChannel.messages!!["testMessage"] as MessageInterface.MessageInline

        assertThat(channelRef.reference.inline).isTrue()
        assertThat(channelRef.reference.model).isNotNull
        assertThat(messageInline.message.payload).isInstanceOf(SchemaInterface.SchemaInline::class.java)
    }

    private fun expectedSingleFileBundled(file: File): AsyncApiDocument {
        return bundlerFixtures.validatedDocument(file)
    }
}
