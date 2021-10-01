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

package com.rudderstack.android.repository.models

import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.EntityFactory

object SampleEntityFactory : EntityFactory {
    override fun <T : Entity> getEntity(entity: Class<T>, values: Map<String, Any>): T? {
        return when(entity){
             SampleEntity::class.java -> SampleEntity(values.get("name") as String,
             values["count"] as Int, (values["items"] as String).split(',')) as T
            else -> null
        }
    }
}