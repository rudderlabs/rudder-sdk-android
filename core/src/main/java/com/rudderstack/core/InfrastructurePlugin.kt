/*
 * Creator: Debanjan Chatterjee on 20/11/23, 1:22 pm Last modified: 20/11/23, 1:20 pm
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

package com.rudderstack.core

import com.rudderstack.models.RudderServerConfig

/**
 * While [Plugin] is mostly used for message processing, [InfrastructurePlugin] is used for
 * implementing infrastructure related tasks.
 * Infrastructure Plugins are generally independent of event processing.
 *
 */
interface InfrastructurePlugin {
    fun setup(analytics: Analytics)
    fun shutdown()
    fun updateConfiguration(configuration: Configuration){
        //optional method
    }
    fun updateRudderServerConfig(serverConfig: RudderServerConfig){
        //optional method
    }

    /**
     * Pause the proceedings if applicable, for example data upload service can halt the upload
     */
    fun pause(){
        //optional-method
    }

    /**
     * Resume the proceedings had the plugin been paused
     */
    fun resume(){
        //optional-method
    }
    fun reset() {
        //optional method
    }
}