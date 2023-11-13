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

import com.rudderstack.android.ruddermetricsreporterandroid.metrics.LongCounter
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModel
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricModelWithId
import com.rudderstack.android.ruddermetricsreporterandroid.metrics.MetricType
import com.rudderstack.android.ruddermetricsreporterandroid.models.Snapshot

object TestDataGenerator {

    fun generateTestMetrics(count : Int) = (1..count).map {
        getTestMetric(it)
    }

    fun getTestMetric(identity: Int) = MetricModel<Long>("test_metric_$identity",
        MetricType.COUNTER, identity.toLong(), mapOf("type" to "type_$identity"))


    fun generateTestErrorEventsJson(count : Int) = (1..count).map {
        getTestErrorEventJsonWithIdentity(it)
    }

    fun generateMetricModelWithId(count : Int) = (1..count).map {
        getMetricModelWithId(it)
    }

    fun getMetricModelWithId(id: Int) =
        MetricModelWithId<Long>(id.toString(),"test_metric_$id",
            MetricType.COUNTER, id.toLong(), mapOf("type" to "type_$id"))


    fun generateTestErrorEventsJson(range: Iterable<Int>) =
        range.map {
        getTestErrorEventJsonWithIdentity(it)
    }
    fun getTestErrorEventJsonWithIdentity(identity : Int) = """
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
    fun mockSnapshot(id: String = "3cc8e4a4-71ca-491e-a5eb-aef83a4b0489") = Snapshot(id,
        """
            {
              "message_id": "$id",
              "metrics": [
                
              ],
              "source": {
                "name": "com.rudderstack.android.sdk.core",
                "os_version": "29",
                "sdk_version": "1.20.1",
                "version_code": "20",
                "write_key": "1xXCubSHWXbpBI2h6EpCjKOsxmQ"
              },
              "version": "1",
              "errors": {
                "events": [
                  {
                    "exceptions": [
                      {
                        "errorClass": "java.lang.Exception",
                        "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023",
                        "stacktrace": [
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity
                            .onCreate${"$"}lambda\${"$"}{'$'}5",
                            "file": "MainActivity.kt",
                            "lineNumber": 78.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.${"$"}r8${"$"}lambda${"$"}{'$'}0jx5-_ODOzEyJmgtXRqjTRGD--8",
                            "file": "MainActivity.kt",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity${'$'}${"$"}ExternalSyntheticLambda5.onClick",
                            "file": "R8${'$'}${"$"}SyntheticClass",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "android.view.View.performClick",
                            "file": "View.java",
                            "lineNumber": 7125.0
                          },
                          {
                            "method": "android.view.View.performClickInternal",
                            "file": "View.java",
                            "lineNumber": 7102.0
                          },
                          {
                            "method": "android.view.View.access${'$'}3500",
                            "file": "View.java",
                            "lineNumber": 801.0
                          },
                          {
                            "method": "android.view.View${"$"}PerformClick.run",
                            "file": "View.java",
                            "lineNumber": 27336.0
                          },
                          {
                            "method": "android.os.Handler.handleCallback",
                            "file": "Handler.java",
                            "lineNumber": 883.0
                          },
                          {
                            "method": "android.os.Handler.dispatchMessage",
                            "file": "Handler.java",
                            "lineNumber": 100.0
                          },
                          {
                            "method": "android.os.Looper.loop",
                            "file": "Looper.java",
                            "lineNumber": 214.0
                          },
                          {
                            "method": "android.app.ActivityThread.main",
                            "file": "ActivityThread.java",
                            "lineNumber": 7356.0
                          },
                          {
                            "method": "java.lang.reflect.Method.invoke",
                            "file": "Method.java",
                            "lineNumber": -2.0
                          },
                          {
                            "method": "com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run",
                            "file": "RuntimeInit.java",
                            "lineNumber": 492.0
                          },
                          {
                            "method": "com.android.internal.os.ZygoteInit.main",
                            "file": "ZygoteInit.java",
                            "lineNumber": 930.0
                          }
                        ],
                        "type": "ANDROID"
                      }
                    ],
                    "severity": "WARNING",
                    "breadcrumbs": [
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.020Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.160Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.329Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.478Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.626Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.774Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.928Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.099Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.242Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.393Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.545Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.693Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.859Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.045Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.177Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.539Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.691Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.862Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.010Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.178Z",
                        "type": "ERROR"
                      }
                    ],
                    "unhandled": false,
                    "projectPackages": [
                      "com.example.testapp1mg"
                    ],
                    "app": {
                      "id": "com.example.testapp1mg",
                      "releaseStage": "development",
                      "version": "1.20.1",
                      "versionCode": "20"
                    },
                    "device": {
                      "manufacturer": "Google",
                      "model": "Android SDK built for x86",
                      "osName": "android",
                      "osVersion": "10",
                      "cpuAbi": "x86",
                      "jailbroken": "true",
                      "locale": "en_US",
                      "totalMemory": "2089168896",
                      "runtimeVersions": {
                        "androidApiLevel": "29",
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
                      },
                      "freeDisk": "1690099712",
                      "freeMemory": "942084096",
                      "orientation": "portrait",
                      "time": "2023-11-08T19:23:55.304Z"
                    },
                    "metadata": {
                      "app": {
                        "memoryUsage": 4672792.0,
                        "memoryTrimLevel": "None",
                        "totalMemory": 6594415.0,
                        "processName": "com.example.testapp1mg",
                        "name": "Sample Kotlin",
                        "memoryLimit": 5.36870912E8,
                        "lowMemory": false,
                        "freeMemory": 1921623.0
                      },
                      "device": {
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys",
                        "manufacturer": "Google",
                        "locationStatus": "allowed",
                        "networkAccess": "none",
                        "osVersion": "10",
                        "fingerprint": "google/sdk_gphone_x86/generic_x86:10/QSR1.210802.001/7603624:userdebug/dev-keys",
                        "model": "Android SDK built for x86",
                        "dpi": 480.0,
                        "screenResolution": "1776x1080",
                        "brand": "google",
                        "apiLevel": 29.0,
                        "batteryLevel": 1.0,
                        "cpuAbis": [
                          "x86"
                        ],
                        "charging": false,
                        "tags": "dev-keys",
                        "emulator": true,
                        "screenDensity": 3.0
                      }
                    }
                  },
                  {
                    "exceptions": [
                      {
                        "errorClass": "java.lang.Exception",
                        "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023",
                        "stacktrace": [
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.onCreate${"$"}lambda${'$'}5",
                            "file": "MainActivity.kt",
                            "lineNumber": 78.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.${"$"}r8${"$"}lambda${'$'}0jx5-_ODOzEyJmgtXRqjTRGD--8",
                            "file": "MainActivity.kt",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity${'$'}${"$"}ExternalSyntheticLambda5.onClick",
                            "file": "R8${'$'}${"$"}SyntheticClass",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "android.view.View.performClick",
                            "file": "View.java",
                            "lineNumber": 7125.0
                          },
                          {
                            "method": "android.view.View.performClickInternal",
                            "file": "View.java",
                            "lineNumber": 7102.0
                          },
                          {
                            "method": "android.view.View.access${'$'}3500",
                            "file": "View.java",
                            "lineNumber": 801.0
                          },
                          {
                            "method": "android.view.View${"$"}PerformClick.run",
                            "file": "View.java",
                            "lineNumber": 27336.0
                          },
                          {
                            "method": "android.os.Handler.handleCallback",
                            "file": "Handler.java",
                            "lineNumber": 883.0
                          },
                          {
                            "method": "android.os.Handler.dispatchMessage",
                            "file": "Handler.java",
                            "lineNumber": 100.0
                          },
                          {
                            "method": "android.os.Looper.loop",
                            "file": "Looper.java",
                            "lineNumber": 214.0
                          },
                          {
                            "method": "android.app.ActivityThread.main",
                            "file": "ActivityThread.java",
                            "lineNumber": 7356.0
                          },
                          {
                            "method": "java.lang.reflect.Method.invoke",
                            "file": "Method.java",
                            "lineNumber": -2.0
                          },
                          {
                            "method": "com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run",
                            "file": "RuntimeInit.java",
                            "lineNumber": 492.0
                          },
                          {
                            "method": "com.android.internal.os.ZygoteInit.main",
                            "file": "ZygoteInit.java",
                            "lineNumber": 930.0
                          }
                        ],
                        "type": "ANDROID"
                      }
                    ],
                    "severity": "WARNING",
                    "breadcrumbs": [
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.020Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.160Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.329Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.478Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.626Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.774Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.928Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.099Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.242Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.393Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.545Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.693Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.859Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.045Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.177Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.539Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.691Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.862Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.010Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.178Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.310Z",
                        "type": "ERROR"
                      }
                    ],
                    "unhandled": false,
                    "projectPackages": [
                      "com.example.testapp1mg"
                    ],
                    "app": {
                      "id": "com.example.testapp1mg",
                      "releaseStage": "development",
                      "version": "1.20.1",
                      "versionCode": "20"
                    },
                    "device": {
                      "manufacturer": "Google",
                      "model": "Android SDK built for x86",
                      "osName": "android",
                      "osVersion": "10",
                      "cpuAbi": "x86",
                      "jailbroken": "true",
                      "locale": "en_US",
                      "totalMemory": "2089168896",
                      "runtimeVersions": {
                        "androidApiLevel": "29",
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
                      },
                      "freeDisk": "1690091520",
                      "freeMemory": "942030848",
                      "orientation": "portrait",
                      "time": "2023-11-08T19:23:55.437Z"
                    },
                    "metadata": {
                      "app": {
                        "memoryUsage": 4792008.0,
                        "memoryTrimLevel": "None",
                        "totalMemory": 6594415.0,
                        "processName": "com.example.testapp1mg",
                        "name": "Sample Kotlin",
                        "memoryLimit": 5.36870912E8,
                        "lowMemory": false,
                        "freeMemory": 1802407.0
                      },
                      "device": {
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys",
                        "manufacturer": "Google",
                        "locationStatus": "allowed",
                        "networkAccess": "none",
                        "osVersion": "10",
                        "fingerprint": "google/sdk_gphone_x86/generic_x86:10/QSR1.210802.001/7603624:userdebug/dev-keys",
                        "model": "Android SDK built for x86",
                        "dpi": 480.0,
                        "screenResolution": "1776x1080",
                        "brand": "google",
                        "apiLevel": 29.0,
                        "batteryLevel": 1.0,
                        "cpuAbis": [
                          "x86"
                        ],
                        "charging": false,
                        "tags": "dev-keys",
                        "emulator": true,
                        "screenDensity": 3.0
                      }
                    }
                  },
                  {
                    "exceptions": [
                      {
                        "errorClass": "java.lang.Exception",
                        "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023",
                        "stacktrace": [
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.onCreate${"$"}lambda${'$'}5",
                            "file": "MainActivity.kt",
                            "lineNumber": 78.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.${"$"}r8${"$"}lambda${"$"}{'$'}0jx5-_ODOzEyJmgtXRqjTRGD--8",
                            "file": "MainActivity.kt",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity${'$'}${"$"}ExternalSyntheticLambda5.onClick",
                            "file": "R8${'$'}${"$"}SyntheticClass",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "android.view.View.performClick",
                            "file": "View.java",
                            "lineNumber": 7125.0
                          },
                          {
                            "method": "android.view.View.performClickInternal",
                            "file": "View.java",
                            "lineNumber": 7102.0
                          },
                          {
                            "method": "android.view.View.access${'$'}3500",
                            "file": "View.java",
                            "lineNumber": 801.0
                          },
                          {
                            "method": "android.view.View${"$"}PerformClick.run",
                            "file": "View.java",
                            "lineNumber": 27336.0
                          },
                          {
                            "method": "android.os.Handler.handleCallback",
                            "file": "Handler.java",
                            "lineNumber": 883.0
                          },
                          {
                            "method": "android.os.Handler.dispatchMessage",
                            "file": "Handler.java",
                            "lineNumber": 100.0
                          },
                          {
                            "method": "android.os.Looper.loop",
                            "file": "Looper.java",
                            "lineNumber": 214.0
                          },
                          {
                            "method": "android.app.ActivityThread.main",
                            "file": "ActivityThread.java",
                            "lineNumber": 7356.0
                          },
                          {
                            "method": "java.lang.reflect.Method.invoke",
                            "file": "Method.java",
                            "lineNumber": -2.0
                          },
                          {
                            "method": "com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run",
                            "file": "RuntimeInit.java",
                            "lineNumber": 492.0
                          },
                          {
                            "method": "com.android.internal.os.ZygoteInit.main",
                            "file": "ZygoteInit.java",
                            "lineNumber": 930.0
                          }
                        ],
                        "type": "ANDROID"
                      }
                    ],
                    "severity": "WARNING",
                    "breadcrumbs": [
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.020Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.160Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.329Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.478Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.626Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.774Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.928Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.099Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.242Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.393Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.545Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.693Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.859Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.045Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.177Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.539Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.691Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.862Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.010Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.178Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.310Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.443Z",
                        "type": "ERROR"
                      }
                    ],
                    "unhandled": false,
                    "projectPackages": [
                      "com.example.testapp1mg"
                    ],
                    "app": {
                      "id": "com.example.testapp1mg",
                      "releaseStage": "development",
                      "version": "1.20.1",
                      "versionCode": "20"
                    },
                    "device": {
                      "manufacturer": "Google",
                      "model": "Android SDK built for x86",
                      "osName": "android",
                      "osVersion": "10",
                      "cpuAbi": "x86",
                      "jailbroken": "true",
                      "locale": "en_US",
                      "totalMemory": "2089168896",
                      "runtimeVersions": {
                        "androidApiLevel": "29",
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
                      },
                      "freeDisk": "1690083328",
                      "freeMemory": "941907968",
                      "orientation": "portrait",
                      "time": "2023-11-08T19:23:55.585Z"
                    },
                    "metadata": {
                      "app": {
                        "memoryUsage": 4943992.0,
                        "memoryTrimLevel": "None",
                        "totalMemory": 6594415.0,
                        "processName": "com.example.testapp1mg",
                        "name": "Sample Kotlin",
                        "memoryLimit": 5.36870912E8,
                        "lowMemory": false,
                        "freeMemory": 1650423.0
                      },
                      "device": {
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys",
                        "manufacturer": "Google",
                        "locationStatus": "allowed",
                        "networkAccess": "none",
                        "osVersion": "10",
                        "fingerprint": "google/sdk_gphone_x86/generic_x86:10/QSR1.210802.001/7603624:userdebug/dev-keys",
                        "model": "Android SDK built for x86",
                        "dpi": 480.0,
                        "screenResolution": "1776x1080",
                        "brand": "google",
                        "apiLevel": 29.0,
                        "batteryLevel": 1.0,
                        "cpuAbis": [
                          "x86"
                        ],
                        "charging": false,
                        "tags": "dev-keys",
                        "emulator": true,
                        "screenDensity": 3.0
                      }
                    }
                  },
                  {
                    "exceptions": [
                      {
                        "errorClass": "java.lang.Exception",
                        "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023",
                        "stacktrace": [
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.onCreate${"$"}lambda${'$'}5",
                            "file": "MainActivity.kt",
                            "lineNumber": 78.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.${"$"}r8${"$"}lambda${'$'}0jx5-_ODOzEyJmgtXRqjTRGD--8",
                            "file": "MainActivity.kt",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity${'$'}${"$"}ExternalSyntheticLambda5.onClick",
                            "file": "R8${'$'}${"$"}SyntheticClass",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "android.view.View.performClick",
                            "file": "View.java",
                            "lineNumber": 7125.0
                          },
                          {
                            "method": "android.view.View.performClickInternal",
                            "file": "View.java",
                            "lineNumber": 7102.0
                          },
                          {
                            "method": "android.view.View.access${'$'}3500",
                            "file": "View.java",
                            "lineNumber": 801.0
                          },
                          {
                            "method": "android.view.View${"$"}PerformClick.run",
                            "file": "View.java",
                            "lineNumber": 27336.0
                          },
                          {
                            "method": "android.os.Handler.handleCallback",
                            "file": "Handler.java",
                            "lineNumber": 883.0
                          },
                          {
                            "method": "android.os.Handler.dispatchMessage",
                            "file": "Handler.java",
                            "lineNumber": 100.0
                          },
                          {
                            "method": "android.os.Looper.loop",
                            "file": "Looper.java",
                            "lineNumber": 214.0
                          },
                          {
                            "method": "android.app.ActivityThread.main",
                            "file": "ActivityThread.java",
                            "lineNumber": 7356.0
                          },
                          {
                            "method": "java.lang.reflect.Method.invoke",
                            "file": "Method.java",
                            "lineNumber": -2.0
                          },
                          {
                            "method": "com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run",
                            "file": "RuntimeInit.java",
                            "lineNumber": 492.0
                          },
                          {
                            "method": "com.android.internal.os.ZygoteInit.main",
                            "file": "ZygoteInit.java",
                            "lineNumber": 930.0
                          }
                        ],
                        "type": "ANDROID"
                      }
                    ],
                    "severity": "WARNING",
                    "breadcrumbs": [
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.020Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.160Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.329Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.478Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.626Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.774Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.928Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.099Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.242Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.393Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.545Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.693Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.859Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.045Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.177Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.539Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.691Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.862Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.010Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.178Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.310Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.443Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.589Z",
                        "type": "ERROR"
                      }
                    ],
                    "unhandled": false,
                    "projectPackages": [
                      "com.example.testapp1mg"
                    ],
                    "app": {
                      "id": "com.example.testapp1mg",
                      "releaseStage": "development",
                      "version": "1.20.1",
                      "versionCode": "20"
                    },
                    "device": {
                      "manufacturer": "Google",
                      "model": "Android SDK built for x86",
                      "osName": "android",
                      "osVersion": "10",
                      "cpuAbi": "x86",
                      "jailbroken": "true",
                      "locale": "en_US",
                      "totalMemory": "2089168896",
                      "runtimeVersions": {
                        "androidApiLevel": "29",
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
                      },
                      "freeDisk": "1690075136",
                      "freeMemory": "941797376",
                      "orientation": "portrait",
                      "time": "2023-11-08T19:23:55.737Z"
                    },
                    "metadata": {
                      "app": {
                        "memoryUsage": 5063208.0,
                        "memoryTrimLevel": "None",
                        "totalMemory": 6594415.0,
                        "processName": "com.example.testapp1mg",
                        "name": "Sample Kotlin",
                        "memoryLimit": 5.36870912E8,
                        "lowMemory": false,
                        "freeMemory": 1531207.0
                      },
                      "device": {
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys",
                        "manufacturer": "Google",
                        "locationStatus": "allowed",
                        "networkAccess": "none",
                        "osVersion": "10",
                        "fingerprint": "google/sdk_gphone_x86/generic_x86:10/QSR1.210802.001/7603624:userdebug/dev-keys",
                        "model": "Android SDK built for x86",
                        "dpi": 480.0,
                        "screenResolution": "1776x1080",
                        "brand": "google",
                        "apiLevel": 29.0,
                        "batteryLevel": 1.0,
                        "cpuAbis": [
                          "x86"
                        ],
                        "charging": false,
                        "tags": "dev-keys",
                        "emulator": true,
                        "screenDensity": 3.0
                      }
                    }
                  }
                ],
                "payloadVersion": 5,
                "notifier": {
                  "name": "com.rudderstack.android.sdk.core",
                  "version": "1.20.1",
                  "url": "https://github.com/rudderlabs/rudder-sdk-android",
                  "os_version": "29"
                }
              }
            }
            
        """.trimIndent())


    val exceptionJson = """
        {
                    "exceptions": [
                      {
                        "errorClass": "java.lang.Exception",
                        "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023",
                        "stacktrace": [
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity
                            .onCreate${"$"}lambda\${'$'}{"${'$'}"}{'${'$'}'}5",
                            "file": "MainActivity.kt",
                            "lineNumber": 78.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity.${"$"}r8${"$"}lambda${"$"}{'${'$'}'}0jx5-_ODOzEyJmgtXRqjTRGD--8",
                            "file": "MainActivity.kt",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "com.rudderstack.android.sample.kotlin.MainActivity${'$'}${"$"}ExternalSyntheticLambda5.onClick",
                            "file": "R8${'$'}${"$"}SyntheticClass",
                            "lineNumber": 0.0
                          },
                          {
                            "method": "android.view.View.performClick",
                            "file": "View.java",
                            "lineNumber": 7125.0
                          },
                          {
                            "method": "android.view.View.performClickInternal",
                            "file": "View.java",
                            "lineNumber": 7102.0
                          },
                          {
                            "method": "android.view.View.access${'$'}3500",
                            "file": "View.java",
                            "lineNumber": 801.0
                          },
                          {
                            "method": "android.view.View${"$"}PerformClick.run",
                            "file": "View.java",
                            "lineNumber": 27336.0
                          },
                          {
                            "method": "android.os.Handler.handleCallback",
                            "file": "Handler.java",
                            "lineNumber": 883.0
                          },
                          {
                            "method": "android.os.Handler.dispatchMessage",
                            "file": "Handler.java",
                            "lineNumber": 100.0
                          },
                          {
                            "method": "android.os.Looper.loop",
                            "file": "Looper.java",
                            "lineNumber": 214.0
                          },
                          {
                            "method": "android.app.ActivityThread.main",
                            "file": "ActivityThread.java",
                            "lineNumber": 7356.0
                          },
                          {
                            "method": "java.lang.reflect.Method.invoke",
                            "file": "Method.java",
                            "lineNumber": -2.0
                          },
                          {
                            "method": "com.android.internal.os.RuntimeInit${"$"}MethodAndArgsCaller.run",
                            "file": "RuntimeInit.java",
                            "lineNumber": 492.0
                          },
                          {
                            "method": "com.android.internal.os.ZygoteInit.main",
                            "file": "ZygoteInit.java",
                            "lineNumber": 930.0
                          }
                        ],
                        "type": "ANDROID"
                      }
                    ],
                    "severity": "WARNING",
                    "breadcrumbs": [
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.020Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.160Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.329Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.478Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.626Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.774Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:41 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:41.928Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.099Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.242Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.393Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.545Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.693Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:42 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:42.859Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.045Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:43 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:43.177Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.539Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.691Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:54 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:54.862Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.010Z",
                        "type": "ERROR"
                      },
                      {
                        "metadata": {
                          "unhandled": "false",
                          "severity": "WARNING",
                          "errorClass": "java.lang.Exception",
                          "message": "Test Error-Thu Nov 09 00:53:55 GMT+05:30 2023"
                        },
                        "name": "java.lang.Exception",
                        "timestamp": "2023-11-08T19:23:55.178Z",
                        "type": "ERROR"
                      }
                    ],
                    "unhandled": false,
                    "projectPackages": [
                      "com.example.testapp1mg"
                    ],
                    "app": {
                      "id": "com.example.testapp1mg",
                      "releaseStage": "development",
                      "version": "1.20.1",
                      "versionCode": "20"
                    },
                    "device": {
                      "manufacturer": "Google",
                      "model": "Android SDK built for x86",
                      "osName": "android",
                      "osVersion": "10",
                      "cpuAbi": "x86",
                      "jailbroken": "true",
                      "locale": "en_US",
                      "totalMemory": "2089168896",
                      "runtimeVersions": {
                        "androidApiLevel": "29",
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys"
                      },
                      "freeDisk": "1690099712",
                      "freeMemory": "942084096",
                      "orientation": "portrait",
                      "time": "2023-11-08T19:23:55.304Z"
                    },
                    "metadata": {
                      "app": {
                        "memoryUsage": 4672792.0,
                        "memoryTrimLevel": "None",
                        "totalMemory": 6594415.0,
                        "processName": "com.example.testapp1mg",
                        "name": "Sample Kotlin",
                        "memoryLimit": 5.36870912E8,
                        "lowMemory": false,
                        "freeMemory": 1921623.0
                      },
                      "device": {
                        "osBuild": "sdk_gphone_x86-userdebug 10 QSR1.210802.001 7603624 dev-keys",
                        "manufacturer": "Google",
                        "locationStatus": "allowed",
                        "networkAccess": "none",
                        "osVersion": "10",
                        "fingerprint": "google/sdk_gphone_x86/generic_x86:10/QSR1.210802.001/7603624:userdebug/dev-keys",
                        "model": "Android SDK built for x86",
                        "dpi": 480.0,
                        "screenResolution": "1776x1080",
                        "brand": "google",
                        "apiLevel": 29.0,
                        "batteryLevel": 1.0,
                        "cpuAbis": [
                          "x86"
                        ],
                        "charging": false,
                        "tags": "dev-keys",
                        "emulator": true,
                        "screenDensity": 3.0
                      }
                    }
                  }
    """.trimIndent()
}