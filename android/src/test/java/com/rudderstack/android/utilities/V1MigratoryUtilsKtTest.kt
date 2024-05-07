package com.rudderstack.android.utilities

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.rudderstack.android.storage.saveObject
import com.rudderstack.core.internal.KotlinLogger
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(
    RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.P])
class V1MigratoryUtilsKtTest{
    private val context = ApplicationProvider.getApplicationContext<Application>()
    @Test
    fun `test sourceId should not exist`(){
        val isSourceIdExist = context.isV1SavedServerConfigContainsSourceId("fileName", "new_source_id")
        assertThat(isSourceIdExist, Matchers.`is`(false))
    }
    @Test
    fun `test wrong sourceId exists`(){
        val fileName = "file_name"
        //create a file
        saveObject("dummy", context, fileName, KotlinLogger)
        val isSourceIdExist = context.isV1SavedServerConfigContainsSourceId(fileName, "new_source_id")
        assertThat(isSourceIdExist, Matchers.`is`(false))
    }
    @Test
    fun `test sourceId should exist`(){

        val sourceId = "i_am_source_id"
        val fileName = "file_name"
        //create a file
        saveObject("my source id is $sourceId", context, fileName, KotlinLogger)
        val isSourceIdExist = context.isV1SavedServerConfigContainsSourceId(fileName, sourceId)
        assertThat(isSourceIdExist, Matchers.`is`(true))
    }
}