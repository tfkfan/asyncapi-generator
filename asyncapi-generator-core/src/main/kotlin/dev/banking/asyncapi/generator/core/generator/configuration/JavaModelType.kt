package dev.banking.asyncapi.generator.core.generator.configuration

/**
 * Java model shape selected by user-facing model generation configuration.
 *
 * Expected behavior is covered by:
 * - `JavaModelTypeTest`
 */
enum class JavaModelType(
    val configurationValue: String,
) {
    CLASS("class"),
    RECORD("record"),
    ;

    companion object {
        val supportedConfigurationValues: List<String> = entries.map { it.configurationValue }

        fun fromConfigurationValue(
            value: String?,
            path: String,
        ): JavaModelType {
            if (value == null) {
                return CLASS
            }

            return entries.firstOrNull { it.configurationValue == value }
                ?: throw IllegalArgumentException(
                    "Invalid $path '$value'. Supported values: ${supportedConfigurationValues.joinToString(", ")}",
                )
        }
    }
}
