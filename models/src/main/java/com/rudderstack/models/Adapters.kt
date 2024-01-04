/*
 * Creator: Debanjan Chatterjee on 20/12/21, 4:08 PM Last modified: 20/12/21, 4:08 PM
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

/*
val coreMoshi : Moshi
    get() = Moshi.Builder()
//        .add(Message.ChannelMoshiAdapter())
        .add(PolymorphicJsonAdapterFactory.of(Message::class.java, "type")
            .withSubtype(TrackMessage::class.java, Message.EventType.TRACK.value)
            .withSubtype(AliasMessage::class.java, Message.EventType.ALIAS.value)
            .withSubtype(GroupMessage::class.java, Message.EventType.GROUP.value)
            .withSubtype(PageMessage::class.java, Message.EventType.PAGE.value)
            .withSubtype(ScreenMessage::class.java, Message.EventType.SCREEN.value)
            .withSubtype(IdentifyMessage::class.java, Message.EventType.IDENTIFY.value)
        )
        .addLast(KotlinJsonAdapterFactory())
        .build()
*/

// val coreGson = RuntimeTypeAdapterFactory.of(Message::class.java)
//    .withSubtype(TrackMessage::class.java, Message.EventType.TRACK.value)
//    .withSubtype(AliasMessage::class.java, Message.EventType.ALIAS.value)
//    .withSubtype(GroupMessage::class.java, Message.EventType.GROUP.value)
//    .withSubtype(PageMessage::class.java, Message.EventType.PAGE.value)
//    .withSubtype(ScreenMessage::class.java, Message.EventType.SCREEN.value)
//    .withSubtype(IdentifyMessage::class.java, Message.EventType.IDENTIFY.value)
