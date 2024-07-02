package com.rudderstack.android.storage

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.core.RudderUtils
import com.rudderstack.gsonrudderadapter.GsonAdapter
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.core.models.GroupMessage
import com.rudderstack.core.models.IdentifyMessage
import com.rudderstack.core.models.ScreenMessage
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import junit.framework.TestSuite
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.robolectric.annotation.Config

abstract class MessageEntityTest {
    abstract val jsonAdapter: JsonAdapter

    @Test
    fun testGenerateContentValuesForTrack() {
        val testMessage = TrackMessage.create(
            "testEvent",
            RudderUtils.timeStamp,
            mapOf("testKey" to "testValue"),
            "testAnonymousId",
            "testUserId",
            mapOf("dest1" to mapOf("key1" to "value1")),
            mapOf("dest2" to mapOf("some_key" to "some_value")),
            listOf(mapOf("type" to "byomkesh")),
            mapOf("k1" to mapOf("k_1_1" to "v1"))
        )
        val messageEntity = MessageEntity(testMessage, jsonAdapter)
        val contentValues = messageEntity.generateContentValues()
        println("serialised: ${contentValues.get(MessageEntity.ColumnNames.message)}")
        assertNotNull(contentValues)
        val regainedEntity = MessageEntity.create(contentValues.keySet().associateWith {
            contentValues.getAsString(it)
        }, jsonAdapter)
        MatcherAssert.assertThat(
            regainedEntity?.message, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(TrackMessage::class.java),
                Matchers.hasProperty("eventName", Matchers.equalTo("testEvent")),
                Matchers.hasProperty("anonymousId", Matchers.equalTo("testAnonymousId")),
                Matchers.hasProperty("userId", Matchers.equalTo("testUserId")),
                Matchers.hasProperty("properties", Matchers.hasEntry("testKey", "testValue")),
                Matchers.hasProperty("messageId", Matchers.equalTo(testMessage.messageId)),
                Matchers.hasProperty("context", Matchers.equalTo(testMessage.context)),
                Matchers.hasProperty("destinationProps", Matchers.equalTo(testMessage.destinationProps)),

                )
        )
    }

    @Test
    fun testGenerateContentValuesForGroup() {
        val testMessage = GroupMessage.create(
            "testAnonymousId",
            "testUserId",
            RudderUtils.timeStamp,
            mapOf("dest1" to mapOf("key1" to "value1")),
            "testGroup",
            mapOf("testKey" to "testValue"),
            mapOf("key2" to mapOf("some_key" to "some_value")),
            listOf(mapOf("type" to "byomkesh")),
            mapOf("k1" to mapOf("k_1_1" to "v1"))
        )
        val messageEntity = MessageEntity(testMessage, jsonAdapter)
        val contentValues = messageEntity.generateContentValues()
        println("serialised: ${contentValues.get(MessageEntity.ColumnNames.message)}")
        assertNotNull(contentValues)
        val regainedEntity = MessageEntity.create(contentValues.keySet().associateWith {
            contentValues.getAsString(it)
        }, jsonAdapter)
        MatcherAssert.assertThat(
            regainedEntity?.message, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(GroupMessage::class.java),
                Matchers.hasProperty("groupId", Matchers.equalTo(testMessage.groupId)),
                Matchers.hasProperty("anonymousId", Matchers.equalTo(testMessage.anonymousId)),
                Matchers.hasProperty("userId", Matchers.equalTo(testMessage.userId)),
                Matchers.hasProperty("traits", Matchers.hasEntry("testKey", "testValue")),
                Matchers.hasProperty("messageId", Matchers.equalTo(testMessage.messageId)),
                Matchers.hasProperty("context", Matchers.equalTo(testMessage.context)),
                Matchers.hasProperty("destinationProps", Matchers.equalTo(testMessage.destinationProps)),

                )
        )
    }

    @Test
    fun testGenerateContentValuesForIdentify() {
        val testMessage = IdentifyMessage.create(
            "testAnonymousId",
            "testUserId",
            RudderUtils.timeStamp,
            mapOf("testKey" to "testValue"),
            mapOf("dest1" to mapOf("key1" to "value1")),
            mapOf("key2" to mapOf("some_key" to "some_value")),
            listOf(mapOf("type" to "byomkesh")),
            mapOf("k1" to mapOf("k_1_1" to "v1")),
        )
        val messageEntity = MessageEntity(testMessage, jsonAdapter)
        val contentValues = messageEntity.generateContentValues()
        assertNotNull(contentValues)
        val regainedEntity = MessageEntity.create(contentValues.keySet().associateWith {
            contentValues.getAsString(it)
        }, jsonAdapter)
        MatcherAssert.assertThat(
            regainedEntity?.message, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(IdentifyMessage::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo(testMessage.anonymousId)),
                Matchers.hasProperty("userId", Matchers.equalTo(testMessage.userId)),
                Matchers.hasProperty("properties", Matchers.hasEntry("testKey", "testValue")),
                Matchers.hasProperty("messageId", Matchers.equalTo(testMessage.messageId)),
                Matchers.hasProperty("context", Matchers.equalTo(testMessage.context)),
                Matchers.hasProperty("destinationProps", Matchers.equalTo(testMessage.destinationProps)),

                )
        )
    }

    @Test
    fun testGenerateContentValuesForScreen() {
        val testMessage = ScreenMessage.create(
            name = "testScreen",
            RudderUtils.timeStamp,
            "testAnonymousId",
            "testUserId",
            mapOf("dest1" to mapOf("key1" to "value1")),
            category = "testCategory",
            mapOf("testKey" to "testValue"),
            mapOf("key2" to mapOf("some_key" to "some_value")),
            listOf(mapOf("type" to "byomkesh")),
            mapOf("k1" to mapOf("k_1_1" to "v1")),
        )
        val messageEntity = MessageEntity(testMessage, jsonAdapter)
        val contentValues = messageEntity.generateContentValues()
        assertNotNull(contentValues)
        val regainedEntity = MessageEntity.create(contentValues.keySet().associateWith {
            contentValues.getAsString(it)
        }, jsonAdapter)
        MatcherAssert.assertThat(
            regainedEntity?.message, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(ScreenMessage::class.java),
                Matchers.hasProperty("anonymousId", Matchers.equalTo(testMessage.anonymousId)),
                Matchers.hasProperty("userId", Matchers.equalTo(testMessage.userId)),
                Matchers.hasProperty("properties", Matchers.hasEntry("testKey", "testValue")),
                Matchers.hasProperty("messageId", Matchers.equalTo(testMessage.messageId)),
                Matchers.hasProperty("context", Matchers.equalTo(testMessage.context)),
                Matchers.hasProperty("destinationProps", Matchers.equalTo(testMessage.destinationProps)),

                )
        )
    }

    @Test
    fun testGetPrimaryKeyValues() {
        val testMessage = TrackMessage.create(
            "testEvent",
            RudderUtils.timeStamp,
            mapOf("testKey" to "testValue"),
            "testAnonymousId",
            "testUserId",
            mapOf("dest1" to mapOf("key1" to "value1")),
            mapOf("dest2" to mapOf("some_key" to "some_value")),
            listOf(mapOf("type" to "byomkesh")),
            mapOf("k1" to mapOf("k_1_1" to "v1"))
        )
        val messageEntity = MessageEntity(testMessage, jsonAdapter)
        val primaryKeyValues = messageEntity.getPrimaryKeyValues()
        MatcherAssert.assertThat(primaryKeyValues, Matchers.arrayContaining(testMessage.messageId))
    }
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class GsonEntityTest : MessageEntityTest() {
    override val jsonAdapter: JsonAdapter
        get() = GsonAdapter()
}

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class JacksonEntityTest : MessageEntityTest() {
    override val jsonAdapter: JsonAdapter
        get() = JacksonAdapter()
}

/*@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class MoshiEntityTest : MessageEntityTest() {
    override val jsonAdapter: JsonAdapter
        get() = MoshiAdapter()
}*/

@RunWith(Suite::class)
@Suite.SuiteClasses(
    GsonEntityTest::class, JacksonEntityTest::class,
    //TODO fix moshi adapter
//    MoshiEntityTest::class
)
class MessageEntityTestSuite : TestSuite() {}
