/*
 * Creator: Debanjan Chatterjee on 28/12/21, 8:15 PM Last modified: 28/12/21, 5:21 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.core.internal

import com.rudderstack.android.core.LifecycleController
import com.rudderstack.android.core.Plugin
import com.rudderstack.android.core.RudderOptions
import com.rudderstack.android.models.Message

/**
 * LCC implementation that processes a message through it's lifetime
 *  @see LifecycleController
 * @property message The associated message
 * @property options RudderOptions object for this message
 * @property plugins The plugins that will work on the Message.
 */
internal class LifecycleControllerImpl(
    override val message: Message,
    override val options: RudderOptions,
    override val plugins: List<Plugin>
) : LifecycleController {
    override fun process() {

    }
}