package dev.banking.asyncapi.generator.core.parser.node

import dev.banking.asyncapi.generator.core.constants.AsyncApiConstants.ROOT
import dev.banking.asyncapi.generator.core.context.AsyncApiContext
import dev.banking.asyncapi.generator.core.reader.InputDocument
import java.io.File

/**
 * Creates parser nodes from reader-stage input documents.
 *
 * This is the adapter between the reader stage and parser stage. It preserves
 * the existing parser path convention while registering reader-provided source
 * locations in the parser context.
 *
 * Expected behavior is covered by:
 * - `ParserNodeFactoryTest`
 */
object ParserNodeFactory {

    fun root(
        document: InputDocument,
        context: AsyncApiContext,
    ): ParserNode {
        context.registerSource(document.source.file, document.source.content)

        val rootPath = "${buildFileId(document.source.file)}.$ROOT"
        document.sourceMap.all().values.forEach { location ->
            val parserPath = parserPath(rootPath, location.path)
            context.registerSourceLocation(parserPath, location)

            val normalizedPath = normalizeArrayPath(parserPath)
            if (normalizedPath != parserPath) {
                context.registerSourceLocation(normalizedPath, location)
            }
        }

        return ParserNode(
            name = rootPath,
            node = document.root,
            path = rootPath,
            context = context,
        )
    }

    private fun parserPath(
        rootPath: String,
        readerPath: String,
    ): String =
        when {
            readerPath == READER_ROOT_PATH -> rootPath
            readerPath.startsWith("$READER_ROOT_PATH.") || readerPath.startsWith("$READER_ROOT_PATH[") ->
                rootPath + readerPath.removePrefix(READER_ROOT_PATH)
            else -> "$rootPath.$readerPath"
        }

    private fun normalizeArrayPath(path: String): String =
        path.replace(Regex("""\[(\d+)]"""), ".$1")

    private fun buildFileId(file: File): String =
        file.nameWithoutExtension
            .replace(Regex("[^A-Za-z0-9_]"), "_")

    private const val READER_ROOT_PATH = "root"
}
