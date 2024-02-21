/*
 * Creator: Debanjan Chatterjee on 14/02/24, 12:15 pm Last modified: 14/02/24, 12:15 pm
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

import android.content.Context
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.core.Logger
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

//file access
/**
 * Saves a serializable object in file
 *
 * @param T
 * @param obj
 * @param context
 * @param fileName
 * @return
 */
internal fun <T : Serializable> saveObject(
    obj: T, context: Context, fileName: String, logger: Logger? = AndroidLogger
): Boolean {
    try {
        val fos: FileOutputStream = context.openFileOutput(
            fileName, Context.MODE_PRIVATE
        )
        val os = ObjectOutputStream(fos)
        os.writeObject(obj)
        os.close()
        fos.close()
        return true
    } catch (e: Exception) {
        logger?.error(
            log = "save object: Exception while saving Object to File", throwable = e
        )
        e.printStackTrace()
    }
    return false
}

/**
 *
 *
 * @param T
 * @param context
 * @param fileName
 * @return
 */
internal fun <T : Serializable> getObject(
    context: Context, fileName: String, logger: Logger? = AndroidLogger
): T? {
    try {
        val file = context.getFileStreamPath(fileName)
        if (file != null && file.exists()) {
            val fis = context.openFileInput(fileName)
            val `is` = ObjectInputStream(fis)
            val obj = `is`.readObject() as? T?
            `is`.close()
            fis.close()
            return obj
        }
    } catch (e: Exception) {
        logger?.error(
            log = "getObject: Failed to read Object from File", throwable = e
        )
        e.printStackTrace()
    }
    return null
}
internal fun fileExists(context: Context, filename: String): Boolean {
    val file = context.getFileStreamPath(filename)
    return file != null && file.exists()
}