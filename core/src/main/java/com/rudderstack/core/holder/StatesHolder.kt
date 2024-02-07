/*
 * Creator: Debanjan Chatterjee on 24/01/24, 11:39 am Last modified: 24/01/24, 11:39 am
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

package com.rudderstack.core.holder
import com.rudderstack.core.State
import com.rudderstack.core.Analytics
import com.rudderstack.core.Controller

/**
 * This data is not persisted and is mainly intended to maintain transient State objects
 * associated to each analytics instance
 */

/**
 *  [State] objects are associated to their Class names, hence the only one instance of a particular
 *  [State] can be associated to an [Analytics] instance.
 *
 * @param state
 */
fun Controller.associateState(state: State<*>){
    store(state.javaClass.name, state)
}
inline fun <reified T : State<*>> Controller.removeState(){
    remove(T::class.java.name)
}

inline fun <reified T : State<*>> Controller.retrieveState() : T?{
    return retrieve(T::class.java.name)
}