package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument

/**
 * Contract for bundler-stage implementations.
 *
 * A bundling stage receives an already parsed and validated AsyncAPI model and
 * returns a bundled AsyncAPI model. It must not read input files, parse
 * documents, validate AsyncAPI rules, or generate output.
 *
 * Expected behavior is covered by:
 * - `AsyncApiBundlerContractTest`
 * - `AsyncApiBundlerTest`
 */
interface BundlingStage {
    fun bundle(document: AsyncApiDocument): AsyncApiDocument
}
