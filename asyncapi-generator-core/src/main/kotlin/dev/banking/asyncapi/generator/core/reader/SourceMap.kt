package dev.banking.asyncapi.generator.core.reader

/**
 * Source locations indexed by reader-stage document paths.
 *
 * Paths use dot notation for object fields and bracket notation for array
 * indexes, for example `root.components.schemas.User.required[0]`.
 *
 * Expected behavior is covered by:
 * - `SourceMapTest`
 */
class SourceMap(
    locationsByPath: Map<String, SourceLocation>,
) {
    private val locations = locationsByPath.toMap()

    operator fun get(path: String): SourceLocation? = locations[path]

    fun locationOf(path: String): SourceLocation? = locations[path]

    fun all(): Map<String, SourceLocation> = locations
}
