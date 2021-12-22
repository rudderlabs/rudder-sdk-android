/*
 * Creator: Debanjan Chatterjee on 06/11/21, 7:19 PM Last modified: 06/11/21, 7:19 PM
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

package com.rudderstack.android.core.plugin

import com.rudderstack.android.models.Message

fun interface Plugin {

    interface Chain {
        fun message(): Message
        fun proceed(message: Message): Message
    }
    /*companion object {
        */
    /**
     * Constructs an interceptor for a lambda. This compact syntax is most useful for inline
     * interceptors.
     *
     * ```kotlin
     * val interceptor = Interceptor { chain: Interceptor.Chain ->
     *     chain.proceed(chain.request())
     * }
     * ```
     *//*
        inline operator fun invoke(crossinline block: (chain: Chain) -> Message): Plugin =
            Plugin { block(it) }
    }*/
    fun intercept(chain: Chain): Message

}