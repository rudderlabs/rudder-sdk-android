/*
 * Creator: Debanjan Chatterjee on 16/01/24, 3:37 pm Last modified: 16/01/24, 3:37 pm
 * Copyright: All rights reserved Ⓒ 2024 http://rudderstack.com
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

package com.rudderstack.android.android.internal.plugins

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.ConfigurationAndroid
import com.rudderstack.android.internal.plugins.PlatformInputsPlugin
import com.rudderstack.core.Analytics
import com.rudderstack.core.Plugin
import com.rudderstack.core.RudderUtils
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import com.rudderstack.models.TrackMessage
import com.rudderstack.rudderjsonadapter.JsonAdapter
import com.rudderstack.rudderjsonadapter.RudderTypeAdapter
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasEntry
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import java.util.TimeZone


@RunWith(AndroidJUnit4::class)
@Config(sdk = [29], application = AndroidContextPluginTestApplication::class)
class PlatformInputsPluginTest {
    private lateinit var platformInputsPlugin: PlatformInputsPlugin
    protected var jsonAdapter: JsonAdapter = JacksonAdapter()
    private lateinit var analytics: Analytics
    @Before
    fun setUp() {
        val app = getApplicationContext<AndroidContextPluginTestApplication>()
        platformInputsPlugin = PlatformInputsPlugin()
        analytics = generateTestAnalytics(ConfigurationAndroid(app,
            jsonAdapter, shouldVerifySdk = false))
        platformInputsPlugin.setup(analytics)
    }
    @After
    fun destroy() {
        platformInputsPlugin.reset()
        analytics.shutdown()
    }
    @Test
    fun testInterceptWithMessage() {

        val message = TrackMessage.create(
            "testEvent",
            timestamp = RudderUtils.timeStamp,
            traits = mapOf("traitKey" to "traitValue")
        )
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        whenever(mockChain.proceed(any())).thenAnswer {
            it.arguments[0] as TrackMessage
        }
        val verifyMsg = platformInputsPlugin.intercept(mockChain)
        assertThat(verifyMsg.context, allOf(Matchers.aMapWithSize(11),
            hasEntry("traits", mapOf("traitKey" to "traitValue")),//yo
            hasKey("screen"),
            hasEntry("timezone", (TimeZone.getDefault().id))
        ),)
        assertThat(
            verifyMsg.context!!["app"], `is`(mapOf("name" to AndroidContextPluginTestApplication.PACKAGE_NAME,
                "build" to "1", "namespace" to AndroidContextPluginTestApplication.PACKAGE_NAME,
                "version" to "1.0")))
        assertThat(
            verifyMsg.context!!["os"] as Map<*, *>, `is`( allOf (aMapWithSize(2), hasEntry
                ("name", "Android"), hasKey("version"))))
        assertThat(
            verifyMsg.context!!["device"] as Map<*,*>, `is`(allOf(
                hasKey("id"),
                hasKey("manufacturer"),
                hasKey("model"),
                hasKey("name"),
                hasKey("type"),
                hasKey("adTrackingEnabled"),
            )))
        assertThat(
            verifyMsg.context!!["network"] as Map<*,*>, `is`(allOf(
//                hasKey("carrier"),
                hasKey("bluetooth"),
                hasKey("cellular"),
                hasKey("wifi"))))


    }


    @Test
    fun testSetAdvertisingId() {
        platformInputsPlugin.setAdvertisingId("testAdvertisingId")
        val message = TrackMessage.create(
            "testEvent",
            timestamp = RudderUtils.timeStamp
        )
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        whenever(mockChain.proceed(any())).thenAnswer {
            it.arguments[0] as TrackMessage
        }
        val verifyMsg = platformInputsPlugin.intercept(mockChain)
        assertThat(
            verifyMsg.context!!["device"] as Map<*, *>, hasEntry("advertisingId", "testAdvertisingId")
        )
    }
    @Test
    fun testChannelIsSetToMessages() {
        platformInputsPlugin.setAdvertisingId("testAdvertisingId")
        val message = TrackMessage.create(
            "testEvent",
            timestamp = RudderUtils.timeStamp
        )
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        whenever(mockChain.proceed(any())).thenAnswer {
            it.arguments[0] as TrackMessage
        }
        val verifyMsg = platformInputsPlugin.intercept(mockChain)
        assertThat(
            verifyMsg, hasProperty("channel", `is`("mobile"))
        )
    }

    @Test
    fun testPutDeviceToken() {
        platformInputsPlugin.putDeviceToken("testDeviceToken")
        val message = TrackMessage.create(
            "testEvent",
            timestamp = RudderUtils.timeStamp
        )
        val mockChain = mock<Plugin.Chain>()
        whenever(mockChain.message()).thenReturn(message)
        whenever(mockChain.proceed(any())).thenAnswer {
            it.arguments[0] as TrackMessage
        }
        val verifyMsg = platformInputsPlugin.intercept(mockChain)
        assertThat(
            verifyMsg!!.context!!["device"] as Map<*,*>, hasEntry("token", "testDeviceToken")
        )
    }

}

class AndroidContextPluginTestApplication : Application() {
    companion object {
        const val TAG = "AndroidContextPluginTestApplication"
        const val PACKAGE_NAME = "com.rudderstack.android.test"
        const val NAME = "TestApp"
    }

    override fun getPackageManager(): PackageManager {
        val pkm = super.getPackageManager()
        val newPkm = spy(pkm)
        val packageInfo = PackageInfo().apply {
            packageName = PACKAGE_NAME
            versionName = "1.0"
            versionCode = 1
            longVersionCode = 1
            applicationInfo = ApplicationInfo().apply {
                packageName = PACKAGE_NAME
                targetSdkVersion = 30
            }
        }

        whenever(newPkm.getPackageInfo(PACKAGE_NAME, 0)).thenReturn(packageInfo)
        whenever(newPkm.getText(eq(PACKAGE_NAME), any<Int>(), anyOrNull())).thenReturn(PACKAGE_NAME)
        return newPkm
    }

    override fun getPackageName(): String {
        return PACKAGE_NAME
    }
}