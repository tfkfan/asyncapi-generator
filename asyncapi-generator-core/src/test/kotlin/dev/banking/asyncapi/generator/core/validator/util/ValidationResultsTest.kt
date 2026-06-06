package dev.banking.asyncapi.generator.core.validator.util

import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.ERROR
import dev.banking.asyncapi.generator.core.model.validator.ValidationSeverity.WARNING
import dev.banking.asyncapi.generator.core.reader.SourceLocation
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ValidationResultsTest {

    @Test
    fun `records error finding while preserving error list`() {
        val results = ValidationResults(AsyncApiContext())

        results.error(
            message = "The 'asyncapi' field is required.",
            line = 12,
            doc = "https://example.com/asyncapi",
            path = "streetlights.root.asyncapi",
        )

        val finding = results.findings.single()
        assertEquals(ERROR, finding.severity)
        assertEquals("The 'asyncapi' field is required.", finding.message)
        assertEquals(12, finding.line)
        assertEquals("https://example.com/asyncapi", finding.doc)
        assertNull(finding.sourceLocation)
        assertEquals("streetlights.root.asyncapi", finding.path)

        val error = results.errors.single()
        assertEquals(finding.message, error.message)
        assertEquals(finding.line, error.line)
        assertEquals(finding.doc, error.doc)
    }

    @Test
    fun `records warning finding with source location while preserving warning list`() {
        val results = ValidationResults(AsyncApiContext())
        val sourceLocation = SourceLocation(
            sourceId = "streetlights",
            file = File("streetlights.yaml"),
            path = "streetlights.root.info.id",
            line = 8,
            column = 5,
        )

        results.warn(
            message = "It is recommended to use a URN.",
            doc = "https://example.com/urn",
            sourceLocation = sourceLocation,
        )

        val finding = results.findings.single()
        assertEquals(WARNING, finding.severity)
        assertEquals("It is recommended to use a URN.", finding.message)
        assertEquals(sourceLocation, finding.sourceLocation)
        assertEquals("streetlights.root.info.id", finding.path)
        assertEquals(8, finding.line)
        assertEquals("https://example.com/urn", finding.doc)

        val warning = results.warnings.single()
        assertEquals(finding.message, warning.message)
        assertEquals(finding.line, warning.line)
        assertEquals(finding.doc, warning.doc)
    }

    @Test
    fun `keeps findings in validation order`() {
        val results = ValidationResults(AsyncApiContext())

        results.warn("First warning", line = 2)
        results.error("Second error", line = 3)

        assertEquals(listOf(WARNING, ERROR), results.findings.map { it.severity })
        assertEquals(listOf("First warning", "Second error"), results.findings.map { it.message })
    }
}
