/*
 * Creator: Debanjan Chatterjee on 04/04/22, 2:00 PM Last modified: 04/04/22, 2:00 PM
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
@file:JvmName("TestUtils")

package com.rudderstack.android.core

import java.io.FileInputStream
import java.io.IOException
import java.util.*

/**
 * For running tests, one should have a "test.properties" file inside core module at the root,
 * that contains the following values
 *  dataPlaneUrl=your_dp_url
controlPlaneUrl=your_cp_url
writeKey=your_write_key
 *
 */

private const val PROPERTIES_FILE = "test.properties"
private const val DATA_PLANE_URL_KEY = "dataPlaneUrl"
private const val CONTROL_PLANE_URL_KEY = "controlPlaneUrl"
private const val WRITE_KEY_KEY = "writeKey"

private val properties: Map<String, String> by lazy {
    try {
        Properties().let {
            it.load(FileInputStream(PROPERTIES_FILE))
            mapOf(
                DATA_PLANE_URL_KEY to it.getProperty(DATA_PLANE_URL_KEY),
                CONTROL_PLANE_URL_KEY to it.getProperty(CONTROL_PLANE_URL_KEY),
                WRITE_KEY_KEY to it.getProperty(WRITE_KEY_KEY),
            )
        }
    } catch (ex: IOException) {
        println("test.properties file not present.")
        mapOf()
    }

}
val dataPlaneUrl
    get() = properties.getOrDefault(DATA_PLANE_URL_KEY, "")
val controlPlaneUrl
    get() = properties.getOrDefault(CONTROL_PLANE_URL_KEY, "")
val writeKey
    get() = properties.getOrDefault(WRITE_KEY_KEY, "")
