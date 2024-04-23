package com.rudderstack.core.internal.plugins

import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.Storage
import com.rudderstack.models.Message

object CoreInputsPlugin : Plugin{
    private const val LIBRARY_KEY = "library"
    private val Storage.libraryContextPair
        get() = LIBRARY_KEY to mapOf("name" to libraryName, "version" to libraryVersion)
    private var storage: Storage?= null
    override fun setup(analytics: Analytics) {
        storage = analytics.storage
    }
    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()
        val context = storage?.let {
            storage -> message.context?.let { it + storage.libraryContextPair }?: mapOf(storage.libraryContextPair)
        }?: return chain.proceed(message)
        return chain.proceed(message.copy(context = context))
    }

    override fun onShutDown() {
        storage = null
    }

}