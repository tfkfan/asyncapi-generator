package dev.banking.asyncapi.generator.core.reader

/**
 * Reads an input document into a format-independent `InputDocument`.
 *
 * This stage is responsible for file format parsing and source-location
 * metadata only. It must not parse AsyncAPI model objects, validate AsyncAPI
 * semantics, resolve references, bundle documents, or generate code.
 *
 * Expected behavior is covered by:
 * - `DocumentReaderContractTest`
 * - `YamlDocumentReaderTest`
 * - `JsonDocumentReaderTest`
 */
interface DocumentReader {
    fun read(source: DocumentSource): InputDocument
}
