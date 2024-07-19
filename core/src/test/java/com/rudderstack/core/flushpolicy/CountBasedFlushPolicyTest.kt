package com.rudderstack.core.flushpolicy

import com.rudderstack.core.Analytics
import com.rudderstack.core.Configuration
import com.rudderstack.core.DataUploadService
import com.rudderstack.core.busyWait
import com.rudderstack.core.models.Message
import com.rudderstack.web.HttpResponse
import com.vagabond.testcommon.generateTestAnalytics
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.atomic.AtomicInteger

class CountBasedFlushPolicyTest {
    private lateinit var analytics: Analytics
    private lateinit var flushPolicy: CountBasedFlushPolicy
    private lateinit var mockUploadService: DataUploadService

    @Before
    fun setup() {
        mockUploadService = mock()
        val mockedResponse: HttpResponse<out Any> = HttpResponse(200, "OK", null)
        whenever(mockUploadService.upload(any(), any(), any())).then {
//            storage.deleteMessages(data)
            it.getArgument<(response: HttpResponse<out Any>) -> Unit>(2).invoke(
                mockedResponse
            )
        }
        Mockito.`when`(mockUploadService.uploadSync(any<List<Message>>(), anyOrNull())).thenReturn(
            mockedResponse
        )
        analytics = generateTestAnalytics(
            Configuration(mock(), shouldVerifySdk = false), dataUploadService = mockUploadService
        )
        flushPolicy = CountBasedFlushPolicy()
        analytics.removeAllFlushPolicies()
        analytics.addFlushPolicies(flushPolicy)
    }

    @After
    fun tearDown() {
        flushPolicy.shutdown()
        analytics.shutdown()
    }

    @Test
    fun testSetup() {
        flushPolicy.setup(analytics)
        //test do not crash
    }

    @Test
    fun testUpdateConfiguration() {
        val config = mock<Configuration>()
        val flushCalledCount = AtomicInteger(0)
        whenever(config.maxFlushInterval).thenReturn(100000)
        whenever(config.flushQueueSize).thenReturn(1)
        analytics.track { event("event") }
        flushPolicy.setFlush {
            flushCalledCount.incrementAndGet()
        }
        flushPolicy.updateConfiguration(config)

        busyWait(250L)
        MatcherAssert.assertThat(flushCalledCount.get(), Matchers.equalTo(1))
    }

}
