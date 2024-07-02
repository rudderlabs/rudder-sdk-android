package com.rudderstack.core.internal.plugins

import com.rudderstack.core.models.*
import com.rudderstack.core.DestinationPlugin
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderOption
import com.rudderstack.core.minusWrtKeys
import com.rudderstack.core.models.updateWith

/**
 * Alters flow and adds values to [Message] depending on options.
 * Manipulates the list of destination plugins based on options
 *
 * Individual plugin option takes precedence over "All"
 * For eg. If "All" is set to true, but a certain integration is set to false,
 * that integration will be dumped and not used.
 * Likewise if "All" is set to false but a particular integration is set to true, we use it.
 *
 * In case of [MessageContext] values of [Message.context] and [RudderOption] are merged together.
 *
 * @param options
 */
internal class RudderOptionPlugin(private val options: RudderOption) : Plugin {

    override fun intercept(chain: Plugin.Chain): Message {
        val msg = chain.message().let { oldMsg ->
            oldMsg.copy(
                context = oldMsg.updateContext(options),
            )
        }
        val validIntegrations = validIntegrations()
        msg.integrations = validIntegrations
        return chain.plugins.takeIf {
            it.isNotEmpty()
        }?.let { plugins ->
            val validPlugins = filterWithAllowedDestinationPlugins(
                plugins,
                msg.integrations ?: mapOf()
            )
            return chain.with(validPlugins).proceed(msg)
        } ?: chain.proceed(msg)

    }

    /**
     * This filter approves any plugin that's either not a destination plugin, or is
     * an allowed destination
     *
     * @param plugins the plugin list to filter
     * @param integrations the map that contains relevant information
     * @return the valid [Plugin] list
     */
    private fun filterWithAllowedDestinationPlugins(
        plugins: List<Plugin>,
        integrations: Map<String, Boolean>
    ): List<Plugin> {
        return plugins.filter {
            it !is DestinationPlugin<*> || (integrations[it.name] ?: integrations["All"] ?:true)
        }

    }

    private fun validIntegrations(): Map<String, Boolean> {
        return options.integrations.ifEmpty { mapOf("All" to true) }

    }

    private fun Message.updateContext(options: RudderOption): MessageContext? {
        //external ids can be present in both message or options.
        //save concatenated external ids, if present with both message and options
        val messageExternalIds = context?.externalIds
        val updatedExternalIds: List<Map<String, String>>? =
            when {
                options.externalIds.isEmpty() -> {
                    messageExternalIds
                }
                messageExternalIds == null -> {
                    options.externalIds
                }
                else -> {//preference is given to external ids in Message
                    val extraIdsInOptions =
                        options.externalIds minusWrtKeys messageExternalIds
                    //adding these to messageExternalIds gives our required ids
                    messageExternalIds + extraIdsInOptions
                }
            }

        return context?.updateWith(externalIds = updatedExternalIds)
    }
}
