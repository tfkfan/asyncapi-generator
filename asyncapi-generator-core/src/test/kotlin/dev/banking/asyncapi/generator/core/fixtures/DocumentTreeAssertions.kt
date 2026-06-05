package dev.banking.asyncapi.generator.core.fixtures

import kotlin.test.fail

/**
 * Assertion helper for traversing reader-stage document trees in tests.
 *
 * This keeps tests focused on behavior instead of unchecked casts and provides
 * a domain-oriented failure message when a fixture no longer has the expected
 * object shape.
 */
internal fun Map<String, Any?>.childObject(key: String): Map<String, Any?> {
    val value = this[key]
    if (value !is Map<*, *>) {
        fail("Expected '$key' to be an object, but was ${value?.javaClass?.simpleName ?: "null"}")
    }

    @Suppress("UNCHECKED_CAST")
    return value as Map<String, Any?>
}
