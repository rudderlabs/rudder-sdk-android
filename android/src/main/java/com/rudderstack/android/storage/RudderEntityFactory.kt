/*
 * Creator: Debanjan Chatterjee on 28/04/22, 12:26 AM Last modified: 28/04/22, 12:26 AM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

package com.rudderstack.android.storage

import com.rudderstack.android.repository.Entity
import com.rudderstack.android.repository.EntityFactory
import com.rudderstack.core.internal.states.ConfigurationsState
import com.rudderstack.models.Message
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter

internal class RudderEntityFactory : EntityFactory {
    override fun <T : Entity> getEntity(entity: Class<T>, values: Map<String, Any?>): T? {

        //we will check the class for conversion
        return when(entity){
            MessageEntity::class.java -> ConfigurationsState.value?.jsonAdapter?.let {  MessageEntity.create(values, it) as? T?}
            else -> null
        }
    }
}