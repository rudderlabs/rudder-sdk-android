/*
 * Creator: Debanjan Chatterjee on 16/06/23, 8:58 pm Last modified: 16/06/23, 8:58 pm
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

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class TestExecutor : AbstractExecutorService() {
    private var _isShutdown = false
    override fun execute(command: Runnable) {
        command.run()
    }

    override fun shutdown() {
        //No op
        _isShutdown = true
    }

    override fun shutdownNow(): MutableList<Runnable> {
        // No op
        shutdown()
        return mutableListOf()
    }

    override fun isShutdown(): Boolean {
        return _isShutdown
    }

    override fun isTerminated(): Boolean {
        return _isShutdown
    }

    override fun awaitTermination(timeout: Long, unit: TimeUnit?): Boolean {
        return false
    }
}