/*
 * Creator: Debanjan Chatterjee on 28/10/21, 8:15 PM Last modified: 28/10/21, 7:44 PM
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

@file:JvmName("RudderClient")

package com.rudderstack.android.android

import android.app.Application

object RudderClient {

    private var application: Application? = null
    private val mApplication: Application
        get() = application ?: throw IllegalStateException("RudderClient has not been initialized")

    private var mAdvertisingId : String? = null
    private var mAnonymousId: String? = null

    init {
        //initialized
//        RudderLogger.logVerbose("RudderClient: constructor invoked.")

    }


}
