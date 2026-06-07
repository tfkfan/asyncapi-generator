package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.references.Reference

/**
 * Shared reference-bundling behavior used by object-specific bundlers.
 *
 * Object-specific bundlers should decide which fields to traverse. This helper
 * keeps common reference state changes, such as marking an unvisited reference
 * as inline, in one place.
 *
 * Expected behavior is covered by:
 * - `ReferenceBundlerTest`
 */
object ReferenceBundler {

    fun inlineIfUnvisited(
        reference: Reference,
        context: BundlingContext,
    ): Reference {
        if (!context.hasVisited(reference)) {
            reference.inline()
        }
        return reference
    }
}
