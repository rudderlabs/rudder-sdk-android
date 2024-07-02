/*
 * Creator: Debanjan Chatterjee on 04/09/23, 7:22 pm Last modified: 04/09/23, 7:22 pm
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

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType

object TestDataGenerator {

    fun generateTestMetrics(count: Int) = (1..count).map {
        getTestMetric(it)
    }

    fun getTestMetric(identity: Int) = MetricModel<Long>(
        "test_metric_$identity",
        MetricType.COUNTER,
        identity.toLong(),
        mapOf("type" to "type_$identity"),
    )

    fun generateTestErrorEventsJson(count: Int) = (1..count).map {
        getTestErrorEventJsonWithIdentity(it)
    }

    fun generateTestErrorEventsJson(range: Iterable<Int>) =
        range.map {
            getTestErrorEventJsonWithIdentity(it)
        }
    fun getTestErrorEventJsonWithIdentity(identity: Int) = """
        {
        "exceptions": [
        {
            "errorClass": "java.lang.Exception",
            "type": "ANDROID",
            "stacktrace": [
            {
                "method": "com.rudderstack.android.ruddermetricsreporterandroid.utils
                .generateTestErrorEventsJson#$identity",
                "file": "TestDataGenerator.kt",
                "lineNumber": 18,
                "inProject": false
            }, {
                "method": "com.rudderstack.android.ruddermetricsreporterandroid.error.ErrorEventTest.serialize",
                "file": "ErrorEventTest.kt",
                "lineNumber": 315,
                "inProject": true
            },
            {
                "method": "jdk.internal.reflect.NativeMethodAccessorImpl.invoke0",
                "file": "NativeMethodAccessorImpl.java",
                "lineNumber": -2
            },
            {
                "method": "jdk.internal.reflect.NativeMethodAccessorImpl.invoke",
                "file": "NativeMethodAccessorImpl.java",
                "lineNumber": 62
            },
            {
                "method": "jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke",
                "file": "DelegatingMethodAccessorImpl.java",
                "lineNumber": 43
            },
            {
                "method": "java.lang.reflect.Method.invoke",
                "file": "Method.java",
                "lineNumber": 566
            },
            {
                "method": "org.junit.runners.model.FrameworkMethod${"\$"}1.runReflectiveCall",
                "file": "FrameworkMethod.java",
                "lineNumber": 50
            },
            {
                "method": "org.junit.internal.runners.model.ReflectiveCallable.run",
                "file": "ReflectiveCallable.java",
                "lineNumber": 12
            },
            {
                "method": "org.junit.runners.model.FrameworkMethod.invokeExplosively",
                "file": "FrameworkMethod.java",
                "lineNumber": 47
            },
            {
                "method": "org.junit.internal.runners.statements.InvokeMethod.evaluate",
                "file": "InvokeMethod.java",
                "lineNumber": 17
            },
            {
                "method": "org.junit.runners.ParentRunner.runLeaf",
                "file": "ParentRunner.java",
                "lineNumber": 325
            },
            {
                "method": "org.junit.runners.BlockJUnit4ClassRunner.runChild",
                "file": "BlockJUnit4ClassRunner.java",
                "lineNumber": 78
            },
            {
                "method": "org.junit.runners.BlockJUnit4ClassRunner.runChild",
                "file": "BlockJUnit4ClassRunner.java",
                "lineNumber": 57
            },
            {
                "method": "org.junit.runners.ParentRunner${"\$"}3.run",
                "file": "ParentRunner.java",
                "lineNumber": 290
            },
            {
                "method": "org.junit.runners.ParentRunner${"\$"}1.schedule",
                "file": "ParentRunner.java",
                "lineNumber": 71
            },
            {
                "method": "org.junit.runners.ParentRunner.runChildren",
                "file": "ParentRunner.java",
                "lineNumber": 288
            },
            {
                "method": "org.junit.runners.ParentRunner.access${"\$"}000",
                "file": "ParentRunner.java",
                "lineNumber": 58
            },
            {
                "method": "org.junit.runners.ParentRunner${"\$"}2.evaluate",
                "file": "ParentRunner.java",
                "lineNumber": 268
            },
            {
                "method": "org.junit.runners.ParentRunner.run",
                "file": "ParentRunner.java",
                "lineNumber": 363
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.runTestClass",
                "file": "JUnitTestClassExecutor.java",
                "lineNumber": 110
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.execute",
                "file": "JUnitTestClassExecutor.java",
                "lineNumber": 58
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.junit.JUnitTestClassExecutor.execute",
                "file": "JUnitTestClassExecutor.java",
                "lineNumber": 38
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.junit.AbstractJUnitTestClassProcessor.processTestClass",
                "file": "AbstractJUnitTestClassProcessor.java",
                "lineNumber": 62
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.SuiteTestClassProcessor.processTestClass",
                "file": "SuiteTestClassProcessor.java",
                "lineNumber": 51
            },
            {
                "method": "jdk.internal.reflect.NativeMethodAccessorImpl.invoke0",
                "file": "NativeMethodAccessorImpl.java",
                "lineNumber": -2
            },
            {
                "method": "jdk.internal.reflect.NativeMethodAccessorImpl.invoke",
                "file": "NativeMethodAccessorImpl.java",
                "lineNumber": 62
            },
            {
                "method": "jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke",
                "file": "DelegatingMethodAccessorImpl.java",
                "lineNumber": 43
            },
            {
                "method": "java.lang.reflect.Method.invoke",
                "file": "Method.java",
                "lineNumber": 566
            },
            {
                "method": "org.gradle.internal.dispatch.ReflectionDispatch.dispatch",
                "file": "ReflectionDispatch.java",
                "lineNumber": 36
            },
            {
                "method": "org.gradle.internal.dispatch.ReflectionDispatch.dispatch",
                "file": "ReflectionDispatch.java",
                "lineNumber": 24
            },
            {
                "method": "org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch",
                "file": "ContextClassLoaderDispatch.java",
                "lineNumber": 33
            },
            {
                "method": "org.gradle.internal.dispatch.ProxyDispatchAdapter${"\$DispatchingInvocationHandler"}.invoke",
                "file": "ProxyDispatchAdapter.java",
                "lineNumber": 94
            },
            {
                "method": "com.sun.proxy.${"\$Proxy2"}.processTestClass",
                "file": "Unknown",
                "lineNumber": -1
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.worker.TestWorker${"\$"}2.run",
                "file": "TestWorker.java",
                "lineNumber": 176
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName",
                "file": "TestWorker.java",
                "lineNumber": 129
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.worker.TestWorker.execute",
                "file": "TestWorker.java",
                "lineNumber": 100
            },
            {
                "method": "org.gradle.api.internal.tasks.testing.worker.TestWorker.execute",
                "file": "TestWorker.java",
                "lineNumber": 60
            },
            {
                "method": "org.gradle.process.internal.worker.child.ActionExecutionWorker.execute",
                "file": "ActionExecutionWorker.java",
                "lineNumber": 56
            },
            {
                "method": "org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call",
                "file": "SystemApplicationClassLoaderWorker.java",
                "lineNumber": 133
            },
            {
                "method": "org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call",
                "file": "SystemApplicationClassLoaderWorker.java",
                "lineNumber": 71
            },
            {
                "method": "worker.org.gradle.process.internal.worker.GradleWorkerMain.run",
                "file": "GradleWorkerMain.java",
                "lineNumber": 69
            },
            {
                "method": "worker.org.gradle.process.internal.worker.GradleWorkerMain.main",
                "file": "GradleWorkerMain.java",
                "lineNumber": 74
            }
            ]
        }
        ],
        "severity": "ERROR",
        "breadcrumbs": [

        ],
        "unhandled": true,
        "projectPackages": [
        "com.rudderstack.android"
        ],
        "app": {
        "binaryArch": "arm64",
        "id": "write_key",
        "version": "2.1.0",
        "versionCode": "14"
    },
    "device": {
    "freeDisk": 54354354,
    "freeMemory": 45345345,
    "time": "Aug 31, 2023, 5:32:32 PM",
    "cpuAbi": [
    "x86_64"
    ],
    "jailbroken": false,
    "locale": "locale",
    "totalMemory": 1234556,
    "manufacturer": "LG",
    "model": "Nexus",
    "osName": "android",
    "osVersion": "8.0.1",
    "runtimeVersions": {
    "androidApiLevel": "29",
    "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
}
},
"metadata": {
"m1": {
"dumb": "dumber"
}
}
}

"""
}
