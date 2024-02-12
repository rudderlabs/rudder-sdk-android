/*
 * Creator: Debanjan Chatterjee on 29/12/21, 5:38 PM Last modified: 24/11/21, 11:18 PM
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

package com.rudderstack.core

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
        synchronized(this) {
            val oldValue = field
            field = value
            // notifies observers as state changes. Initial value won't be notified
            observers.forEach {
                it.get()?.onStateChange(value, oldValue)
            }
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
     fun subscribe(observer: Observer<T>) {
        synchronized(this){
            if (observers.firstOrNull {
                    it.get()?.equals(observer) == true
            } != null) return
            observers.add(WeakReference(observer))
            observer.onStateChange(value, null)
        }
    }

    /**
     * update the value of the state.
     * This value is going to be notified to all listed observers
     *
     * @param value New value of state
     */
    fun update(value : T?){
        this._value = value
    }

    fun removeObserver(observer: Observer<T>){
        synchronized(this) {
            observers.removeAll(observers.filter{
                //remove if observer ref is removed or observer is same as given one
                it.get()?.equals(observer) ?: true
            }.toSet())
        }
    }
    fun removeAllObservers(){
        synchronized(this) {
            observers.clear()
        }
    }

    /**
     * Observer interface for State
     *
     * @param T Type of value State holds
     */
    fun interface Observer<T> {
        /**
         * Called when state changes
         *
         * @param state New state
         * @param previousState Old state - Not available if subscribed for first time
         */
        fun onStateChange(state: T?, previousState: T?)
    }
}