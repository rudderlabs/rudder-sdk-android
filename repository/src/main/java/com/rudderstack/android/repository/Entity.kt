/*
 * Creator: Debanjan Chatterjee on 24/09/21, 11:09 PM Last modified: 15/09/21, 11:06 PM
 * Copyright: All rights reserved Ⓒ 2021 http://hiteshsahu.com
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

package com.rudderstack.android.repository

import android.content.ContentValues

interface Entity {



    /**
     * Maintain same sequence as fields
     *
     * @return
     */
    fun generateContentValues() : ContentValues
    fun nullHackColumn() : String? = null

    /**
     * returns the values associated with the primary key
     *
     * @return for example if there's a single primary key, return arrayOf(id.toString())
     * else return arrayOf(id.toString(), email)
     */
    fun getPrimaryKeyValues() : Array<String>

}