/*
 * Creator: Debanjan Chatterjee on 30/01/24, 6:48 pm Last modified: 30/01/24, 6:48 pm
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

package com.rudderstack.core.flushpolicy

import com.rudderstack.core.Controller

fun Controller.addFlushPolicies(vararg flushPolicies: FlushPolicy) {
    addInfrastructurePlugin(*flushPolicies)
}
fun Controller.setFlushPolicies(vararg flushPolicies: FlushPolicy) {
    synchronized(this) {
        removeAllFlushPolicies()
        addFlushPolicies(*flushPolicies)
    }
}
fun Controller.removeAllFlushPolicies() {
    applyInfrastructureClosure{
        if(this is FlushPolicy) {
            onRemoved()
            removeInfrastructurePlugin(this)
        }
    }
}
fun Controller.removeFlushPolicy(flushPolicy: FlushPolicy) {
    removeInfrastructurePlugin(flushPolicy)
}

fun Controller.applyFlushPoliciesClosure(closure : FlushPolicy.() -> Unit){
    applyInfrastructureClosure {
        if (this is FlushPolicy)
            this.closure()
    }
}