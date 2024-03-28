package com.rudderstack.android.internal.infrastructure

import com.rudderstack.core.Analytics
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class LifecycleObserverPluginTest {

    @Test
    fun `when app is backgrounded, then flush is called`() {
        val analytics = mockk<Analytics>(relaxed = true)
        val lifecycleObserverPlugin = LifecycleObserverPlugin()
        lifecycleObserverPlugin.setup(analytics)

        lifecycleObserverPlugin.onAppBackgrounded()

        verify { analytics.flush() }
    }
}
