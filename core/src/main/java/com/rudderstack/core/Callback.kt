package com.rudderstack.core

import com.rudderstack.core.models.Message

/**
 * Callback invoked when the client library is done processing a message.
 *
 *
 * Methods may be called on background threads, implementations must implement their own
 * synchronization if needed. Implementations should also take care to make the methods
 * non-blocking.
 */
interface Callback {
    /**
     * Invoked when the message is successfully uploaded to Rudder.
     *
     *
     * Note: The Rudder HTTP API itself is asynchronous, so this doesn't indicate whether the
     * message was sent to all integrations or not — just that the message was sent to the Rudder API
     * and will be sent to integrations at a later time.
     */
    fun success(message: Message?)

    /**
     * Invoked when the library fails sending a message. The message is still stored in DB
     *
     *
     * This could be due to exhausting retries, or other unexpected errors. Use the `throwable` provided to take further action.
     */
    fun failure(message: Message?, throwable: Throwable?)
}
