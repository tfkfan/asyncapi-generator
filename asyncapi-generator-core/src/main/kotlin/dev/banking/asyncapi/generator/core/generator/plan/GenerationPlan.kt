package dev.banking.asyncapi.generator.core.generator.plan

/**
 * Ordered generation work selected before rendering or writing artifacts.
 *
 * Expected behavior is covered by:
 * - `GenerationPlannerTest`
 */
data class GenerationPlan(
    val tasks: List<GenerationTask>,
)
