package com.rudderstack.core

import com.rudderstack.core.models.Message

/**
 * Handles the lifecycle of a message.
 * Most importantly aligns the plugins.
 * Acts as a bridge between application layer and internal business layer.
 * Might also be referred as LCC later on
 *
 */
interface LifecycleController {
    /**
     * Each message is connected to it's Lifecycle Controller. Returns the associated message
     */
    val message: Message

    /**
     * Separate options can be added for each message, null if there are no specific options
     *//*
    val options : RudderOptions?*/

    /**
     * Associated list of plugins
     */
    val plugins: List<Plugin>

    /**
     * The message is up for processing.
     * Plugins will be applied to it.
     *
     */
    fun process()
}
