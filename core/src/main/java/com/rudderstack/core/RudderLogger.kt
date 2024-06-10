/*
 * Creator: Debanjan Chatterjee on 02/02/22, 11:02 PM Last modified: 02/02/22, 11:02 PM
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

package com.rudderstack.core

/**
 * Logger interface.
 * Contains methods for different scenarios
 *
 */
interface RudderLogger {
    companion object{
        const val DEFAULT_TAG = "Rudder-Analytics"
    }

    /**
     * Activate or deactivate logger based on choice.
     *
     * @param level should log anything greater or equal to that level. See [LogLevel]
     */
    fun activate (level : LogLevel)

    fun info(tag : String = DEFAULT_TAG, log: String)

    fun debug(tag : String = DEFAULT_TAG, log: String)

    fun warn(tag : String = DEFAULT_TAG, log: String)

    fun error(tag : String = DEFAULT_TAG, log: String, throwable : Throwable? = null)

    /**
     * Level based on priority. Higher the number, greater the priority
     *
     * @property level priority for each type
     */
    enum class LogLevel{
        DEBUG,
        INFO,
        WARN,
        ERROR,
        NONE,
    }
    val level: LogLevel

    object Noob : RudderLogger{
        override fun activate(level: LogLevel) {
            // do nothing
        }

        override fun info(tag: String, log: String) {
            // do nothing
        }

        override fun debug(tag: String, log: String) {
            // do nothing
        }

        override fun warn(tag: String, log: String) {
            // do nothing
        }

        override fun error(tag: String, log: String, throwable: Throwable?) {
            // do nothing
        }

        override val level: LogLevel
            get() = LogLevel.NONE
    }
}
