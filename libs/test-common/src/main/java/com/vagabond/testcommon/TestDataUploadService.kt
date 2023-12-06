/*
 * Creator: Debanjan Chatterjee on 05/12/23, 3:45 pm Last modified: 05/12/23, 3:45 pm
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.vagabond.testcommon

import com.rudderstack.core.DataUploadService
import com.rudderstack.models.Message
import com.rudderstack.web.HttpResponse

class TestDataUploadService : DataUploadService {
    private var headers = mutableMapOf<String, String>()

    var mockUploadStatus = 200
    var mockUploadBody = "OK"
    var errorBody : String? = null
    var error: Throwable? = null
    override fun addHeaders(headers: Map<String, String>) {
        this.headers += headers
    }

    override fun upload(
        data: List<Message>,
        extraInfo: Map<String, String>?,
        callback: (response: HttpResponse<out Any>) -> Unit
    ) {
        callback(HttpResponse(mockUploadStatus, mockUploadBody, errorBody, error))
    }

    override fun uploadSync(
        data: List<Message>, extraInfo: Map<String, String>?
    ): HttpResponse<out Any>? {
        return HttpResponse(mockUploadStatus, mockUploadBody, errorBody, error)
    }

    override fun shutdown() {
        headers = mutableMapOf()
    }
}