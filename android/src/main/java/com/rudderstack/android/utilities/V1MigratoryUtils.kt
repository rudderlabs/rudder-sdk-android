/*
 * Creator: Debanjan Chatterjee on 14/02/24, 11:14 am Last modified: 14/02/24, 11:14 am
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

package com.rudderstack.android.utilities

import android.content.Context
import com.rudderstack.android.storage.fileExists
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * The methods in this class are used to migrate data from V1 SDK to V2 SDK
 */

internal fun Context.isV1SavedServerConfigContainsSourceId(
    serverConfigFileName: String, newSourceId: String
): Boolean {
    //check if v1 source config exists
    if (!fileExists(this, serverConfigFileName)) return false
    //if exists, read the source id from the file
    // it's not possible to read it using ObjectOutputStream as the uid won't match.
    //We will try parsing it's byte and check if it contains the source id
    try {
        openFileInput(serverConfigFileName).use { fis ->
                ByteArrayOutputStream().use { outputStream ->
                    val bufLen = 4 * 0x400 // 4KB
                    val buf = ByteArray(bufLen)
                    var readLen: Int
                    while (fis.read(buf, 0, bufLen).also { readLen = it } != -1) {
                        outputStream.write(buf, 0, readLen)
                    }
                    return String(outputStream.toByteArray(), StandardCharsets.UTF_8).contains(
                        newSourceId
                    )
                }
            }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}



