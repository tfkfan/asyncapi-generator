package dev.banking.asyncapi.generator.core.bundler

import dev.banking.asyncapi.generator.core.bundler.channels.ChannelBundler
import dev.banking.asyncapi.generator.core.bundler.components.ComponentBundler
import dev.banking.asyncapi.generator.core.bundler.info.InfoBundler
import dev.banking.asyncapi.generator.core.bundler.operations.OperationBundler
import dev.banking.asyncapi.generator.core.bundler.servers.ServerBundler
import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument

/**
 * Bundles a parsed and validated [AsyncApiDocument].
 *
 * The bundler stage resolves already-registered references into the model shape
 * expected by generator stages. It does not read files, parse YAML or JSON,
 * validate AsyncAPI semantics, or generate code.
 *
 * Expected behavior is covered by:
 * - `AsyncApiBundlerContractTest`
 * - `AsyncApiBundlerTest`
 */
class AsyncApiBundler : BundlingStage {

    private val infoBundler = InfoBundler()
    private val serverBundler = ServerBundler()
    private val channelBundler = ChannelBundler()
    private val operationBundler = OperationBundler()
    private val componentBundler = ComponentBundler()

    override fun bundle(document: AsyncApiDocument): AsyncApiDocument {
        val context = BundlingContext.empty()
        return document.copy(
            info = infoBundler.bundle(document.info, context),
            servers = serverBundler.bundleServers(document.servers, context),
            channels = channelBundler.bundleMap(document.channels, context),
            operations = operationBundler.bundleMap(document.operations, context),
            components = componentBundler.bundleComponents(document.components, context),
        )
    }
}
