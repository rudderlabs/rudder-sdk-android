/*
 * Creator: Debanjan Chatterjee on 07/03/24, 11:49 am Last modified: 07/03/24, 11:49 am
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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
@file:JvmName("ConfigurationActions")
package com.rudderstack.android

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.rudderstack.android.internal.AndroidLogger
import com.rudderstack.android.storage.AndroidStorage
import com.rudderstack.core.Analytics
import com.rudderstack.core.ConfigurationScope

fun Analytics.applyConfigurationAndroid(scope: ConfigurationAndroidScope.() ->
Unit){
    currentConfigurationAndroid?.let {
        val configurationAndroidScope = ConfigurationAndroidScope(it)
        applyConfigurationInternal(configurationAndroidScope, scope)
    }?:currentConfiguration?.rudderLogger?.error(log = "No Configuration found!!")
}
val Analytics.currentConfigurationAndroid: ConfigurationAndroid?
    get() = (currentConfiguration as? ConfigurationAndroid)

internal fun Application.initialConfigurationAndroid(storage: AndroidStorage):
        ConfigurationAndroid {
    return initialConfigurationAndroid(storage, true)
}
@VisibleForTesting
internal fun Application.initialConfigurationAndroid(storage: AndroidStorage, shouldVerifySdk: Boolean):
        ConfigurationAndroid {
    return ConfigurationAndroid(application = this, trackAutoSession = storage.trackAutoSession,
        rudderLogger = AndroidLogger(), shouldVerifySdk = shouldVerifySdk)
}