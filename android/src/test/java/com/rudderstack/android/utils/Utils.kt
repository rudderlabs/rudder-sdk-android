/*
 * Creator: Debanjan Chatterjee on 27/12/23, 11:41 am Last modified: 27/12/23, 11:41 am
 * Copyright: All rights reserved Ⓒ 2023 http://rudderstack.com
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

package com.rudderstack.android.utils

fun busyWait(millis: Long) {
    val start = System.currentTimeMillis()
    while (System.currentTimeMillis() - start < millis) {
        // busy wait
    }
}