package com.rudderstack.android.internal

import org.junit.Test
import org.mockito.Mockito.*
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*

class DmtUtilsTest {

    @Test
    fun `maskWith should set the specified status bit`() {
        val initialStatus = STATUS_NEW
        val maskedStatus = initialStatus maskWith STATUS_CLOUD_MODE_DONE
        assertThat(maskedStatus, `is`(initialStatus or (1 shl STATUS_CLOUD_MODE_DONE)))
    }

    @Test
    fun `unmaskWith should clear the specified status bit`() {
        val initialStatus = STATUS_NEW maskWith STATUS_CLOUD_MODE_DONE
        val unmaskedStatus = initialStatus unmaskWith STATUS_CLOUD_MODE_DONE
        assertThat(unmaskedStatus, `is`(STATUS_NEW))
    }

    @Test
    fun `isDeviceModeDone should return true if the device mode is done`() {
        val status = STATUS_NEW maskWith STATUS_DEVICE_MODE_DONE
        assertThat(status.isDeviceModeDone(), `is`(true))
    }

    @Test
    fun `isDeviceModeDone should return false if the device mode is not done`() {
        val status = STATUS_NEW
        assertThat(status.isDeviceModeDone(), `is`(false))
    }

    @Test
    fun `isCloudModeDone should return true if the cloud mode is done`() {
        val status = STATUS_NEW maskWith STATUS_CLOUD_MODE_DONE
        assertThat(status.isCloudModeDone(), `is`(true))
    }
    @Test
    fun `isCloudModeDone should return true if the cloud mode and device modes are done`() {
        val status = STATUS_NEW maskWith STATUS_CLOUD_MODE_DONE maskWith STATUS_DEVICE_MODE_DONE
        assertThat(status.isCloudModeDone(), `is`(true))
    }
    @Test
    fun `isDeviceModeDone should return true if the cloud mode and device modes are done`() {
        val status = STATUS_NEW maskWith STATUS_CLOUD_MODE_DONE maskWith STATUS_DEVICE_MODE_DONE
        assertThat(status.isDeviceModeDone(), `is`(true))
    }

    @Test
    fun `isCloudModeDone should return false if the cloud mode is not done`() {
        val status = STATUS_NEW
        assertThat(status.isCloudModeDone(), `is`(false))
    }
    @Test
    fun `isCloudModeDone should return false if the only device mode done`() {
        val status = STATUS_NEW maskWith STATUS_DEVICE_MODE_DONE
        assertThat(status.isCloudModeDone(), `is`(false))
    }
}