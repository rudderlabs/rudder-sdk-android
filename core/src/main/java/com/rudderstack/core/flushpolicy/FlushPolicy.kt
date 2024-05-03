/*
 * Creator: Debanjan Chatterjee on 31/01/24, 12:09 pm Last modified: 31/01/24, 9:49 am
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

import com.rudderstack.core.Analytics
import com.rudderstack.core.InfrastructurePlugin

interface FlushPolicy : InfrastructurePlugin {
    fun reschedule()
    fun onRemoved()
    fun setFlush(flush: Analytics.() -> Unit)
}