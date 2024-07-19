package com.rudderstack.android.internal.plugins

import com.rudderstack.android.utilities.androidStorage
import com.rudderstack.android.utilities.contextState
import com.rudderstack.android.utilities.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.MissingPropertiesException
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.*

/**
 * Fill the defaults for a [Message]
 * In case a message contains traits, external ids, custom contexts, this will override
 * the values in storage for the same. That means if message contains traits
 * {a:b, c:d} and saved traits contain {c:e, g:h, i:j}, the resultant will be
 * that of the message, i.e {a:b, c:d}. This is applicable to traits, external ids and custom contexts
 * The default context will be added irrespectively.
 * In case default context contains externalIds/traits/custom contexts that are common with
 * message external ids/traits/custom contexts respectively, the values will be amalgamated with
 * preference given to those belonging to message, in case keys match.
 * This plugin also adds the userId and anonymousId to the message, if not present.
 * this plugin also changes the channel for the messages to android
 *
 */
internal class FillDefaultsPlugin : Plugin {

    override lateinit var analytics: Analytics

    /**
     * Fill default details for [Message]
     * If message contains context, this will replace the ones present
     * @throws [MissingPropertiesException] if neither of userId or anonymous id is present
     */
    @Throws(MissingPropertiesException::class)
    private inline fun <reified T : Message> T.withDefaults(): T {
        val anonId = this.anonymousId ?: analytics.currentConfigurationAndroid?.anonymousId
        val userId = this.userId ?: analytics.androidStorage.userId
        if (anonId.isNullOrEmpty() && userId.isEmpty()) {
            val ex = MissingPropertiesException("Either Anonymous Id or User Id must be present")
            analytics.currentConfigurationAndroid?.logger?.error(
                log = "Missing both anonymous Id and user Id. Use settings to update " + "anonymous id in Analytics constructor",
                throwable = ex
            )
            throw ex
        }
        //copying top level context to message context
        val newContext =
            // in case of alias we purposefully remove traits from context
            analytics.contextState?.value?.let {
                if (this is AliasMessage && this.userId != analytics.androidStorage.userId) it.updateWith(
                    traits = mapOf()
                ) else it
            } selectiveReplace context.let {
                if (this !is IdentifyMessage) {
                    // remove any external ids present in the message
                    // this is in accordance to v1
                    it?.withExternalIdsRemoved()
                } else it
            }

        return (this.copy(
            context = newContext,
            anonymousId = anonId,
            userId = userId.ifEmpty { null }
        ) as T)
    }

    private infix fun MessageContext?.selectiveReplace(context: MessageContext?): MessageContext? {
        if (this == null) return context else if (context == null) return this
        return this.updateWith(context)
    }

    override fun intercept(chain: Plugin.Chain): Message {
        val message = chain.message().withDefaults()
        return chain.proceed(message)
    }
}
