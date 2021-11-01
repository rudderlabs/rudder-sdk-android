/*
 * Creator: Debanjan Chatterjee on 30/10/21, 8:13 PM Last modified: 30/10/21, 8:13 PM
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

package com.rudderstack.android.core.state.internal

import java.lang.ref.WeakReference

/**
 * Base class for specific grouped configuration.
 * The value can be observed by adding observers.
 * Observers are added as weak reference to counter memory leaks
 *
 * @param T The type of value the class holds
 * @constructor
 *
 *
 * @param initialValue An initial value can be supplied, which defaults to null
 */
abstract class State<T>(initialValue: T? = null) {


    private var _value: T? = initialValue
    set(value) {
        field = value
        // notifies observers as state changes. Initial value won't be notified
        observers.forEach {
            it.get()?.onStateChange(value)
        }
    }
    val value: T?
        get() = _value

    private val observers: MutableSet<WeakReference<Observer<T>>> = HashSet()

    /**
     * Observe the value change
     *
     * @see Observer
     * @param observer an instance of Observer
     */
    internal fun subscribe(observer: Observer<T>) {
        observers.add(WeakReference(observer))
        observer.onStateChange(value)
    }

    /**
     * update the value of the state.
     * This value is going to be notified to all listed observers
     *
     * @param value New value of state
     */
    internal fun update(value : T?){
        this._value = value
    }

    internal fun removeObserver(observer: Observer<T>){
        observers.removeIf {
            //remove if observer ref is removed or observer is same as given one
            it.get()?.equals(observer)?:true
        }
    }

    /**
     * Observer interface for State
     *
     * @param T Type of value State holds
     */
    interface Observer<T> {
        fun onStateChange(state: T?)
    }
}