package com.vagabond.testcommon

import com.rudderstack.core.Analytics
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.models.Message
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

    override fun setup(analytics: Analytics) {}

    override fun shutdown() {
        headers = mutableMapOf()
    }
}
