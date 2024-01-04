/*
 * Creator: Debanjan Chatterjee on 21/06/23, 2:47 pm Last modified: 21/06/23, 2:47 pm
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
@file:JvmName("SqliteConflictIgnoreIssueWorkAround")

package com.rudderstack.android.repository.utils

internal fun getInsertedRowIdForConflictIgnore(prevDbCount: Long, returnedRowId: Long): Long {
    if (prevDbCount <= 0L) {
        return returnedRowId
    }
    if (returnedRowId in 0..prevDbCount) {
        return -1
    }
    return returnedRowId
}
