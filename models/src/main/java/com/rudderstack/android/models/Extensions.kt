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
@file:JvmName("ModelExtensions")
package com.rudderstack.android.models

/**
 * To be used to extract traits from context of [Message]
 */
val Map<String, Any?>.traits : Map<String, Any?>?
    get() = getOrDefault(Constants.TRAITS_ID, null) as? Map<String, Any?>?

/**
 * To be used to extract external ids from context of [Message]
 */
val Map<String, Any?>.externalIds : List<Map<String, String>>?
    get() = getOrDefault(Constants.EXTERNAL_ID, null) as? List<Map<String, String>>?
/**
 * To be used to extract custom contexts from context of [Message]
 */
val Map<String, Any?>.customContexts
    get() = getOrDefault(Constants.CUSTOM_CONTEXT_MAP_ID, null)