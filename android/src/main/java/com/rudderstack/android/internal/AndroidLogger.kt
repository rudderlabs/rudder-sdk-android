/*
 * Creator: Debanjan Chatterjee on 28/04/22, 12:26 AM Last modified: 28/04/22, 12:26 AM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.android.internal

import android.util.Log
import com.rudderstack.core.Logger

/**
 * Logger implementation specifically for android.
 *
 */
internal class AndroidLogger : Logger {
    private var _logLevel : Logger.LogLevel = Logger.LogLevel.Info
    override fun activate(level: Logger.LogLevel) {
        _logLevel = level
    }

    override fun info(tag: String, log: String) {
        if(Logger.LogLevel.Info >= _logLevel)
            Log.i(tag,log)
    }

    override fun debug(tag: String, log: String) {
        if(Logger.LogLevel.Debug >= _logLevel)
            Log.d(tag,log)
    }

    override fun warn(tag: String, log: String) {
        if(Logger.LogLevel.Warn >= _logLevel)
            Log.w(tag,log)
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if(Logger.LogLevel.Error >= _logLevel)
            Log.e(tag,log, throwable)
    }
}