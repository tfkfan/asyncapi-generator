package dev.banking.asyncapi.generator.core.generator.configuration

/**
 * Typed schema artifact generation configuration.
 *
 * `AvroProjection` represents AsyncAPI Schema Object to `.avsc` projection.
 * Native Avro payload generation will be modeled separately when Multi Format
 * Schema support is implemented.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
sealed interface SchemaGeneration {
    data class AvroProjection(
        val packageName: String,
    ) : SchemaGeneration
}
