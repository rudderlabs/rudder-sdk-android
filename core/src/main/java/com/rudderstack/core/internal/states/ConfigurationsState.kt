/*
 * Creator: Debanjan Chatterjee on 29/12/21, 5:38 PM Last modified: 29/12/21, 5:37 PM
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

package com.rudderstack.core.internal.states

import com.rudderstack.core.Configuration
import com.rudderstack.core.State

internal class ConfigurationsState(initialConfiguration: Configuration? = null) :
    State<Configuration>(initialConfiguration) {}