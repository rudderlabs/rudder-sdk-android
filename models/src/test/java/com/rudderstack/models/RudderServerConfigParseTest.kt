package com.rudderstack.models

import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


private const val rudderServerConfigMockResponse1 = "mock/rudder_config_response.json"
private const val rudderServerConfigMockResponse2 = "mock/rudder_config_response_2.json"

open class RudderServerConfigParseTest {

    @Test
    fun `given a rudder server config response that includes destinations, when the response is parsed, then assert that all destinations have an api key`() {
        // deserialize
        val rta = object : RudderTypeAdapter<RudderServerConfig>() {}
        val res = MockResponse.fromJsonFile(rudderServerConfigMockResponse1, rta)
        assert(res != null)
        assertThat(
            res?.source?.destinations?.get(1),
            allOf(
                notNullValue(),
                isA(RudderServerConfig.RudderServerDestination::class.java),
            ),
        )
        assertThat(
            res?.source?.destinations?.get(1)?.destinationConfig?.get("apiKey"),
            allOf(
                notNullValue(),
                `is`("1234abcd"),
            ),
        )
    }

    @Test
    fun `given a rudder server config response that includes transformations, when the response is parsed, then assert that all destinations have transformations enabled`() {
        // deserialize
        val rta = object : RudderTypeAdapter<RudderServerConfig>() {}
        val res = MockResponse.fromJsonFile(rudderServerConfigMockResponse2, rta)
        assert(res != null)
        assertThat(
            res?.source?.destinations?.get(0),
            allOf(
                notNullValue(),
                isA(RudderServerConfig.RudderServerDestination::class.java),
            ),
        )
        assertThat(
            res?.source?.destinations?.get(0)?.destinationId,
            allOf(
                notNullValue(),
                `is`("20NBa9wa4Zb5ZHkJHO2IEiw8eWl"),
            ),
        )
        assertThat(
            res?.source?.destinations?.get(0)?.areTransformationsConnected,
            allOf(
                notNullValue(),
                `is`(true),
            ),
        )
    }

    @Test
    fun testObjectSerializationDeserialization() {
        val objectToSave = provideRudderServerConfig()

        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos).also { it.writeObject(objectToSave) }
        val savedBytes = baos.toByteArray()
        oos.close()

        val bais = ByteArrayInputStream(savedBytes)
        val ois = ObjectInputStream(bais)
        val savedObject = ois.readObject()
        ois.close()

        assertThat(
            savedObject, allOf(
                isA(RudderServerConfig::class.java),
                hasProperty("hosted", `is`(true)),
                hasProperty(
                    "source", allOf(
                        isA<RudderServerConfig.RudderServerConfigSource>(RudderServerConfig.RudderServerConfigSource::class.java),
                        hasProperty("sourceId", `is`("sourceId")),
                        hasProperty("sourceName", `is`("sourceName")),
                        hasProperty("sourceEnabled", `is`(true)),
                        hasProperty("updatedAt", `is`("2020-02-26T09:17:52.231Z")),
                        hasProperty(
                            "destinations", allOf<List<RudderServerConfig.RudderServerDestination>>(
                                iterableWithSize(1),
                                hasItem<RudderServerConfig.RudderServerDestination>(
                                    allOf(
                                        hasProperty("destinationId", `is`("d_id")),
                                        hasProperty("destinationName", `is`("d_name")),
                                        hasProperty("destinationEnabled", `is`(true)),
                                        hasProperty("areTransformationsConnected", `is`(true)),
                                        hasProperty("updatedAt", `is`("2021-02-26T09:17:52.231Z")),
                                        hasProperty(
                                            "destinationConfig", allOf<Map<String, Any>>(
                                                aMapWithSize(1),
                                                hasEntry("config", "config_v")
                                            )
                                        ),
                                        hasProperty(
                                            "destinationDefinition",
                                            allOf<RudderServerConfig.RudderServerDestinationDefinition>(
                                                isA(RudderServerConfig.RudderServerDestinationDefinition::class.java),
                                                hasProperty("definitionName", `is`("d_d_name")),
                                                hasProperty("displayName", `is`("d_disp_name")),
                                                hasProperty(
                                                    "updatedAt",
                                                    `is`("2022-02-26T09:17:52.231Z")
                                                ),
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}

private fun provideRudderServerConfig() = RudderServerConfig(
    isHosted = true,
    source = RudderServerConfig.RudderServerConfigSource(
        sourceId = "sourceId",
        sourceName = "sourceName",
        isSourceEnabled = true,
        updatedAt = "2020-02-26T09:17:52.231Z",
        destinations = provideServerConfigDestinationList()
    )
)

private fun provideServerConfigDestinationList(): List<RudderServerConfig.RudderServerDestination> {
    return listOf(
        provideServerConfigDestination()
    )
}

private fun provideServerConfigDestination(): RudderServerConfig.RudderServerDestination {
    return RudderServerConfig.RudderServerDestination(
        destinationId = "d_id",
        destinationName = "d_name",
        isDestinationEnabled = true,
        updatedAt = "2021-02-26T09:17:52.231Z",
        destinationDefinition = provideDestinationDefinition(),
        destinationConfig = mapOf("config" to "config_v"),
        areTransformationsConnected = true
    )
}

private fun provideDestinationDefinition(): RudderServerConfig.RudderServerDestinationDefinition {
    return RudderServerConfig.RudderServerDestinationDefinition(
        definitionName = "d_d_name",
        displayName = "d_disp_name",
        updatedAt = "2022-02-26T09:17:52.231Z"
    )
}


