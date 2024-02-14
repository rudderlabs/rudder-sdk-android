/*
 * Creator: Debanjan Chatterjee on 08/02/24, 7:29 pm Last modified: 08/02/24, 7:29 pm
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
import com.rudderstack.core.InfrastructurePlugin
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)

class FlushPolicyExtensionTests {

    @Mock
    private lateinit var controller: Controller

    @Mock
    private lateinit var flushPolicy1: FlushPolicy

    @Mock
    private lateinit var flushPolicy2: FlushPolicy

    @Test
    fun testAddFlushPolicies() {
        controller.addFlushPolicies(flushPolicy1, flushPolicy2)
        verify(controller).addInfrastructurePlugin(flushPolicy1, flushPolicy2)
    }

    @Test
    fun testSetFlushPolicies() {

        whenever(controller.applyInfrastructureClosure(any())).thenAnswer {
            val closure = it.getArgument<(InfrastructurePlugin.() -> Unit)>(0)
            closure(flushPolicy1)
            closure(flushPolicy2)
        }
        controller.setFlushPolicies(flushPolicy1, flushPolicy2)
        verify(controller).removeInfrastructurePlugin(flushPolicy1)
        verify(controller).removeInfrastructurePlugin(flushPolicy2)
        verify(controller).addFlushPolicies(flushPolicy1, flushPolicy2)
    }

    @Test
    fun testRemoveAllFlushPolicies() {
        whenever(controller.applyInfrastructureClosure(any())).thenAnswer {
            val closure = it.getArgument<(InfrastructurePlugin.() -> Unit)>(0)
            closure(flushPolicy1)
            closure(flushPolicy2)
        }
        controller.removeAllFlushPolicies()
        verify(controller).applyInfrastructureClosure(any())
        verify(controller).removeInfrastructurePlugin(flushPolicy1)
        verify(controller).removeInfrastructurePlugin(flushPolicy2)
    }

    @Test
    fun testRemoveFlushPolicy() {
        controller.removeFlushPolicy(flushPolicy1)
        verify(controller).removeInfrastructurePlugin(flushPolicy1)
    }

    @Test
    fun testApplyFlushPoliciesClosure() {
        controller.applyFlushPoliciesClosure { onRemoved() }
        verify(controller).applyInfrastructureClosure(any())
    }

}