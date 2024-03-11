package com.rudderstack.android

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.storage.AndroidStorage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class ConfigurationActionsTest{

    @Test
    fun `test initial configuration is generated from storage`(){
        val storage = mock<AndroidStorage>()
        whenever(storage.trackAutoSession).thenReturn(true)
        val application = ApplicationProvider.getApplicationContext<Application>()
        val initialConfiguration = application.initialConfigurationAndroid(storage)
        assertThat(initialConfiguration.trackAutoSession, Matchers.`is`(true))
    }

}