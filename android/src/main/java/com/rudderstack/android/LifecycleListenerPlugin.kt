/*
 * Creator: Debanjan Chatterjee on 21/11/23, 12:13 pm Last modified: 21/11/23, 12:13 pm
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

package com.rudderstack.android

import android.app.Activity
import com.rudderstack.core.InfrastructurePlugin
import com.rudderstack.core.Plugin

/**
 * Any [InfrastructurePlugin] or [Plugin] that wants to listen to lifecycle events should implement
 * this interface.
 *
 * onAppForegrounded() and onAppBackgrounded() are called when the app goes to foreground and background respectively.
 * These methods are called only if [ConfigurationAndroid.trackLifecycleEvents] is set to true.
 *
 * onActivityStarted(), onActivityStopped() and screenChange() are called when an activity is
 * started and stopped respectively.
 * These methods are called only if both [ConfigurationAndroid.trackLifecycleEvents] and
 * [ConfigurationAndroid.recordScreenViews] are set to true.
 *
 * Getting callbacks is subject to availability of Infrastructure plugins that are designed
 * to listen to lifecycle events.
 *
 */
interface LifecycleListenerPlugin {
    /**
     * Called when at least one activity is started.
     * Called first.
     *
     */
    fun onAppForegrounded(){}
    /**
     * Called when all activities are stopped.
     * The last callback to be fired.
     *
     */
    fun onAppBackgrounded(){}

    /**
     * Called when an activity is started.
     * This is called after [setCurrentActivity]
     *
     *
     * @param activityName The name of the activity that is started
     */
    fun onActivityStarted(activityName: String){}
    /**
     * Called when the current activity is changed
     * Will be null if the app is backgrounded
     * If using this method, make sure to make it thread safe.
     * Storing the activity as a strong reference might lead to memory leaks.
     * If you're only interested in the name of the activity, use [onActivityStarted] instead.
     * Called after [onAppForegrounded]
     *
     * @param activity The activity that is started
     */
    fun setCurrentActivity(activity: Activity?){}

    /**
     * Called when an activity is stopped
     * Called before [onAppBackgrounded]
     * @param activityName The name of the activity that is stopped
     */
    fun onActivityStopped(activityName: String){}

    /**
     * Called when the screen changes.
     * This is mostly associated to fragment or compose navigation.
     *
     *
     * @param name Name of the compose route or fragment label
     * @param arguments The arguments passed to the to the composable or fragment in question.
     */
    fun onScreenChange(name: String, arguments: Map<String, Any>?){}
}