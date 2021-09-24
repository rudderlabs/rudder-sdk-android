/*
 * Creator: Debanjan Chatterjee on 24/09/21, 11:09 PM Last modified: 15/09/21, 7:46 PM
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

/**
 * Store Field values. This will be used to create table
 * if isIndex is true, the index name will be "$fieldName1_$fieldName2_idx"
 *
 * @property type
 * @property fieldName
 * @property primaryKey
 * @property isAutoInc only applicable if primary key and type is Integer
 * @property isIndex if this column should serve as a basis for indexing
 *
 */

annotation class RudderField(val type: Type, val fieldName: String, val primaryKey: Boolean = false,
                 val isNullable : Boolean = true,
                 val isAutoInc: Boolean = false, val isIndex: Boolean = false){
    /**
     * Represents type of column
     *
     */
    enum class Type(val notation : String){
        INTEGER("INTEGER"),
        TEXT("TEXT")
    }
}

