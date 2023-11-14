/*
 * Creator: Debanjan Chatterjee on 13/11/23, 12:21 pm Last modified: 13/11/23, 12:21 pm
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

package com.rudderstack.android.ruddermetricsreporterandroid.utils

import org.hamcrest.BaseMatcher
import org.hamcrest.Description

class NotEmptyStringMatcher : BaseMatcher<Any>() {
    override fun describeTo(description: Description?) {
        description?.appendText("Not empty string")
    }

    override fun matches(item: Any?): Boolean {
        return (item is String && item.isNotEmpty()) || (item != null && item.toString() != "")
    }
}