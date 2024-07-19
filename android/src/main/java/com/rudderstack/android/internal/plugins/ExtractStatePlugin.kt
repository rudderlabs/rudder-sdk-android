package com.rudderstack.android.internal.plugins

import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.android.utilities.processNewContext
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.AliasMessage
import com.rudderstack.core.models.IdentifyMessage
import com.rudderstack.core.models.Message
import com.rudderstack.core.models.MessageContext
import com.rudderstack.core.models.optAddContext
import com.rudderstack.core.models.traits
import com.rudderstack.core.models.updateWith
import com.rudderstack.core.optAdd

/**
 * Mutates the system state, if required, based on the Event.
 * In case of [IdentifyMessage], it is expected to save the traits provided.
 */
internal class ExtractStatePlugin : Plugin {

    override lateinit var analytics: Analytics

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message()

        if (!(message is IdentifyMessage || message is AliasMessage)) {
            return chain.proceed(message)
        }
        // alias message can change user id permanently
        //save and update traits
        //update userId
        //save and update external ids
        (message.context ?: return message).let {
            if (it.traits?.get("anonymousId") == null)
                analytics.currentConfigurationAndroid?.anonymousId.let { anonId ->
                    it.updateWith(traits = it.traits optAdd ("anonymousId" to anonId))
                } else it
        }.let {

            //alias and identify messages are expected to contain user id.
            //We check in context as well as context.traits with either keys "userId" and "id"
            //user id can be retrieved if put directly in context or context.traits with the
            //aforementioned ids
            val newUserId = getUserId(message)
            analytics.logger.debug(log = "New user id detected: $newUserId")
            val prevId = analytics.androidStorage.userId.ifEmpty {
                analytics.currentConfigurationAndroid?.anonymousId
            }
            // in case of identify, the stored traits (if any) are replaced by the ones provided
            // if user id is different. else traits are added to it
            val msg = when (message) {
                is AliasMessage -> {
                    // in case of alias, we change the user id in traits
                    newUserId?.let { newId ->
                        updateNewAndPrevUserIdInContext(
                            newId, it
                        )
                    }?.let {
                        replaceContext(it)
                        message.copy(context = it, userId = newUserId, previousId = prevId)
                    }
                }

                is IdentifyMessage -> {
                    val updatedContext = if (newUserId != prevId) {
                        it
                    } else {
                        appendContextForIdentify(it)
                    }
                    replaceContext(updatedContext)
                    message.copy(context = updatedContext)
                }

                else -> {
                    message
                }
            } ?: message
            msg.also {
                newUserId?.let { id ->
                    analytics.androidStorage.setUserId(id)
                }
            }
            return chain.proceed(msg)

        }
    }

    private fun appendContextForIdentify(messageContext: MessageContext): MessageContext {
        return analytics.contextState?.value?.let { savedContext ->
            messageContext optAddContext savedContext
        } ?: messageContext
    }

    private fun replaceContext(messageContext: MessageContext) {
        analytics.processNewContext(messageContext)
    }


    /**
     * Checks in the order
     * "user_id" key at root
     * "user_id" key at context.traits
     * "userId" key at root
     * "userId" key at context.traits
     * "id" key at root
     * "id" key at context.traits
     *
     *
     */
    private fun getUserId(message: Message): String? {
        return message.context?.let {
            (it[KeyConstants.CONTEXT_USER_ID_KEY]
                ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY))
                ?: (it[KeyConstants.CONTEXT_USER_ID_KEY_ALIAS])
                ?: (it.traits?.get(KeyConstants.CONTEXT_USER_ID_KEY_ALIAS))
                ?: (it[KeyConstants.CONTEXT_ID_KEY])
                ?: (it.traits?.get(KeyConstants.CONTEXT_ID_KEY)))?.toString()
        } ?: message.userId
    }

    private fun updateNewAndPrevUserIdInContext(
        newUserId: String, messageContext: MessageContext
    ): MessageContext {
        val newTraits =
            messageContext.traits optAdd mapOf(
                KeyConstants.CONTEXT_ID_KEY to newUserId, KeyConstants.CONTEXT_USER_ID_KEY to newUserId
            )

        //also in case of alias, user id in context should also change, given it's
        // present there
        return messageContext.updateWith(
            traits = newTraits
        )
    }

    object KeyConstants {
        const val CONTEXT_USER_ID_KEY = "user_id"
        const val CONTEXT_USER_ID_KEY_ALIAS = "userId"
        const val CONTEXT_ID_KEY = "id"
    }
}
