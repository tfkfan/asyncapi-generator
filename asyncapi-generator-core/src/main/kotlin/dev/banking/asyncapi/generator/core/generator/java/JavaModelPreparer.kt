package dev.banking.asyncapi.generator.core.generator.java

import dev.banking.asyncapi.generator.core.generator.input.GenerationInput
import dev.banking.asyncapi.generator.core.generator.java.factory.JavaGeneratorModelFactory
import dev.banking.asyncapi.generator.core.generator.java.model.GeneratorItem

/**
 * Prepares Java model items from analyzed generator input.
 *
 * Expected behavior is covered by:
 * - `JavaModelPreparerTest`
 */
class JavaModelPreparer {
    fun prepare(
        input: GenerationInput,
        packageName: String,
    ): List<GeneratorItem> {
        val factory =
            JavaGeneratorModelFactory(
                packageName = packageName,
                context = input.schemaContext,
                polymorphicRelationships = input.polymorphicRelationships,
            )

        return input.schemas.mapNotNull { (name, schema) ->
            factory.create(name, schema)
        }
    }
}
