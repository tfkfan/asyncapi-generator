package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.fixtures.BundlerFixtures
import dev.banking.asyncapi.generator.core.fixtures.TestResources
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AsyncApiBundlerContractTest {

    private val bundlerFixtures = BundlerFixtures()
    private val bundlingStage: BundlingStage = AsyncApiBundler()

    @Test
    fun `bundling stage consumes a parsed and validated document`() {
        val document = bundlerFixtures.validatedDocument(TestResources.file("asyncapi_kafka_single_file_example.yaml"))

        val bundled = bundlingStage.bundle(document)

        assertThat(bundled)
            .usingRecursiveComparison()
            .ignoringFieldsMatchingRegexes(".*sourceId", ".*inline")
            .isEqualTo(document)
    }

    @Test
    fun `bundling stage handles an already validated multi-file document`() {
        val document = bundlerFixtures.validatedDocument("bundler/multi/asyncapi_multifile_example_main.yaml")

        val bundled = bundlingStage.bundle(document)

        assertThat(bundled.channels).isNotNull
        assertThat(bundled.components).isNotNull
    }
}
