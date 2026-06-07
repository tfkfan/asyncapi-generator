package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.model.references.Reference

/**
 * Carries traversal state while the bundler walks an AsyncAPI document.
 *
 * The context keeps reference-visit tracking in one dedicated model instead of
 * passing bare sets through every bundler. This makes circular-reference
 * handling explicit while object-specific bundlers still control their own
 * traversal rules.
 *
 * Expected behavior is covered by:
 * - `BundlingContextTest`
 */
class BundlingContext private constructor(
    private val visitedReferences: Set<String>,
) {

    fun hasVisited(reference: Reference): Boolean = hasVisited(reference.ref)

    fun hasVisited(reference: String): Boolean = reference in visitedReferences

    fun enter(reference: Reference): BundlingContext = enter(reference.ref)

    fun enter(reference: String): BundlingContext =
        BundlingContext(visitedReferences + reference)

    companion object {
        fun empty(): BundlingContext = BundlingContext(emptySet())

        fun from(visitedReferences: Set<String>): BundlingContext =
            BundlingContext(visitedReferences)
    }
}
