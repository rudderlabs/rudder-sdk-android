/*
 * Creator: Debanjan Chatterjee on 20/11/23, 6:04 pm Last modified: 20/11/23, 4:02 pm
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
package com.rudderstack.android.navigationplugin

import androidx.navigation.NavController
import com.rudderstack.android.currentConfigurationAndroid
import com.rudderstack.android.navigationplugin.internal.NavControllerState
import com.rudderstack.android.navigationplugin.internal.NavigationPlugin
import com.rudderstack.core.Analytics

/**
 * Lifecycle events to be used for tracking app lifecycle events
 */
private val navControllerState: NavControllerState by lazy {
    NavControllerState()
}
private var navigationPlugin: NavigationPlugin? = null

/**
 * Tracks lifecycle events for the given [NavController]
 * example code for Compose navigation:
 * ```
 * @Composable
 * fun SunflowerApp() {
 *     val navController = rememberNavController()
 *     LaunchedEffect("first_launch") {
 *         trackLifecycle(navController)
 *     }
 *     SunFlowerNavHost(
 *         navController = navController
 *     )
 * }
 * ```
 * Example code for Fragment navigation:
 * ```
 *    override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         val binding = ActivityMainBinding.inflate(layoutInflater)
 *         setContentView(binding.root)
 *
 *         // Get the navigation host fragment from this Activity
 *         val navHostFragment = supportFragmentManager
 *             .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
 *         // Instantiate the navController using the NavHostFragment
 *         navController = navHostFragment.navController
 *         trackLifecycle(navController)
 *     }
 * ```
 * In case multiple [NavController]s are used, call this method for each of them.
 * To stop tracking lifecycle events for a [NavController], call [removeLifecycleTracking]
 *
 * @param navController : [NavController] to be tracked
 */
fun Analytics.trackLifecycle(navController: NavController) {
    if(currentConfigurationAndroid?.recordScreenViews != true ||
        currentConfigurationAndroid?.trackLifecycleEvents != true) return

    navControllerState.update(
        navControllerState.value?.plus(navController)?: setOf(navController)
    )
    if (navigationPlugin == null) {
        navigationPlugin = NavigationPlugin(navControllerState).also {
            addInfrastructurePlugin(it)
        }
    }
}

/**
 * To stop tracking lifecycle events for a [NavController], call this method.
 *
 * @param navController : [NavController] to be removed from tracking
 */
fun Analytics.removeLifecycleTracking(navController: NavController) {
    navControllerState.update(
        navControllerState.value?.minus(navController)
    )
}