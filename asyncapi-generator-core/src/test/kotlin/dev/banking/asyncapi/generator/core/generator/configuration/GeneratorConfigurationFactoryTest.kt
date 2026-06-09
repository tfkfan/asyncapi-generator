package dev.banking.asyncapi.generator.core.generator.configuration

import dev.banking.asyncapi.generator.core.generator.model.GeneratorName
import dev.banking.asyncapi.generator.core.generator.plan.SpringKafkaClientType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratorConfigurationFactoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `create enables model generation when model package is configured`() {
        val configuration =
            GeneratorConfigurationFactory.create(
                request(
                    modelPackageName = "com.example.model",
                    modelAnnotation = "com.example.NoArg",
                ),
            )

        assertEquals(
            ModelGeneration.Enabled(
                packageName = "com.example.model",
                annotation = "com.example.NoArg",
            ),
            configuration.models,
        )
        assertTrue(configuration.hasConfiguredOutputs())
    }

    @Test
    fun `create enables Spring Kafka client generation when client type is configured`() {
        val configuration =
            GeneratorConfigurationFactory.create(
                request(
                    modelPackageName = "com.example.model",
                    clientPackageName = "com.example.client",
                    clientType = GeneratorClientType.SPRING_KAFKA_SIMPLE,
                    kafkaTopicsPropertyPrefix = "custom.topics",
                ),
            )

        assertEquals(
            listOf(
                ClientGeneration.SpringKafka(
                    packageName = "com.example.client",
                    modelPackageName = "com.example.model",
                    clientType = SpringKafkaClientType.SIMPLE,
                    topicPropertyPrefix = "custom.topics",
                ),
            ),
            configuration.clients,
        )
    }

    @Test
    fun `create enables Avro projection when schema mode is configured`() {
        val configuration =
            GeneratorConfigurationFactory.create(
                request(
                    schemaPackageName = "com.example.schema",
                    schemaMode = GeneratorSchemaMode.AVRO_PROJECTION,
                ),
            )

        assertEquals(
            listOf(SchemaGeneration.AvroProjection(packageName = "com.example.schema")),
            configuration.schemas,
        )
    }

    @Test
    fun `create treats explicit none values as disabled output`() {
        val configuration =
            GeneratorConfigurationFactory.create(
                request(
                    clientPackageName = "com.example.client",
                    schemaPackageName = "com.example.schema",
                    clientType = GeneratorClientType.NONE,
                    schemaMode = GeneratorSchemaMode.NONE,
                ),
            )

        assertEquals(emptyList(), configuration.clients)
        assertEquals(emptyList(), configuration.schemas)
        assertFalse(configuration.hasConfiguredOutputs())
    }

    @Test
    fun `create rejects client type without client package`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorConfigurationFactory.create(
                    request(clientType = GeneratorClientType.SPRING_KAFKA),
                )
            }

        assertEquals("clientType requires clientPackage", exception.message)
    }

    @Test
    fun `create rejects schema mode without schema package`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorConfigurationFactory.create(
                    request(schemaMode = GeneratorSchemaMode.AVRO_PROJECTION),
                )
            }

        assertEquals("schemaMode requires schemaPackage", exception.message)
    }

    @Test
    fun `create rejects model annotation without model package`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorConfigurationFactory.create(
                    request(modelAnnotation = "com.example.NoArg"),
                )
            }

        assertEquals("modelAnnotation requires modelPackage", exception.message)
    }

    @Test
    fun `create rejects blank Kafka topics property prefix`() {
        val exception =
            assertFailsWith<IllegalArgumentException> {
                GeneratorConfigurationFactory.create(
                    request(kafkaTopicsPropertyPrefix = ""),
                )
            }

        assertEquals("kafkaTopicsPropertyPrefix cannot be empty", exception.message)
    }

    private fun request(
        modelPackageName: String? = null,
        clientPackageName: String? = null,
        schemaPackageName: String? = null,
        clientType: GeneratorClientType? = null,
        schemaMode: GeneratorSchemaMode? = null,
        modelAnnotation: String? = null,
        kafkaTopicsPropertyPrefix: String = GeneratorConfigurationRequest.DEFAULT_KAFKA_TOPICS_PROPERTY_PREFIX,
    ): GeneratorConfigurationRequest =
        GeneratorConfigurationRequest(
            language = GeneratorName.KOTLIN,
            sourceOutputDirectory = tempDir.resolve("sources").toFile(),
            resourceOutputDirectory = tempDir.resolve("resources").toFile(),
            modelPackageName = modelPackageName,
            clientPackageName = clientPackageName,
            schemaPackageName = schemaPackageName,
            clientType = clientType,
            schemaMode = schemaMode,
            modelAnnotation = modelAnnotation,
            kafkaTopicsPropertyPrefix = kafkaTopicsPropertyPrefix,
        )
}
