package com.rudderstack.core

import com.rudderstack.core.models.Message
import com.rudderstack.web.HttpResponse

/**
 * Class to handle data upload to server.
 * Pass this instance to [Analytics] constructor to enable data upload.
 * If added again using [Analytics.addInfrastructurePlugin] method,
 * data will be sent through all instances implementing this interface.
 *
 */
interface DataUploadService : InfrastructurePlugin {

    fun addHeaders(headers: Map<String, String>)

    /**
     * Uploads data to cloud
     *
     * @param data The list of messages to upload
     * @param extraInfo If any data needs to be added to the body
     * @param callback Callback providing either success or failure status of upload
     */
    fun upload(
        data: List<Message>,
        extraInfo: Map<String, String>? = null,
        callback: (response: HttpResponse<out Any>) -> Unit
    )

    /**
     * Uploads data synchronously
     *
     * @param data The list of messages to upload
     * @param extraInfo If any data needs to be added to the body
     * @return status of upload, true if success, false otherwise
     */
    fun uploadSync(data: List<Message>, extraInfo: Map<String, String>? = null):
            HttpResponse<out Any>?

}
