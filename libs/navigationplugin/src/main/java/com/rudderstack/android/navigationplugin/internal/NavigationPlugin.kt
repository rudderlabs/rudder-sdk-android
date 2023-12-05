/*
 * Creator: Debanjan Chatterjee on 20/11/23, 8:55 pm Last modified: 20/11/23, 6:29 pm
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

package com.rudderstack.android.navigationplugin.internal

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.rudderstack.android.LifecycleListenerPlugin
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.InfrastructurePlugin

internal class NavigationPlugin(private val navControllerState: NavControllerState) :
    InfrastructurePlugin, NavController.OnDestinationChangedListener {
    private var analytics: Analytics? = null
    private var currentNavControllers: Collection<NavController>? = null
    private val currentConfig
    get() = analytics?.currentConfigurationAndroid
    override fun setup(analytics: Analytics) {
        this.analytics = analytics
        if (currentConfig?.trackLifecycleEvents == true
            && currentConfig?.recordScreenViews == true)
            subscribeToNavControllerState()
    }

    private fun subscribeToNavControllerState() {
        navControllerState.subscribe {
            updateNavControllers(it)
        }
    }

    private fun updateNavControllers(updatedNavControllers: Collection<NavController>?) {
        synchronized(this) {
            removeDeletedNavControllers(updatedNavControllers)
            setupAddedNavControllers(updatedNavControllers)
            currentNavControllers = updatedNavControllers
        }
    }


    private fun setupAddedNavControllers(updatedNavControllers: Collection<NavController>?) {
        updatedNavControllers?.let {
            val addedNavControllers: List<NavController> =
                it.minus((currentNavControllers ?: emptyList()).toSet())
            addedNavControllers.forEach { navController ->
                navController.addOnDestinationChangedListener(this)
            }
        }
    }

    private fun removeDeletedNavControllers(updatedNavControllers: Collection<NavController>?) {
        currentNavControllers?.let {
            val deletedNavControllers: List<NavController> =
                it.minus((updatedNavControllers ?: emptyList()).toSet())
            deletedNavControllers.forEach { navController ->
                navController.removeOnDestinationChangedListener(this)
            }
        }
    }

    override fun shutdown() {
        updateNavControllers(listOf())
    }


    @Synchronized
    override fun onDestinationChanged(
        controller: NavController, destination: NavDestination, arguments: Bundle?
    ) {
        when (destination.navigatorName) {
            "fragment" -> {
                trackFragmentScreenView(destination, arguments)
            }

            "composable" -> {
                trackComposableScreenView(destination, arguments)
            }
        }
    }

    private fun trackComposableScreenView(destination: NavDestination, arguments: Bundle?) {
        if(currentConfig?.trackLifecycleEvents != true || currentConfig?.recordScreenViews != true) return
        val argumentKeys = destination.arguments.keys
        val screenName = destination.route?.let {
            if (argumentKeys.isEmpty()) it
            else {
                val argumentsIndex = it.indexOf('/')
                if (argumentsIndex == -1)  it
                else it.substring(0, argumentsIndex)
            }
        }.toString()
        broadcastScreenChange(screenName, getProperties(arguments, argumentKeys))
    }

    private fun trackFragmentScreenView(destination: NavDestination, arguments: Bundle?) {
        if(currentConfig?.trackLifecycleEvents != true || currentConfig?.recordScreenViews != true) return
        val screenName = destination.label.toString()
        val properties = getProperties(arguments, destination.arguments.keys)
        broadcastScreenChange(screenName, properties)
    }

    private fun getProperties(
        arguments: Bundle?, argumentKeys: Set<String>
    ): Map<String, Any> = arguments?.let { bundle ->
            argumentKeys.associateWith { bundle.get(it) }
        }?.filter { it.value != null }?.mapValues { it.value!! } ?: mapOf()

    private fun broadcastScreenChange(
        screenName: String, properties: Map<String, Any>?
    ) {
        analytics?.applyInfrastructureClosure {
            if (this is LifecycleListenerPlugin) {
                this.onScreenChange(screenName, properties)
            }
        }
        analytics?.applyClosure {
            if (this is LifecycleListenerPlugin) {
                this.onScreenChange(screenName, properties)
            }
        }
    }

}