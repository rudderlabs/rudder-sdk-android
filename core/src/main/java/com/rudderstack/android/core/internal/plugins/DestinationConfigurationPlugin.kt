/*
 * Creator: Debanjan Chatterjee on 01/02/22, 10:50 AM Last modified: 01/02/22, 10:50 AM
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

package com.rudderstack.android.core.internal.plugins

import com.rudderstack.android.core.Plugin
import com.rudderstack.android.models.Message

/**
 * Enables or disables destination plugins based on server config.
 * There can be three cases.
 * In case there is no device mode plugins, there will be no checking for Destination configuration.
 *
 * In case device mode plugins are present but no destination configuration, destination plugins will
 * be removed (though this scenario is very hard to produce, since as device mode plugins are required to
 * setup when server config is available), else it is responsibility of other plugins to do the same.
 *
 * In case device mode destination plugins are present along with server config, all destination
 * plugins that are disabled in destination config are filtered out.
 */
internal class DestinationConfigurationPlugin /*: Plugin {
    override fun intercept(chain: Plugin.Chain): Message {

    }
}*/