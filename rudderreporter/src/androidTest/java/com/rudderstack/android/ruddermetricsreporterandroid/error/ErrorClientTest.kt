/*
 * Creator: Debanjan Chatterjee on 11/09/23, 10:30 am Last modified: 11/09/23, 10:30 am
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

package com.rudderstack.android.ruddermetricsreporterandroid.error

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.ruddermetricsreporterandroid.Configuration
import com.rudderstack.android.ruddermetricsreporterandroid.Reservoir
import com.rudderstack.android.ruddermetricsreporterandroid.error.TestUtils.generateClient
import com.rudderstack.android.ruddermetricsreporterandroid.error.TestUtils.generateConfiguration
import com.rudderstack.android.ruddermetricsreporterandroid.error.TestUtils.generateLibraryMetadata
import com.rudderstack.android.ruddermetricsreporterandroid.internal.DefaultReservoir
import com.rudderstack.jacksonrudderadapter.JacksonAdapter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Arrays
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

class ErrorClientTest {
    private var context: Context? = null
    private var config: Configuration? = null
    private var client: DefaultErrorClient? = null

    private val jsonAdapter = JacksonAdapter()

    /**
     * Generates a configuration and clears sharedPrefs values to begin the test with a clean slate
     */
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        config = generateConfiguration()
    }

    /**
     * Clears sharedPreferences to remove any values persisted
     */
    @After
    fun tearDown() {
        client?.close()
        client = null
    }

    @Test
    fun testNotify() {
        // Notify should not crash
        client = TestUtils.generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        client?.notify(RuntimeException("Testing"))
    }

    @Test
    fun testMaxErrors() {
        val config: Configuration = generateConfiguration()
        config.maxPersistedEvents = 2
        val reservoir = DefaultReservoir(context!!, false)
        reservoir.clearErrors()
        client = TestUtils.generateClient(config, reservoir, jsonAdapter)
        reservoir.assertErrorSize(0)
        client?.notify(RuntimeException("test"))
        client?.notify(RuntimeException("another"))
        client?.notify(RuntimeException("yet another"))
        client?.notify(RuntimeException("more"))
        client?.notify(RuntimeException("yet more"))
        reservoir.assertErrorSize(2)
    }

    private fun Reservoir.assertErrorSize(expectedSize: Long) {
        val countBlocker = AtomicBoolean(true)
        getErrorsCount {
            try {
                assertEquals(expectedSize, it)
                countBlocker.set(false)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        block(countBlocker)
    }

    private fun block(condition: AtomicBoolean) {
        while (condition.get()) {
            try {
                // busy block
                Thread.sleep(10)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @Test
    fun testMaxBreadcrumbs() {
        val config: Configuration = generateConfiguration()
        val breadcrumbTypes = Arrays.asList(BreadcrumbType.MANUAL)
        config.maxBreadcrumbs = 2
        client = TestUtils.generateClient(config, jsonAdapter)
        assertEquals(0, client?.breadcrumbState?.copy()?.size)
        client?.leaveBreadcrumb("test")
        client?.leaveBreadcrumb("another")
        client?.leaveBreadcrumb("yet another")
        assertEquals(2, client?.breadcrumbState?.copy()?.size)
        val poll: Breadcrumb? = client?.breadcrumbState?.copy()?.get(0)
        assertEquals(BreadcrumbType.MANUAL, poll?.type)
        assertEquals("another", poll?.name)
    }

    @Test
    fun testClientAddToTab() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        client?.addMetadata("drink", "cola", "cherry")
        assertNotNull(client?.getMetadata("drink"))
    }

    @Test
    fun testClientClearTab() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        client?.addMetadata("drink", "cola", "cherry")
        client?.addMetadata("food", "berries", "raspberry")
        client?.clearMetadata("drink")
        assertNull(client?.getMetadata("drink"))
        assertEquals("raspberry", client?.getMetadata("food", "berries"))
    }

    @Test
    fun testClientClearValue() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        client?.addMetadata("drink", "cola", "cherry")
        client?.addMetadata("drink", "soda", "cream")
        client?.clearMetadata("drink", "cola")
        assertNull(client?.getMetadata("drink", "cola"))
        assertEquals("cream", client?.getMetadata("drink", "soda"))
    }

    @Test
    fun testClientBreadcrumbRetrieval() {
        val config = Configuration(generateLibraryMetadata())
        config.enabledBreadcrumbTypes = emptySet<BreadcrumbType>()
        client = generateClient(config, jsonAdapter)
        client?.leaveBreadcrumb("Hello World")
        val breadcrumbs: List<Breadcrumb>? = client?.breadcrumbs
        val store: List<Breadcrumb> = ArrayList<Breadcrumb>(client?.breadcrumbState?.copy())
        assertEquals(store, breadcrumbs)
        assertNotSame(store, breadcrumbs)
    }

    @Test
    fun testBreadcrumbGetter() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        val breadcrumbs: List<Breadcrumb>? = client?.breadcrumbs
        val breadcrumbCount = breadcrumbs?.size
        client?.leaveBreadcrumb("Foo")
        assertEquals(
            breadcrumbCount?.toLong(),
            breadcrumbs?.size?.toLong(),
        ) // should not pick up new breadcrumbs
    }

    @Test
    fun testBreadcrumbStoreNotModified() {
        config?.enabledBreadcrumbTypes = setOf(BreadcrumbType.MANUAL)
        client = generateClient(config!!, jsonAdapter)
        client?.leaveBreadcrumb("Manual breadcrumb")
        val breadcrumbs: MutableList<Breadcrumb>? = client?.getBreadcrumbs()
        breadcrumbs?.clear() // only the copy should be cleared
        assertTrue(breadcrumbs?.isEmpty()!!)
        assertEquals(1, client?.breadcrumbState?.copy()?.size)
        assertEquals("Manual breadcrumb", client?.breadcrumbState?.copy()?.get(0)?.name)
    }

    @Test
    fun testAppDataMetadata() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        val app: Map<String, Any?>? = client?.getAppDataCollector()?.getAppDataMetadata()
        assertEquals(9, app?.size)
        assertEquals("com.rudderstack.android.ruddermetricsreporterandroid.test", app!!["name"])
        assertEquals("com.rudderstack.android.ruddermetricsreporterandroid.test", app["processName"])
        assertNotNull(app["memoryUsage"])
        assertTrue(app.containsKey("installerPackage"))
        assertNotNull(app["lowMemory"])
        assertNotNull(app["memoryTrimLevel"])
    }

    @Test
    fun testPopulateDeviceMetadata() {
        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
        val metadata: Map<String, Any?>? = client?.getDeviceDataCollector()?.getDeviceMetadata()
        assertEquals(17, metadata!!.size.toLong())
        assertNotNull(metadata["batteryLevel"])
        assertNotNull(metadata["charging"])
        assertNotNull(metadata["locationStatus"])
        assertNotNull(metadata["networkAccess"])
        assertNotNull(metadata["brand"])
        assertNotNull(metadata["screenDensity"])
        assertNotNull(metadata["dpi"])
        assertNotNull(metadata["emulator"])
        assertNotNull(metadata["screenResolution"])
        assertNotNull(metadata["osVersion"])
        assertNotNull(metadata["manufacturer"])
    }

    @Test
    fun testMetadataCloned() {
        config?.addMetadata("test_section", "foo", "bar")
        client = generateClient(config!!, jsonAdapter)
        client?.addMetadata("test_section", "second", "another value")

        // metadata state should be deep copied
        assertNotSame(config?.metadataState, client?.metadataState)

        // metadata object should be deep copied
        val configData: Metadata = config?.metadataState?.metadata!!
        val clientData: Metadata = client?.metadataState?.metadata!!
        assertNotSame(configData, clientData)

        // metadata backing map should be deep copied

        // validate configuration metadata
        val configExpected = Collections.singletonMap<String, Any>("foo", "bar")
        assertEquals(configExpected, config?.getMetadata("test_section"))

        // validate client metadata
        val data: MutableMap<String, Any> = mutableMapOf()
        data["foo"] = "bar"
        data["second"] = "another value"
        assertEquals(data, client?.getMetadata("test_section"))
    }

    /**
     * Verifies that calling notify() concurrently delivers event payloads and
     * does not crash the app.
     */
//    @Test
//    @Throws(InterruptedException::class)
//    fun testClientMultiNotify() {
//        // concurrently call notify()
//        client = generateClient(Configuration(generateLibraryMetadata()), jsonAdapter)
//        val executor: Executor = Executors.newSingleThreadExecutor()
//        val count = 200
//        val latch = CountDownLatch(count)
//
//        for (k in 0 until count / 2) {
//            client?.notify(RuntimeException("Whoops"))
//            latch.countDown()
//            executor.execute {
//                client?.notify(RuntimeException("Oh dear"))
//                latch.countDown()
//            }
//        }
//        // wait for all events to be delivered
//        assertTrue(latch.await(5, TimeUnit.SECONDS))
//    }
}
