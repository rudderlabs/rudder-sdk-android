/*
 * Creator: Debanjan Chatterjee on 11/01/22, 12:10 PM Last modified: 11/01/22, 12:10 PM
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.DataUploadService
import com.rudderstack.android.core.Settings
import com.rudderstack.android.core.State
import com.rudderstack.android.core.Utils
import com.rudderstack.android.core.internal.states.SettingsState
import com.rudderstack.android.models.Message
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import com.rudderstack.android.rudderjsonadapter.RudderTypeAdapter
import com.rudderstack.android.web.WebServiceFactory
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

internal class DataUploadServiceImpl(
    writeKey : String,
    private val jsonAdapter: JsonAdapter,
    private val settingsState: State<Settings> = SettingsState,
    dataPlaneUrl: String,
    private val networkExecutor: ExecutorService = Executors.newCachedThreadPool(),

) : DataUploadService {

    private val webService = WebServiceFactory.getWebService(
        dataPlaneUrl,
        jsonAdapter, executor = networkExecutor
    )
    private val encodedWriteKey = Base64.getEncoder().encodeToString(
        String.format(Locale.US, "%s:", writeKey).toByteArray(charset("UTF-8"))
    )


    override fun upload(data: List<Message>, extraInfo: Map<String,String>?, callback: (success: Boolean) -> Unit) {
        if(networkExecutor.isShutdown || networkExecutor.isTerminated)
            return
        val batchBody = mapOf<String, Any>("sentAt" to Utils.timeStamp,
        "batch" to data).let {
            if(extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
        }
            .let {
            jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String,Any>>(){})
        }
        webService.post(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to
            String.format(Locale.US, "Basic %s", encodedWriteKey),
        "anonymousId" to (settingsState.value?.anonymousId?.let {  Base64.getEncoder().encodeToString(
            it.toByteArray(charset("UTF-8"))
        )}?:encodedWriteKey)),null, batchBody, "v1/batch", String::class.java ){
            println(it)
            callback.invoke(it.status in 200..299)
        }
    }

    override fun uploadSync(data: List<Message>, extraInfo: Map<String, String>?): Boolean {
        val batchBody = mapOf<String, Any>("sentAt" to Utils.timeStamp,
            "batch" to data).let {
            if(extraInfo.isNullOrEmpty()) it else it + extraInfo //adding extra info data
        }
            .let {
                jsonAdapter.writeToJson(it, object : RudderTypeAdapter<Map<String,Any>>(){})
            }
        val response = webService.post(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to
                    String.format(Locale.US, "Basic %s", encodedWriteKey),
            "anonymousId" to (settingsState.value?.anonymousId?.let {  Base64.getEncoder().encodeToString(
                it.toByteArray(charset("UTF-8"))
            )}?:encodedWriteKey)),null, batchBody, "v1/batch", String::class.java ).get()
        return response.status in 200..299
    }

    override fun shutdown() {
        networkExecutor.shutdown()
    }

    override val isShutdown: Boolean
        get() = networkExecutor.isShutdown || networkExecutor.isTerminated


}