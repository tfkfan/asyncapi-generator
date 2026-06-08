package dev.banking.asyncapi.generator.core.fixtures

import dev.banking.asyncapi.generator.core.model.asyncapi.AsyncApiDocument
import dev.banking.asyncapi.generator.core.model.channels.Channel
import dev.banking.asyncapi.generator.core.model.channels.ChannelInterface
import dev.banking.asyncapi.generator.core.model.components.Component
import dev.banking.asyncapi.generator.core.model.components.ComponentInterface
import dev.banking.asyncapi.generator.core.model.info.Info
import dev.banking.asyncapi.generator.core.model.messages.Message
import dev.banking.asyncapi.generator.core.model.messages.MessageInterface
import dev.banking.asyncapi.generator.core.model.references.Reference
import dev.banking.asyncapi.generator.core.model.schemas.Schema
import dev.banking.asyncapi.generator.core.model.schemas.SchemaInterface

/**
 * Fixture facade for generation-input tests.
 *
 * It builds focused AsyncAPI model objects for generator preparation scenarios
 * without requiring the tests to repeat the full object graph.
 */
internal class GenerationInputFixtures {
    fun documentWithPolymorphicComponentAndChannel(): AsyncApiDocument {
        val externalSchema =
            Schema(
                type = "object",
                properties =
                    mapOf(
                        "status" to SchemaInterface.SchemaInline(Schema(type = "string", enum = listOf("ACTIVE"))),
                    ),
            )
        val rootSchema =
            Schema(
                oneOf =
                    listOf(
                        SchemaInterface.SchemaReference(
                            Reference(
                                ref = "#/components/schemas/External",
                                model = externalSchema,
                            ),
                        ),
                    ),
            )

        return AsyncApiDocument(
            asyncapi = "3.0.0",
            info = Info(title = "Test API", version = "1.0.0"),
            channels =
                mapOf(
                    "userEvents" to
                        ChannelInterface.ChannelInline(
                            Channel(
                                address = "user.events",
                                messages =
                                    mapOf(
                                        "userCreated" to
                                            MessageInterface.MessageInline(
                                                Message(
                                                    name = "UserCreated",
                                                    payload = SchemaInterface.SchemaInline(Schema(type = "object")),
                                                ),
                                            ),
                                    ),
                            ),
                        ),
                ),
            components =
                ComponentInterface.ComponentInline(
                    Component(
                        schemas =
                            mapOf(
                                "Root" to SchemaInterface.SchemaInline(rootSchema),
                            ),
                    ),
                ),
        )
    }
}
