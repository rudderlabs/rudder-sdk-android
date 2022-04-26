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

package com.rudderstack.web

/**
 * Base response class for http calls
 *
 * @param T The type of response body expected
 * @property status Http status codes, [HTTP_STATUS_NONE] if failed with an exception provided in [error]
 * @property body returned object of type T
 * @property errorBody In case of error this is non null, and contains raw Error Body returned from server
 * @property error In case a http(s) request fails due to an exception
 */
data class HttpResponse<T>(
    val status: Int, val body: T?, val errorBody: String?,
    val error: Throwable? = null
){
    companion object{
        const val HTTP_STATUS_NONE = 0
    }
}
