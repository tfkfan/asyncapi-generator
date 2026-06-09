package dev.banking.asyncapi.generator.core.generator.model

/**
 * Target source language selected by user-facing generator configuration.
 *
 * Expected behavior is covered by:
 * - `GeneratorNameTest`
 */
enum class GeneratorName(
    val configurationValue: String,
) {
    KOTLIN("kotlin"),
    JAVA("java"),
    ;

    companion object {
        val supportedConfigurationValues: List<String> = entries.map { it.configurationValue }

        fun fromConfigurationValue(
            value: String?,
            path: String,
        ): GeneratorName {
            if (value == null) {
                return KOTLIN
            }

            return entries.firstOrNull { it.configurationValue == value }
                ?: throw IllegalArgumentException(
                    "Invalid $path '$value'. Supported values: ${supportedConfigurationValues.joinToString(", ")}",
                )
        }
    }
}
