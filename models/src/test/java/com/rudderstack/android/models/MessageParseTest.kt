/*
 * Creator: Debanjan Chatterjee on 17/12/21, 12:47 PM Last modified: 17/12/21, 12:47 PM
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

package com.rudderstack.android.models

import com.rudderstack.android.gsonrudderadapter.GsonAdapter
import com.rudderstack.android.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.android.moshirudderadapter.MoshiAdapter
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Test

abstract class MessageParseTest {
    abstract var jsonAdapter : JsonAdapter

    companion object {
        private const val TRACK_JSON =
            "{\n" +
                    "  \"type\": \"Track\",\n" +
                    "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                    "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                    "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                    "  \"userId\": \"debanjanchatterjee\",\n" +
                    "  \"integrations\": {\n" +
                    "    \n" +
                    "  },\n" +
                    "  \"event\": \"Java Test\",\n" +
                    "  \"properties\": {\n" +
                    "    \"count\": 1\n" +
                    "  }\n" +
                    "}"
        private const val ALIAS_JSON = "{\n" +
                "  \"type\": \"Alias\",\n" +
                "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                "  \"userId\": \"debanjanchatterjee\",\n" +
                "  \"integrations\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"previousId\": \"172d84b9-a684-4249-8646-0994173555cd\"\n" +
                "}"

        private const val GROUP_JSON = "{\n" +
                "  \"type\": \"Group\",\n" +
                "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                "  \"userId\": \"debanjanchatterjee\",\n" +
                "  \"integrations\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"groupId\": \"193d84b9-a684-4249-8646-0994173555cd\",\n" +
                "  \"traits\": {\n" +
                "    \"group\": \"some_name\",\n" +
                "    \"journey\": \"Australia\"\n" +
                "  }\n" +
                "}"
        private const val SCREEN_JSON = "{\n" +
                "  \"type\": \"Screen\",\n" +
                "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                "  \"userId\": \"debanjanchatterjee\",\n" +
                "  \"integrations\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"event\": \"Java Test\",\n" +
                "  \"properties\": {\n" +
                "    \"count\": 1\n" +
                "  }\n" +
                "}"

        private const val PAGE_JSON = "{\n" +
                "  \"type\": \"Page\",\n" +
                "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                "  \"userId\": \"debanjanchatterjee\",\n" +
                "  \"integrations\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"event\": \"Java Test\",\n" +
                "  \"properties\": {\n" +
                "    \"count\": 1\n" +
                "  },\n" +
                "  \"category\": \"some_category\"\n" +
                "}"
        private const val IDENTIFY_JSON = "{\n" +
                "  \"type\": \"Identify\",\n" +
                "  \"messageId\": \"172d84b9-a684-4249-8646-0994173555cc\",\n" +
                "  \"timestamp\": \"2021-11-20T15:37:19.753Z\",\n" +
                "  \"anonymousId\": \"bc73bb87-8fb4-4498-97c8-570299a4686d\",\n" +
                "  \"userId\": \"debanjanchatterjee\",\n" +
                "  \"integrations\": {\n" +
                "    \"firebase\": true,\n" +
                "    \"amplitude\": false\n" +
                "  }\n" +
                "}"
    }
    @Test
    fun testTrackParsing(){
        val track = jsonAdapter.readJson(TRACK_JSON, TrackMessage::class.java)
        MatcherAssert.assertThat(track, allOf(notNullValue(),
        hasProperty("type", `is`(Message.EventType.TRACK)),
        hasProperty("messageChanel", `is`("server")),
        hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z")),
        hasProperty("properties", allOf( aMapWithSize<String,String>(1))),
        hasProperty("eventName", `is`("Java Test"))))
        assertThat(track!!.properties!!["count"], `is`("1"))
        //serialization
        val trackJson = jsonAdapter.writeToJson(track)
        assertThat(trackJson, `is`(TRACK_JSON.replace("\n", "").replace(" ", "")))
    }
    @Test
    fun testAliasParsing(){
        val alias = jsonAdapter.readJson(ALIAS_JSON, AliasMessage::class.java)
        MatcherAssert.assertThat(alias, allOf(notNullValue(),
        hasProperty("type", `is`(Message.EventType.ALIAS)),
        hasProperty("messageChanel", `is`("server")),
        hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z"))))
    }

    @Test
    fun testGroupParsing(){
        val group = jsonAdapter.readJson(GROUP_JSON, GroupMessage::class.java)
        assertThat(group, allOf(notNullValue(),
            hasProperty("type", `is`(Message.EventType.GROUP)),
            hasProperty("messageChanel", `is`("server")),
            hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z")),
            hasProperty("traits", allOf( aMapWithSize<String,String>(2))),
//            hasProperty("eventName", `is`("Java Test"))
            ))
        assertThat(group!!.traits!!["group"], `is`("some_name"))
        assertThat(group.traits!!["journey"], `is`("Australia"))
    }
    @Test
    fun testScreenParsing(){
        val screen = jsonAdapter.readJson(SCREEN_JSON, ScreenMessage::class.java)
        assertThat(screen, allOf(notNullValue(),
            hasProperty("type", `is`(Message.EventType.SCREEN)),
            hasProperty("messageChanel", `is`("server")),
            hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z")),
            hasProperty("properties", allOf( aMapWithSize<String,String>(1))),
            hasProperty("userId", `is`("debanjanchatterjee"))
            ))
        assertThat(screen!!.properties!!["count"], `is`("1"))
    }
    @Test
    fun testPageParsing(){
        val page = jsonAdapter.readJson(PAGE_JSON, PageMessage::class.java)
        assertThat(page, allOf(notNullValue(),
            hasProperty("type", `is`(Message.EventType.PAGE)),
            hasProperty("messageChanel", `is`("server")),
            hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z")),
            hasProperty("properties", allOf( aMapWithSize<String,String>(1))),
            hasProperty("userId", `is`("debanjanchatterjee")),
            hasProperty("category", `is`("some_category"))
            ))
        assertThat(page!!.properties!!["count"], `is`("1"))
    }
    @Test
    fun testIdentifyParsing(){
        val identify = jsonAdapter.readJson(IDENTIFY_JSON, IdentifyMessage::class.java)
        assertThat(identify, allOf(notNullValue(),
            hasProperty("type", `is`(Message.EventType.IDENTIFY)),
            hasProperty("messageChanel", `is`("server")),
            hasProperty("timestamp", `is`("2021-11-20T15:37:19.753Z")),
            hasProperty("integrations", allOf( aMapWithSize<String,String>(2))),
            ))
        assertThat(identify!!.integrations!!["firebase"], `is`(true))
        assertThat(identify.integrations!!["amplitude"], `is`(false))
    }

}

class MessageParseGsonTest : MessageParseTest() {
    override var jsonAdapter: JsonAdapter = GsonAdapter()
}
class MessageParseJacksonTest : MessageParseTest() {
    override var jsonAdapter: JsonAdapter = JacksonAdapter()
}
class MessageParseMoshiTest : MessageParseTest() {
    override var jsonAdapter: JsonAdapter = MoshiAdapter(coreMoshi)
}