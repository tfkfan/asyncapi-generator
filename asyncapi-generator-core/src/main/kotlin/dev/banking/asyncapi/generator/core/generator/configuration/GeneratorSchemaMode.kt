package dev.banking.asyncapi.generator.core.generator.configuration

/**
 * Public schema generation mode selected by CLI, Maven, or Gradle configuration.
 *
 * Expected behavior is covered by:
 * - `GeneratorSchemaModeTest`
 */
enum class GeneratorSchemaMode(
    val configValue: String,
) {
    NONE("none"),
    AVRO_PROJECTION("avro-projection");

    companion object {
        fun fromConfigValue(value: String?): GeneratorSchemaMode? =
            value?.let { candidate ->
                entries.firstOrNull { it.configValue == candidate }
                    ?: throw IllegalArgumentException(
                        "Invalid schemaMode '$candidate'. Supported values: ${supportedValues()}",
                    )
            }

        fun supportedValues(): String =
            entries.joinToString { it.configValue }
    }
}
