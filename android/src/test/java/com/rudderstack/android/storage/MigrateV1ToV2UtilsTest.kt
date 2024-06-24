/*
 * Creator: Debanjan Chatterjee on 26/02/24, 7:15 pm Last modified: 26/02/24, 7:15 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.android.storage

import android.content.ContentValues
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import com.rudderstack.android.utils.TestExecutor
import com.rudderstack.android.utils.busyWait
import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.RudderDatabase
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import com.rudderstack.core.RudderUtils
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.Message
import com.rudderstack.models.android.RudderApp
import com.rudderstack.models.android.RudderContext
import com.rudderstack.models.android.RudderDeviceInfo
import com.rudderstack.models.android.RudderOSInfo
import com.rudderstack.models.android.RudderScreenInfo
import com.rudderstack.models.android.RudderTraits
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.squareup.moshi.Json
import junit.framework.TestSuite
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.robolectric.annotation.Config
import java.lang.reflect.Type
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
abstract class MigrateV1ToV2UtilsTest {
    abstract val jsonAdapter: JsonAdapter
    private fun v1Entity1(type: String) = V1Entity(
        V1Message(
            messageId = "messageId1",
            channel = "mobile",
            context = RudderContext().also {
                it.app = RudderApp(
                    "build", "name", "namespace", "1.0"
                )
                it.customContextMap = mutableMapOf("key_cc" to "value_cc")
                it.os = RudderOSInfo(
                    "name", "version"
                )
                it.device = RudderDeviceInfo(
                    "id", "manufacturer", "model", "name", "type", "token"
                )
                it.screen = RudderScreenInfo(
                    10, 45, 60
                )
                it.locale = "locale"
            },
            type = type,
            action = "action",
            timestamp = "timestamp",
            anonymousId = "anonymousId",
            userId = "userId",
            event = "event",
            properties = mapOf("key1" to "value1"),
            userProperties = mapOf("key2" to "value2"),
            integrations = mapOf("key3" to true),
            destinationProps = mapOf("key4" to mapOf("key5" to "value5")),
            previousId = "previousId",
            traits = RudderTraits(
                RudderTraits.Address(
                    "city", "country", "7474747", "state", "street"
                ),
                "email",
                "19/12/2005",
                RudderTraits.Company().also {
                    it.putId("id")
                    it.putName("name")
                    it.putIndustry("industry")
                },
                "19/01/1992",
                "description",
                "c_name@gmail.com",
                "firstName",
                "male",
                "id",
                "lastName",
                "my_name",
                "023-8393939",
                "title",
                "username",
            ),

            groupId = "groupId"
        )
    )

    private lateinit var v2Database: RudderDatabase

    @Before
    fun setUp() {
        v2Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence-default.db",
            entityFactory = RudderEntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(100)
    }

    @After
    fun tearDown() {
        v2Database.shutDown()
    }

    @Test
    fun testMigrateTrackV1ToV2() {

        var v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        var v1Dao = v1Database.getDao(V1Entity::class.java)
        val v1Entity1 = v1Entity1("track")
        println(v1Entity1.v1Message)
        with(v1Dao) {
            listOf(v1Entity1).insertSync()
        }
        v1Database.shutDown()

        migrateV1MessagesToV2Database(
            ApplicationProvider.getApplicationContext(),
            v2Database,
            jsonAdapter,
            null,
            TestExecutor()
        )
        //assert that v1 database is empty
        v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        v1Dao = v1Database.getDao(V1Entity::class.java)
        MatcherAssert.assertThat(v1Dao.getAllSync()?.size, Matchers.equalTo(0))
        v1Database.shutDown()
        val v2Dao = v2Database.getDao(MessageEntity::class.java)
        val v2Entities = v2Dao.getAllSync()
        MatcherAssert.assertThat(v2Entities?.size ?: 0, greaterThanOrEqualTo(1))
        val v2Entity = v2Entities?.get(0)
        MatcherAssert.assertThat(
            v2Entity?.message, allOf(
                Matchers.notNullValue(),
                hasProperty("messageId", Matchers.equalTo(v1Entity1.v1Message.messageId)),
                hasProperty("channel", Matchers.equalTo(v1Entity1.v1Message.channel)),
                hasProperty("eventName", Matchers.equalTo(v1Entity1.v1Message.event)),
                hasProperty(
                    "type", Matchers.equalTo(
                        Message.EventType.fromValue(
                            v1Entity1.v1Message.type!!
                        )
                    )
                ),
//            hasProperty("action", Matchers.equalTo(v1Entity1.action)), not required
                hasProperty("timestamp", Matchers.equalTo(v1Entity1.v1Message.timestamp)),
                hasProperty("anonymousId", Matchers.equalTo(v1Entity1.v1Message.anonymousId)),
                hasProperty("userId", Matchers.equalTo(v1Entity1.v1Message.userId)),
                hasProperty("properties", Matchers.equalTo(v1Entity1.v1Message.properties)),
//            hasProperty("userProperties", Matchers.equalTo(v1Entity1.userProperties)),
                hasProperty("integrations", Matchers.equalTo(v1Entity1.v1Message.integrations)),
                hasProperty(
                    "destinationProps", Matchers.equalTo(v1Entity1.v1Message.destinationProps)
                ),
//            hasProperty("previousId", Matchers.equalTo(v1Entity1.v1Message.previousId)),
//            hasProperty("groupId", Matchers.equalTo(v1Entity1.v1Message.groupId))
            )
        )
    }

    @Test
    fun testMigrateGroupV1ToV2() {

        var v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        var v1Dao = v1Database.getDao(V1Entity::class.java)
        val v1Entity1 = v1Entity1("group")
        println(v1Entity1.v1Message)
        with(v1Dao) {
            listOf(v1Entity1).insertSync()
        }
        v1Database.shutDown()

        migrateV1MessagesToV2Database(
            ApplicationProvider.getApplicationContext(),
            v2Database,
            jsonAdapter,
            executorService = TestExecutor()
        )
        //assert that v1 database is empty
        v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        v1Dao = v1Database.getDao(V1Entity::class.java)
        MatcherAssert.assertThat(v1Dao.getAllSync()?.size, Matchers.equalTo(0))
        v1Database.shutDown()
        val v2Dao = v2Database.getDao(MessageEntity::class.java)
        val v2Entities = v2Dao.getAllSync()
        MatcherAssert.assertThat(v2Entities?.size ?: 0, greaterThanOrEqualTo(1))
        val v2Entity = v2Entities?.get(0)
        MatcherAssert.assertThat(
            v2Entity?.message, allOf(
                Matchers.notNullValue(),
                hasProperty("messageId", Matchers.equalTo(v1Entity1.v1Message.messageId)),
                hasProperty("channel", Matchers.equalTo(v1Entity1.v1Message.channel)),
//                hasProperty("eventName", Matchers.equalTo(v1Entity1.v1Message.event)),
                hasProperty(
                    "type", Matchers.equalTo(
                        Message.EventType.fromValue(
                            v1Entity1.v1Message.type!!
                        )
                    )
                ),
//            hasProperty("action", Matchers.equalTo(v1Entity1.action)), not required
                hasProperty("timestamp", Matchers.equalTo(v1Entity1.v1Message.timestamp)),
                hasProperty("anonymousId", Matchers.equalTo(v1Entity1.v1Message.anonymousId)),
                hasProperty("userId", Matchers.equalTo(v1Entity1.v1Message.userId)),
//                hasProperty("properties", Matchers.equalTo(v1Entity1.v1Message.properties)),
//            hasProperty("userProperties", Matchers.equalTo(v1Entity1.userProperties)),
                hasProperty("integrations", Matchers.equalTo(v1Entity1.v1Message.integrations)),
                hasProperty(
                    "destinationProps", Matchers.equalTo(v1Entity1.v1Message.destinationProps)
                ),
//            hasProperty("previousId", Matchers.equalTo(v1Entity1.v1Message.previousId)),
                hasProperty("groupId", Matchers.equalTo(v1Entity1.v1Message.groupId))
            )
        )
    }

    @Test
    fun testMigrateIdentifyV1ToV2() {

        var v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        var v1Dao = v1Database.getDao(V1Entity::class.java)
        val v1Entity1 = v1Entity1("identify")
        println(v1Entity1.v1Message)
        with(v1Dao) {
            listOf(v1Entity1).insertSync()
        }
        v1Database.shutDown()

        migrateV1MessagesToV2Database(
            ApplicationProvider.getApplicationContext(),
            v2Database,
            jsonAdapter,
            executorService = TestExecutor()
        )
        //assert that v1 database is empty
        v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        v1Dao = v1Database.getDao(V1Entity::class.java)
        MatcherAssert.assertThat(v1Dao.getAllSync()?.size, Matchers.equalTo(0))
        v1Database.shutDown()
        val v2Dao = v2Database.getDao(MessageEntity::class.java)
        val v2Entities = v2Dao.getAllSync()
        MatcherAssert.assertThat(v2Entities?.size ?: 0, greaterThanOrEqualTo(1))
        val v2Entity = v2Entities?.get(0)
        MatcherAssert.assertThat(
            v2Entity?.message, allOf(
                Matchers.notNullValue(),
                hasProperty("messageId", Matchers.equalTo(v1Entity1.v1Message.messageId)),
                hasProperty("channel", Matchers.equalTo(v1Entity1.v1Message.channel)),
//                hasProperty("eventName", Matchers.equalTo(v1Entity1.v1Message.event)),
                hasProperty(
                    "type", Matchers.equalTo(
                        Message.EventType.fromValue(
                            v1Entity1.v1Message.type!!
                        )
                    )
                ),
//            hasProperty("action", Matchers.equalTo(v1Entity1.action)), not required
                hasProperty("timestamp", Matchers.equalTo(v1Entity1.v1Message.timestamp)),
                hasProperty("anonymousId", Matchers.equalTo(v1Entity1.v1Message.anonymousId)),
                hasProperty("userId", Matchers.equalTo(v1Entity1.v1Message.userId)),
                hasProperty("properties", Matchers.equalTo(v1Entity1.v1Message.properties)),
//            hasProperty("userProperties", Matchers.equalTo(v1Entity1.userProperties)),
                hasProperty("integrations", Matchers.equalTo(v1Entity1.v1Message.integrations)),
                hasProperty(
                    "destinationProps", Matchers.equalTo(v1Entity1.v1Message.destinationProps)
                ),
//            hasProperty("previousId", Matchers.equalTo(v1Entity1.v1Message.previousId)),
//            hasProperty("groupId", Matchers.equalTo(v1Entity1.v1Message.groupId))
            )
        )
    }

    @Test
    fun testMigrateScreenV1ToV2() {

        var v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        var v1Dao = v1Database.getDao(V1Entity::class.java)
        val v1Entity1 = v1Entity1("screen")
        println(v1Entity1.v1Message)
        with(v1Dao) {
            listOf(v1Entity1).insertSync()
        }
        v1Database.shutDown()

        migrateV1MessagesToV2Database(
            ApplicationProvider.getApplicationContext(),
            v2Database,
            jsonAdapter,
            executorService = TestExecutor()
        )
        //assert that v1 database is empty
        v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        v1Dao = v1Database.getDao(V1Entity::class.java)
        MatcherAssert.assertThat(v1Dao.getAllSync()?.size, Matchers.equalTo(0))
        v1Database.shutDown()
        val v2Dao = v2Database.getDao(MessageEntity::class.java)
        val v2Entities = v2Dao.getAllSync()
        MatcherAssert.assertThat(v2Entities?.size ?: 0, greaterThanOrEqualTo(1))
        val v2Entity = v2Entities?.get(0)
        MatcherAssert.assertThat(
            v2Entity?.message, allOf(
                Matchers.notNullValue(),
                hasProperty("messageId", Matchers.equalTo(v1Entity1.v1Message.messageId)),
                hasProperty("channel", Matchers.equalTo(v1Entity1.v1Message.channel)),
                hasProperty("eventName", Matchers.equalTo(v1Entity1.v1Message.event)),
                hasProperty(
                    "type", Matchers.equalTo(
                        Message.EventType.fromValue(
                            v1Entity1.v1Message.type!!
                        )
                    )
                ),
                hasProperty("timestamp", Matchers.equalTo(v1Entity1.v1Message.timestamp)),
                hasProperty("anonymousId", Matchers.equalTo(v1Entity1.v1Message.anonymousId)),
                hasProperty("userId", Matchers.equalTo(v1Entity1.v1Message.userId)),
                hasProperty("properties", Matchers.equalTo(v1Entity1.v1Message.properties)),
//            hasProperty("userProperties", Matchers.equalTo(v1Entity1.userProperties)),
                hasProperty("integrations", Matchers.equalTo(v1Entity1.v1Message.integrations)),
                hasProperty(
                    "destinationProps", Matchers.equalTo(v1Entity1.v1Message.destinationProps)
                ),
//            hasProperty("previousId", Matchers.equalTo(v1Entity1.v1Message.previousId)),
//            hasProperty("groupId", Matchers.equalTo(v1Entity1.v1Message.groupId))
            )
        )
    }
    @Test
    fun testCloudModeEventsFilteredWhenCloudModeDoneV1ToV2() {

        var v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        var v1Dao = v1Database.getDao(V1Entity::class.java)
        val v1Entity1 = v1Entity1("screen")
        v1Entity1.status = V1_STATUS_CLOUD_MODE_DONE
        with(v1Dao) {
            listOf(v1Entity1).insertSync()
        }
        v1Database.shutDown()

        migrateV1MessagesToV2Database(
            ApplicationProvider.getApplicationContext(),
            v2Database,
            jsonAdapter,
            executorService = TestExecutor()
        )
        //assert that v1 database is empty
        v1Database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
            databaseName = "rl_persistence.db",
            entityFactory = V1EntityFactory(jsonAdapter),
            providedExecutorService = TestExecutor()
        )
        busyWait(50)
        v1Dao = v1Database.getDao(V1Entity::class.java)
        MatcherAssert.assertThat(v1Dao.getAllSync()?.size, Matchers.equalTo(0))
        v1Database.shutDown()
        val v2Dao = v2Database.getDao(MessageEntity::class.java)
        val v2Entities = v2Dao.getAllSync()
        MatcherAssert.assertThat(v2Entities?.size ?: -1, `is`(0))
    }

    @RudderEntity(
        tableName = "events", fields = arrayOf(
            RudderField(
                RudderField.Type.TEXT, V1Entity.ColumnNames.MESSAGE_ID_COL, primaryKey = true
            ), RudderField(RudderField.Type.TEXT, V1Entity.ColumnNames.MESSAGE_COL), RudderField(
                RudderField.Type.INTEGER, V1Entity.ColumnNames.UPDATED_COL, isIndex = true
            ), RudderField(
                RudderField.Type.INTEGER, V1Entity.ColumnNames.STATUS_COL
            ), RudderField(
                RudderField.Type.INTEGER, V1Entity.ColumnNames.DM_PROCESSED_COL
            )
        )
    )
    class V1Entity(
        val v1Message: V1Message
    ) : Entity {
        val BACKSLASH = "\\\\'"

        object ColumnNames {
            const val MESSAGE_COL = "message"
            const val UPDATED_COL: String = "updated"
            const val MESSAGE_ID_COL: String = "id"
            const val STATUS_COL: String = "status"
            const val DM_PROCESSED_COL = "dm_processed"
        }
        var status = V1_STATUS_NEW

        private val gsonAdapter = GsonAdapter(
            GsonBuilder().registerTypeAdapter(RudderTraits::class.java, RudderTraitsTypeAdapter())
                .registerTypeAdapter(RudderContext::class.java, RudderContextTypeAdapter()).create()
        )

        override fun generateContentValues(): ContentValues {
            val messageJson = gsonAdapter.writeToJson(v1Message)?.replace("'", BACKSLASH)
            return ContentValues().also {
                it.put(ColumnNames.MESSAGE_ID_COL, v1Message.messageId)
                it.put(
                    ColumnNames.MESSAGE_COL,
                    messageJson,
                )
                it.put(ColumnNames.UPDATED_COL, System.currentTimeMillis())
                it.put(ColumnNames.STATUS_COL, status)
                it.put(ColumnNames.DM_PROCESSED_COL, 1)
            }
        }

        override fun getPrimaryKeyValues(): Array<String> {
            return arrayOf(v1Message.messageId!!)
        }
    }
}

data class V1Message(
    val messageId: String? = UUID.randomUUID().toString(),
    val channel: String? = "mobile",
    val context: RudderContext? = null,
    val type: String? = null,
    val action: String? = null,
    @SerializedName("originalTimestamp") @JsonProperty("originalTimestamp") @Json(name = "originalTimestamp") val timestamp: String? = RudderUtils.timeStamp,
    val anonymousId: String? = null,
    val userId: String? = null,
    val event: String? = null,
    val properties: Map<String?, Any?>? = null,
    val userProperties: Map<String?, Any?>? = null,
    val integrations: Map<String?, Any?>? = HashMap(),
    val destinationProps: Map<String?, Map<*, *>?>? = null,
    val previousId: String? = null,
    val traits: RudderTraits? = null,
    val groupId: String? = null
)

class RudderContextTypeAdapter : JsonSerializer<RudderContext?> {
    override fun serialize(
        rudderContext: RudderContext?, typeOfSrc: Type, context: JsonSerializationContext
    ): JsonElement? {
        return try {
            val outputContext = JsonObject()
            val gson = Gson()
            val inputContext = gson.toJsonTree(rudderContext) as JsonObject
            for ((key, value) in inputContext.entrySet()) {
                if (key == "customContextMap") {
                    val customContextMapObject = gson.toJsonTree(value) as JsonObject
                    for ((key1, value1) in customContextMapObject.entrySet()) {
                        outputContext.add(key1, value1)
                    }
                    continue
                }
                outputContext.add(key, value)
            }
            outputContext
        } catch (e: Exception) {
            e.toString()
            null
        }
    }
}

class RudderTraitsTypeAdapter : JsonSerializer<RudderTraits?> {
    override fun serialize(
        traits: RudderTraits?, typeOfSrc: Type, context: JsonSerializationContext
    ): JsonElement? {
        return try {
            val outputTraits = JsonObject()
            val gson = Gson()
            val inputTraits = gson.toJsonTree(traits) as JsonObject
            for ((key, value) in inputTraits.entrySet()) {
                if (key == "extras") {
                    val extrasObject = gson.toJsonTree(value) as JsonObject
                    for ((key1, value1) in extrasObject.entrySet()) {
                        outputTraits.add(key1, value1)
                    }
                    continue
                }
                outputTraits.add(key, value)
            }
            outputTraits
        } catch (e: java.lang.Exception) {
            null
        }
    }


}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class GsonMigrateV1ToV2UtilsTest : MigrateV1ToV2UtilsTest() {
    override val jsonAdapter: JsonAdapter
        get() = GsonAdapter()
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class JacksonMigrateV1ToV2UtilsTest : MigrateV1ToV2UtilsTest() {
    override val jsonAdapter: JsonAdapter
        get() = JacksonAdapter()
}

/*@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class MoshiMigrateV1ToV2UtilsTest : MigrateV1ToV2UtilsTest() {
    override val jsonAdapter: JsonAdapter
        get() = MoshiAdapter()
}*/

@RunWith(Suite::class)
@Suite.SuiteClasses(
    GsonMigrateV1ToV2UtilsTest::class, JacksonMigrateV1ToV2UtilsTest::class,
    //TODO fix moshi adapter
//    MoshiMigrateV1ToV2UtilsTest::class
)
class MigrateV1ToV2UtilsTestSuite : TestSuite() {}

