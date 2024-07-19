/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
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

package com.rudderstack.rudderjsonadapter

import java.lang.reflect.ParameterizedType

/**
 * A abstract class to be constructed with type variable T.
 * Implementation of this class helps in determining the type at runtime
 *
 * @param T The generic type to be determined
 */
abstract class RudderTypeAdapter<T> {
    val type
        get() = (this::class.java.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(0)
    companion object {
        /**
         * For ease of instantiation
         * ```
         * RudderTypeAdapter<SomeClass>{}
         * ```
         *
         * @param T the Type or Class on which RudderTypeAdapter will act
         * @param body Empty body to facilitate. Not used
         * @return [RudderTypeAdapter]
         */
        inline operator fun<T> invoke(crossinline body: () -> Unit): RudderTypeAdapter<T> =
            object : RudderTypeAdapter<T>() {}
    }
}
