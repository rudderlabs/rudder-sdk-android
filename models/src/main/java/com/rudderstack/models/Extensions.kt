@file:JvmName("MessageUtils")
/*
 * Creator: Debanjan Chatterjee on 29/03/22, 11:15 AM Last modified: 29/03/22, 11:15 AM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

/**
 * To be used to extract traits from context of [Message]
 */
val MessageContext.traits: Map<String, Any?>?
    get() = get(Constants.TRAITS_ID) as? Map<String, Any?>?

/**
 * To be used to extract external ids from context of [Message]
 */
val MessageContext.externalIds: List<Map<String, String>>?
    get() = get(Constants.EXTERNAL_ID) as? List<Map<String, String>>?

/**
 * To be used to extract custom contexts from context of [Message]
 */
val MessageContext.customContexts: Map<String, Any>?
    get() = get(Constants.CUSTOM_CONTEXT_MAP_ID) as? Map<String, Any>?

fun MessageContext.withExternalIdsRemoved() = this - Constants.EXTERNAL_ID
