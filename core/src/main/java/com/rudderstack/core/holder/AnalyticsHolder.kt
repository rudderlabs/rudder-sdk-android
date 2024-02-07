/*
 * Creator: Debanjan Chatterjee on 24/01/24, 11:39 am Last modified: 24/01/24, 11:35 am
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
@file:JvmName("AnalyticsHolder")
package com.rudderstack.core.holder

import com.rudderstack.core.Analytics
import com.rudderstack.core.Controller
import java.util.concurrent.ConcurrentHashMap

/**
 * A thread safe map to store the analytics data
 * This data is not persisted and is mainly intended to maintain transient data associated to each analytics instance
 */
private val store = ConcurrentHashMap<String, ConcurrentHashMap<String, Any?>>()
fun Controller.store(identifier : String, value: Any){
    val analyticsStorageMap = store[this.instanceName] ?: ConcurrentHashMap<String, Any?>().also {
        store[instanceName] = it
    }
    analyticsStorageMap[identifier] = value
}
fun Controller.remove(identifier : String){
    val analyticsStorageMap = store[this.instanceName] ?: return
    analyticsStorageMap.remove(identifier)
}
fun <T> Controller.retrieve(identifier: String) : T?{
    val analyticsStorageMap = store[this.instanceName] ?: return null
    return analyticsStorageMap[identifier] as? T
}
