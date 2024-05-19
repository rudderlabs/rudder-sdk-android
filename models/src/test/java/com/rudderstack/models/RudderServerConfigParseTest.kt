/*
 * Creator: Debanjan Chatterjee on 05/11/21, 4:53 PM Last modified: 05/11/21, 4:53 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.rudderstack.models

import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.moshirudderadapter.MoshiAdapter
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

open class RudderServerConfigParseTest {
    protected var jsonAdapter: JsonAdapter = JacksonAdapter()

    // test parsing and back
    companion object {
        // obtained through baseUrl/sourceConfig
        const val testJson = "{\n" +
            "  \"isHosted\": true,\n" +
            "  \"source\": {\n" +
            "    \"config\": {},\n" +
            "    \"id\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "    \"name\": \"TestAndroid\",\n" +
            "    \"writeKey\": \"1xXCubSHWXbpBI2h6EpCjKOsxmQ\",\n" +
            "    \"enabled\": true,\n" +
            "    \"sourceDefinitionId\": \"1QGzOQGVLM35GgtteFH1vYCE0WT\",\n" +
            "    \"createdBy\": \"1xXC9Q9pYMrw0OON9Aw2AFeBsUp\",\n" +
            "    \"workspaceId\": \"1xXCSVsmPjqFKhRlpajnDXkGwYX\",\n" +
            "    \"deleted\": false,\n" +
            "    \"createdAt\": \"2021-09-01T10:31:09.414Z\",\n" +
            "    \"updatedAt\": \"2021-09-01T10:31:09.414Z\",\n" +
            "    \"connections\": [\n" +
            "      {\n" +
            "        \"id\": \"20NBaKc9N7u13PPOVb3CNfozNvo\",\n" +
            "        \"sourceId\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "        \"destinationId\": \"20NBa9wa4Zb5ZHkJHO2IEiw8eWl\",\n" +
            "        \"enabled\": true,\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-02T17:47:06.381Z\",\n" +
            "        \"updatedAt\": \"2021-11-02T17:47:06.381Z\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"20Pa0p59biSDa8y0cgjLh7JTP05\",\n" +
            "        \"sourceId\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "        \"destinationId\": \"20Pa0T3VoQZZYPgh5LQS2vuwg3N\",\n" +
            "        \"enabled\": true,\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-03T14:07:35.341Z\",\n" +
            "        \"updatedAt\": \"2021-11-03T14:07:35.341Z\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"destinations\": [\n" +
            "      {\n" +
            "        \"config\": {},\n" +
            "        \"secretConfig\": {},\n" +
            "        \"id\": \"20NBa9wa4Zb5ZHkJHO2IEiw8eWl\",\n" +
            "        \"name\": \"androidTest-firebase\",\n" +
            "        \"enabled\": true,\n" +
            "        \"workspaceId\": \"1xXCSVsmPjqFKhRlpajnDXkGwYX\",\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-02T17:47:05.153Z\",\n" +
            "        \"updatedAt\": \"2021-11-02T17:47:05.153Z\",\n" +
            "        \"destinationDefinition\": {\n" +
            "          \"config\": {\n" +
            "            \"destConfig\": {\n" +
            "              \"ios\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"unity\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"android\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"reactnative\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"defaultConfig\": []\n" +
            "            },\n" +
            "            \"secretKeys\": [],\n" +
            "            \"excludeKeys\": [],\n" +
            "            \"includeKeys\": [],\n" +
            "            \"transformAt\": \"processor\",\n" +
            "            \"transformAtV1\": \"processor\",\n" +
            "            \"supportedSourceTypes\": [\n" +
            "              \"android\",\n" +
            "              \"ios\",\n" +
            "              \"unity\",\n" +
            "              \"reactnative\",\n" +
            "              \"flutter\"\n" +
            "            ],\n" +
            "            \"saveDestinationResponse\": false\n" +
            "          },\n" +
            "          \"configSchema\": null,\n" +
            "          \"responseRules\": null,\n" +
            "          \"id\": \"1YL4j4RpSLloVaMwKrOoXLfiryj\",\n" +
            "          \"name\": \"FIREBASE\",\n" +
            "          \"displayName\": \"Firebase\",\n" +
            "          \"category\": null,\n" +
            "          \"createdAt\": \"2020-02-26T09:17:52.231Z\",\n" +
            "          \"updatedAt\": \"2021-11-01T17:29:27.375Z\"\n" +
            "        }\n" +
            "      },\n" +
            "      {\n" +
            "        \"config\": {\n" +
            "          \"apiKey\": \"1234abcd\",\n" +
            "          \"groupTypeTrait\": \"test\",\n" +
            "          \"groupValueTrait\": \"test_name\",\n" +
            "          \"trackAllPages\": false,\n" +
            "          \"trackCategorizedPages\": true,\n" +
            "          \"trackNamedPages\": true,\n" +
            "          \"traitsToIncrement\": [\n" +
            "            {\n" +
            "              \"traits\": \"\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"traitsToSetOnce\": [\n" +
            "            {\n" +
            "              \"traits\": \"\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"traitsToAppend\": [\n" +
            "            {\n" +
            "              \"traits\": \"\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"traitsToPrepend\": [\n" +
            "            {\n" +
            "              \"traits\": \"\"\n" +
            "            }\n" +
            "          ],\n" +
            "          \"trackProductsOnce\": true,\n" +
            "          \"trackRevenuePerProduct\": false,\n" +
            "          \"eventUploadPeriodMillis\": 30000,\n" +
            "          \"eventUploadThreshold\": 30,\n" +
            "          \"versionName\": \"\",\n" +
            "          \"enableLocationListening\": false,\n" +
            "          \"useAdvertisingIdForDeviceId\": false,\n" +
            "          \"trackSessionEvents\": true\n" +
            "        },\n" +
            "        \"secretConfig\": {},\n" +
            "        \"id\": \"20Pa0T3VoQZZYPgh5LQS2vuwg3N\",\n" +
            "        \"name\": \"android-test-amplitude\",\n" +
            "        \"enabled\": true,\n" +
            "        \"workspaceId\": \"1xXCSVsmPjqFKhRlpajnDXkGwYX\",\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-03T14:07:33.995Z\",\n" +
            "        \"updatedAt\": \"2021-11-03T14:07:33.995Z\",\n" +
            "        \"destinationDefinition\": {\n" +
            "          \"config\": {\n" +
            "            \"destConfig\": {\n" +
            "              \"ios\": [\n" +
            "                \"eventUploadPeriodMillis\",\n" +
            "                \"eventUploadThreshold\",\n" +
            "                \"useNativeSDK\",\n" +
            "                \"trackSessionEvents\",\n" +
            "                \"useIdfaAsDeviceId\"\n" +
            "              ],\n" +
            "              \"web\": [\n" +
            "                \"useNativeSDK\",\n" +
            "                \"preferAnonymousIdForDeviceId\",\n" +
            "                \"deviceIdFromUrlParam\",\n" +
            "                \"forceHttps\",\n" +
            "                \"trackGclid\",\n" +
            "                \"trackReferrer\",\n" +
            "                \"saveParamsReferrerOncePerSession\",\n" +
            "                \"trackUtmProperties\",\n" +
            "                \"unsetParamsReferrerOnNewSession\",\n" +
            "                \"batchEvents\",\n" +
            "                \"eventUploadPeriodMillis\",\n" +
            "                \"eventUploadThreshold\",\n" +
            "                \"blackListedEvents\"\n" +
            "              ],\n" +
            "              \"android\": [\n" +
            "                \"eventUploadPeriodMillis\",\n" +
            "                \"eventUploadThreshold\",\n" +
            "                \"useNativeSDK\",\n" +
            "                \"enableLocationListening\",\n" +
            "                \"trackSessionEvents\",\n" +
            "                \"useAdvertisingIdForDeviceId\"\n" +
            "              ],\n" +
            "              \"defaultConfig\": [\n" +
            "                \"apiKey\",\n" +
            "                \"groupTypeTrait\",\n" +
            "                \"groupValueTrait\",\n" +
            "                \"trackAllPages\",\n" +
            "                \"trackCategorizedPages\",\n" +
            "                \"trackNamedPages\",\n" +
            "                \"traitsToIncrement\",\n" +
            "                \"traitsToSetOnce\",\n" +
            "                \"traitsToAppend\",\n" +
            "                \"traitsToPrepend\",\n" +
            "                \"trackProductsOnce\",\n" +
            "                \"trackRevenuePerProduct\",\n" +
            "                \"versionName\"\n" +
            "              ]\n" +
            "            },\n" +
            "            \"secretKeys\": [\n" +
            "              \"apiKey\"\n" +
            "            ],\n" +
            "            \"excludeKeys\": [],\n" +
            "            \"includeKeys\": [\n" +
            "              \"apiKey\",\n" +
            "              \"groupTypeTrait\",\n" +
            "              \"groupValueTrait\",\n" +
            "              \"trackAllPages\",\n" +
            "              \"trackCategorizedPages\",\n" +
            "              \"trackNamedPages\",\n" +
            "              \"traitsToIncrement\",\n" +
            "              \"traitsToSetOnce\",\n" +
            "              \"traitsToAppend\",\n" +
            "              \"traitsToPrepend\",\n" +
            "              \"trackProductsOnce\",\n" +
            "              \"trackRevenuePerProduct\",\n" +
            "              \"preferAnonymousIdForDeviceId\",\n" +
            "              \"deviceIdFromUrlParam\",\n" +
            "              \"forceHttps\",\n" +
            "              \"trackGclid\",\n" +
            "              \"trackReferrer\",\n" +
            "              \"saveParamsReferrerOncePerSession\",\n" +
            "              \"trackUtmProperties\",\n" +
            "              \"unsetParamsReferrerOnNewSession\",\n" +
            "              \"batchEvents\",\n" +
            "              \"eventUploadPeriodMillis\",\n" +
            "              \"eventUploadThreshold\",\n" +
            "              \"versionName\",\n" +
            "              \"enableLocationListening\",\n" +
            "              \"useAdvertisingIdForDeviceId\",\n" +
            "              \"trackSessionEvents\",\n" +
            "              \"useIdfaAsDeviceId\",\n" +
            "              \"blackListedEvents\"\n" +
            "            ],\n" +
            "            \"transformAt\": \"processor\",\n" +
            "            \"transformAtV1\": \"processor\",\n" +
            "            \"supportedSourceTypes\": [\n" +
            "              \"android\",\n" +
            "              \"ios\",\n" +
            "              \"web\",\n" +
            "              \"unity\",\n" +
            "              \"amp\",\n" +
            "              \"cloud\",\n" +
            "              \"warehouse\",\n" +
            "              \"reactnative\",\n" +
            "              \"flutter\",\n" +
            "              \"cordova\"\n" +
            "            ],\n" +
            "            \"supportedMessageTypes\": [\n" +
            "              \"alias\",\n" +
            "              \"group\",\n" +
            "              \"identify\",\n" +
            "              \"page\",\n" +
            "              \"screen\",\n" +
            "              \"track\"\n" +
            "            ],\n" +
            "            \"saveDestinationResponse\": true\n" +
            "          },\n" +
            "          \"configSchema\": null,\n" +
            "          \"responseRules\": null,\n" +
            "          \"id\": \"1QGzO4fWSyq3lsyFHf4eQAMDSr9\",\n" +
            "          \"name\": \"AM\",\n" +
            "          \"displayName\": \"Amplitude\",\n" +
            "          \"category\": null,\n" +
            "          \"createdAt\": \"2019-09-02T08:08:05.613Z\",\n" +
            "          \"updatedAt\": \"2021-11-01T17:27:45.061Z\"\n" +
            "        }\n" +
            "      }\n" +
            "    ],\n" +
            "    \"sourceDefinition\": {\n" +
            "      \"id\": \"1QGzOQGVLM35GgtteFH1vYCE0WT\",\n" +
            "      \"name\": \"Android\",\n" +
            "      \"options\": null,\n" +
            "      \"displayName\": \"Android\",\n" +
            "      \"category\": null,\n" +
            "      \"createdAt\": \"2019-09-02T08:08:08.373Z\",\n" +
            "      \"updatedAt\": \"2020-06-18T11:54:00.449Z\"\n" +
            "    }\n" +
            "  }\n" +
            "}"
        private const val testJson2 = "{\n" +
            "  \"isHosted\": true,\n" +
            "  \"source\": {\n" +
            "    \"config\": {\n" +
            "      \"statsCollection\": {\n" +
            "        \"errorReports\": {\n" +
            "          \"enabled\": false\n" +
            "        },\n" +
            "        \"metrics\": {\n" +
            "          \"enabled\": false\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"liveEventsConfig\": {\n" +
            "      \"eventUpload\": false,\n" +
            "      \"eventUploadTS\": 1659967927971\n" +
            "    },\n" +
            "    \"id\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "    \"name\": \"TestAndroid\",\n" +
            "    \"writeKey\": \"1xXCubSHWXbpBI2h6EpCjKOsxmQ\",\n" +
            "    \"enabled\": true,\n" +
            "    \"sourceDefinitionId\": \"1QGzOQGVLM35GgtteFH1vYCE0WT\",\n" +
            "    \"createdBy\": \"1xXC9Q9pYMrw0OON9Aw2AFeBsUp\",\n" +
            "    \"workspaceId\": \"1xXCSVsmPjqFKhRlpajnDXkGwYX\",\n" +
            "    \"deleted\": false,\n" +
            "    \"transient\": false,\n" +
            "    \"secretVersion\": null,\n" +
            "    \"createdAt\": \"2021-09-01T10:31:09.414Z\",\n" +
            "    \"updatedAt\": \"2022-08-08T14:12:07.974Z\",\n" +
            "    \"connections\": [\n" +
            "      {\n" +
            "        \"id\": \"20NBaKc9N7u13PPOVb3CNfozNvo\",\n" +
            "        \"sourceId\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "        \"destinationId\": \"20NBa9wa4Zb5ZHkJHO2IEiw8eWl\",\n" +
            "        \"enabled\": true,\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-02T17:47:06.381Z\",\n" +
            "        \"updatedAt\": \"2021-11-02T17:47:06.381Z\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"id\": \"20Pa0p59biSDa8y0cgjLh7JTP05\",\n" +
            "        \"sourceId\": \"1xXCuf5lPxC0FjFeZD3udJjYY98\",\n" +
            "        \"destinationId\": \"20Pa0T3VoQZZYPgh5LQS2vuwg3N\",\n" +
            "        \"enabled\": true,\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-03T14:07:35.341Z\",\n" +
            "        \"updatedAt\": \"2021-11-03T14:07:35.341Z\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"destinations\": [\n" +
            "      {\n" +
            "        \"config\": {},\n" +
            "        \"liveEventsConfig\": null,\n" +
            "        \"secretConfig\": {},\n" +
            "        \"id\": \"20NBa9wa4Zb5ZHkJHO2IEiw8eWl\",\n" +
            "        \"name\": \"androidTest-firebase\",\n" +
            "        \"enabled\": true,\n" +
            "        \"workspaceId\": \"1xXCSVsmPjqFKhRlpajnDXkGwYX\",\n" +
            "        \"deleted\": false,\n" +
            "        \"createdAt\": \"2021-11-02T17:47:05.153Z\",\n" +
            "        \"updatedAt\": \"2022-05-25T11:34:17.873Z\",\n" +
            "        \"revisionId\": \"29efQiNb1Cwh7GO9dfuuFZ7UN3Q\",\n" +
            "        \"secretVersion\": null,\n" +
            "        \"transformations\": [],\n" +
            "        \"destinationDefinition\": {\n" +
            "          \"config\": {\n" +
            "            \"destConfig\": {\n" +
            "              \"ios\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"unity\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"android\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"reactnative\": [\n" +
            "                \"useNativeSDK\"\n" +
            "              ],\n" +
            "              \"defaultConfig\": [\n" +
            "                \"blacklistedEvents\",\n" +
            "                \"whitelistedEvents\",\n" +
            "                \"eventFilteringOption\"\n" +
            "              ]\n" +
            "            },\n" +
            "            \"secretKeys\": [],\n" +
            "            \"excludeKeys\": [],\n" +
            "            \"includeKeys\": [\n" +
            "              \"blacklistedEvents\",\n" +
            "              \"whitelistedEvents\",\n" +
            "              \"eventFilteringOption\"\n" +
            "            ],\n" +
            "            \"transformAt\": \"processor\",\n" +
            "            \"transformAtV1\": \"processor\",\n" +
            "            \"supportedSourceTypes\": [\n" +
            "              \"android\",\n" +
            "              \"ios\",\n" +
            "              \"unity\",\n" +
            "              \"reactnative\"\n" +
            "            ],\n" +
            "            \"saveDestinationResponse\": false\n" +
            "          },\n" +
            "          \"configSchema\": null,\n" +
            "          \"responseRules\": null,\n" +
            "          \"options\": null,\n" +
            "          \"id\": \"1YL4j4RpSLloVaMwKrOoXLfiryj\",\n" +
            "          \"name\": \"FIREBASE\",\n" +
            "          \"displayName\": \"Firebase\",\n" +
            "          \"category\": null,\n" +
            "          \"createdAt\": \"2020-02-26T09:17:52.231Z\",\n" +
            "          \"updatedAt\": \"2022-05-13T09:31:26.459Z\"\n" +
            "        },\n" +
            "        \"areTransformationsConnected\": true\n" +
            "      }\n" +
            "    ],\n" +
            "    \"sourceDefinition\": {\n" +
            "      \"options\": null,\n" +
            "      \"id\": \"1QGzOQGVLM35GgtteFH1vYCE0WT\",\n" +
            "      \"name\": \"Android\",\n" +
            "      \"displayName\": \"Android\",\n" +
            "      \"category\": null,\n" +
            "      \"createdAt\": \"2019-09-02T08:08:08.373Z\",\n" +
            "      \"updatedAt\": \"2020-06-18T11:54:00.449Z\"\n" +
            "    }\n" +
            "  }\n" +
            "}"
    }

    @Test
    fun testJsonDeserialization() {
        // deserialize
        val rta = object : RudderTypeAdapter<RudderServerConfig>() {}
        val res = jsonAdapter.readJson<RudderServerConfig>(testJson, rta)
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
    fun testJsonDeserialization2() {
        // deserialize
        val rta = object : RudderTypeAdapter<RudderServerConfig>() {}
        val res = jsonAdapter.readJson<RudderServerConfig>(testJson2, rta)
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
    fun testObjectSerializationDeserialization(){
        val objectToSave = RudderServerConfig(true,
            RudderServerConfig.RudderServerConfigSource(
                "sourceId", "sourceName", true,
                "2020-02-26T09:17:52.231Z",
                listOf(
                    RudderServerConfig.RudderServerDestination(
                        "d_id", "d_name", true,
                        "2021-02-26T09:17:52.231Z",
                        RudderServerConfig.RudderServerDestinationDefinition(
                            "d_d_name", "d_disp_name", "2022-02-26T09:17:52.231Z"
                        ), mapOf(
                            "config" to "config_v"
                        ), true
                    )
                )
            ))
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos).also {  it.writeObject(objectToSave)}
        val savedBytes = baos.toByteArray()
        oos.close()
        val bais = ByteArrayInputStream(savedBytes)
        val ois = ObjectInputStream(bais)
        val savedObject = ois.readObject()
        ois.close()
        assertThat(savedObject, allOf(
            isA(RudderServerConfig::class.java),
            hasProperty("hosted", `is`(true)),
            hasProperty("source", allOf(
                isA<RudderServerConfig.RudderServerConfigSource>(RudderServerConfig.RudderServerConfigSource::class.java),
                hasProperty("sourceId", `is`("sourceId")),
                hasProperty("sourceName", `is`("sourceName")),
                hasProperty("sourceEnabled", `is`(true)),
                hasProperty("updatedAt", `is`("2020-02-26T09:17:52.231Z")),
                hasProperty("destinations", allOf<List<RudderServerConfig.RudderServerDestination>>(
                    iterableWithSize(1),
                    hasItem<RudderServerConfig.RudderServerDestination>(
                        allOf(
                            hasProperty("destinationId", `is`("d_id")),
                            hasProperty("destinationName", `is`("d_name")),
                            hasProperty("destinationEnabled", `is`(true)),
                            hasProperty("areTransformationsConnected", `is`(true)),
                            hasProperty("updatedAt", `is`("2021-02-26T09:17:52.231Z")),
                            hasProperty("destinationConfig", allOf<Map<String,Any>>(
                                aMapWithSize(1),
                                hasEntry("config", "config_v")
                            )),
                            hasProperty("destinationDefinition", allOf<RudderServerConfig.RudderServerDestinationDefinition>(
                                isA(RudderServerConfig.RudderServerDestinationDefinition::class.java),
                                hasProperty("definitionName", `is`("d_d_name")),
                                hasProperty("displayName", `is`("d_disp_name")),
                                hasProperty("updatedAt", `is`("2022-02-26T09:17:52.231Z")),
                            ))
                        )
                    )
                ))
            ))
        ))
    }
}
class RudderServerConfigParseTestJackson : RudderServerConfigParseTest() {
    init {
        jsonAdapter = JacksonAdapter()
    }
}
class RudderServerConfigParseTestGson : RudderServerConfigParseTest() {
    init {
        jsonAdapter = GsonAdapter()
    }
}
class RudderServerConfigParseTestMoshi : RudderServerConfigParseTest() {
    init {
        jsonAdapter = MoshiAdapter()
    }
}
