package dev.banking.asyncapi.generator.core.fixtures

import kotlin.test.fail

internal fun Map<String, Any?>.childObject(key: String): Map<String, Any?> {
    val value = this[key]
    if (value !is Map<*, *>) {
        fail("Expected '$key' to be an object, but was ${value?.javaClass?.simpleName ?: "null"}")
    }

    @Suppress("UNCHECKED_CAST")
    return value as Map<String, Any?>
}
