/*
 * Creator: Debanjan Chatterjee on 02/02/22, 11:45 PM Last modified: 02/02/22, 11:45 PM
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

package com.rudderstack.core.internal

import com.rudderstack.core.RudderLogger

class KotlinLogger(initialLogLevel: RudderLogger.LogLevel = RudderLogger.LogLevel.NONE) : RudderLogger {

    private var logLevel = initialLogLevel
        @Synchronized set
        @Synchronized get

    override fun activate(level: RudderLogger.LogLevel) {
        logLevel = level
    }

    override fun info(tag: String, log: String) {
        if (RudderLogger.LogLevel.INFO >= logLevel)
            println("$tag-info : $log")
    }

    override fun debug(tag: String, log: String) {
        if (RudderLogger.LogLevel.DEBUG >= logLevel)
            println("$tag-debug : $log")
    }

    override fun warn(tag: String, log: String) {
        if (RudderLogger.LogLevel.WARN >= logLevel)
            println("$tag-warn : $log")
    }

    override fun error(tag: String, log: String, throwable: Throwable?) {
        if (RudderLogger.LogLevel.ERROR >= logLevel)
            println("$tag-error : $log")
    }

    override val level: RudderLogger.LogLevel
        get() = logLevel
}
