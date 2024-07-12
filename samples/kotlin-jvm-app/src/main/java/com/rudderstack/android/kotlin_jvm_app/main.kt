package com.rudderstack.android.kotlin_jvm_app

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.RudderUtils
import com.rudderstack.core.models.AliasMessage
import com.rudderstack.core.models.GroupMessage
import com.rudderstack.core.models.IdentifyMessage
import com.rudderstack.core.models.PageMessage
import com.rudderstack.core.models.ScreenMessage
import com.rudderstack.core.models.TrackMessage
import com.rudderstack.core.models.TrackProperties
import com.rudderstack.gsonrudderadapter.GsonAdapter

private lateinit var analytics: Analytics
fun main(args: Array<String>) {
    analytics = Analytics(
        "<WRITE_KEY>",
        Configuration(
            jsonAdapter = GsonAdapter(),
        )
    )

    makeAllEventsDirectlyUsingKotlinEventsAPI()
//    makeAllEventsDirectlyUsingAndroidCompatibleEventsAPI()

    analytics.flush()
}

fun makeAllEventsDirectlyUsingKotlinEventsAPI() {
    val trackMessage = TrackMessage.create(
        eventName = "Track Event 1",
        properties = mapOf("key1" to "prop1", "key2" to "prop2"),
        timestamp = RudderUtils.timeStamp,
    )

    val groupMessage = GroupMessage.create(
        groupId = "testGroupId",
        userId = "testUserId",
        timestamp = RudderUtils.timeStamp,
        groupTraits = mapOf("testKey" to "testValue"),
    )

    val screenMessage = ScreenMessage.create(
        name = "testScreen",
        category = "testCategory",
        properties = mapOf("testKey" to "testValue"),
        timestamp = RudderUtils.timeStamp,
    )

    val identify = IdentifyMessage.create(
        userId = "testUserId",
        traits = mapOf("testKey" to "testValue"),
        timestamp = RudderUtils.timeStamp,
    )

    val alias = AliasMessage.create(
        userId = "New Alias UserID",
        previousId = "Old Alias UserID",
        timestamp = RudderUtils.timeStamp,
    )

    val pageMessage = PageMessage.create(
        anonymousId = "testAnonymousId",
        userId = "testUserId",
        timestamp = RudderUtils.timeStamp,
        name = "testPage",
        category = "testCategory",
        properties = mapOf("testKey" to "testValue"),
    )

    analytics.processMessage(trackMessage, null)
    analytics.processMessage(identify, null)
    analytics.processMessage(screenMessage, null)
    analytics.processMessage(groupMessage, null)
    analytics.processMessage(alias, null)
    analytics.processMessage(pageMessage, null)
}

fun makeAllEventsDirectlyUsingAndroidCompatibleEventsAPI() {
    analytics.track(
        eventName = "Track Event 1",
        trackProperties = TrackProperties("key1" to "prop1", "key2" to "prop2"),
    )

    analytics.identify(
        userId = "testUserId",
        traits = mapOf("testKey" to "testValue"),
    )

    analytics.screen(
        screenName = "testScreen",
        category = "testCategory",
        screenProperties = TrackProperties("testKey" to "testValue"),
    )

    analytics.group(
        groupId = "testGroupId",
        groupTraits = TrackProperties("testKey" to "testValue"),
    )

    analytics.alias(
        newId = "New Alias UserID",
        previousId = "Old Alias UserID",
    )
}
