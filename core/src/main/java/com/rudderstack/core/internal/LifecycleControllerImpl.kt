package com.rudderstack.core.internal

import com.rudderstack.core.LifecycleController
import com.rudderstack.core.Plugin
import com.rudderstack.core.models.Message

/**
 * LCC implementation that processes a message through it's lifetime
 *  @see LifecycleController
 * @property message The associated message
 * @property plugins The plugins that will work on the Message.
 */
internal class LifecycleControllerImpl(
    override val message: Message,
    override val plugins: List<Plugin>
) : LifecycleController {
    override fun process() {
        val centralPluginChain = CentralPluginChain(message, plugins, originalMessage = message)
        centralPluginChain.proceed(message)
    }
}
