package dev.banking.asyncapi.generator.core.fixtures

import kotlin.test.assertTrue

/**
 * Assertion helper for exception-message expectations.
 *
 * Use this when a test needs to verify a user-facing diagnostic while keeping
 * failure output readable.
 */
internal fun Throwable.assertMessageContains(expected: String) {
    assertTrue(
        message.orEmpty().contains(expected),
        "Expected message to contain '$expected', but was:\n$message",
    )
}
