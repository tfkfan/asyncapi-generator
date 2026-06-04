package dev.banking.asyncapi.generator.core.fixtures

import java.io.File
import java.nio.file.Paths

internal object TestResources {

    fun file(path: String): File {
        val resourcePath = path.removePrefix(TEST_RESOURCE_PREFIX)
        val resource = requireNotNull(Thread.currentThread().contextClassLoader.getResource(resourcePath)) {
            "Missing test resource: $resourcePath"
        }
        return Paths.get(resource.toURI()).toFile()
    }

    fun text(path: String): String =
        file(path).readText()

    private const val TEST_RESOURCE_PREFIX = "src/test/resources/"
}
