/*
 * Creator: Debanjan Chatterjee on 30/09/21, 11:41 PM Last modified: 30/09/21, 11:39 PM
 * Copyright: All rights reserved Ⓒ 2021 http://rudderstack.com
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

package com.rudderstack.android.repository

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.repository.models.SampleEntity
import com.rudderstack.android.repository.models.SampleEntityFactory
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

//@RunWith(RobolectricTestRunner::class)
@RunWith(AndroidJUnit4::class)
class RudderDatabaseTest {
    //    private lateinit var
    @Before
    fun initialize() {
        RudderDatabase.init(
            ApplicationProvider.getApplicationContext(),
//            RuntimeEnvironment.application,
            "testDb", SampleEntityFactory
        )

    }

    @After
    fun tearDown() {
        RudderDatabase.shutDown()
    }

    @Test
    fun multipleDaoCallsToReturnSameDaoObject() {
        val sampleDaoCheck = RudderDatabase.getDao(SampleEntity::class.java)
        val sampleDao = RudderDatabase.getDao(SampleEntity::class.java)
        MatcherAssert.assertThat(sampleDao, Matchers.equalTo(sampleDaoCheck))
    }

    @Test
    fun testInsertionAndGetSync() {
        val sampleEntitiesToSave = listOf(
            SampleEntity("abc", 10, listOf("12", "34", "56")),
            SampleEntity("def", 20, listOf("78", "90", "12"))
        )
        val sampleDao = RudderDatabase.getDao(SampleEntity::class.java)
        //save data
        val isInserted = AtomicBoolean(false)
        with(sampleDao) {
            sampleEntitiesToSave.insert() { rowIds ->
                assertThat(rowIds, iterableWithSize(2))
                println("inserted: ${rowIds.size}")
                isInserted.set(true)
            }
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(isInserted)
        //getting the data
        val savedData = with(sampleDao) { getAllSync() }

        MatcherAssert.assertThat(
            savedData, allOf(
                Matchers.iterableWithSize(2),
                contains(*sampleEntitiesToSave.toTypedArray())
            )
        )

    }

    @Test
    fun testGetAsync() {
        val sampleEntitiesToSave = listOf(
            SampleEntity("abc", 10, listOf("12", "34", "56")),
            SampleEntity("def", 20, listOf("78", "90", "12"))
        )
        val sampleDao = RudderDatabase.getDao(SampleEntity::class.java)
        //save data
        val isInserted = AtomicBoolean(false)
        with(sampleDao) {
            val rowIds = sampleEntitiesToSave.insertSync()
            assertThat(rowIds, iterableWithSize(2))
            println("inserted: ${rowIds?.size}")
            isInserted.set(true)

        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(isInserted)
        val isGetComplete = AtomicBoolean(false)
        //getting the data
        with(sampleDao) {
            getAll() {
                assertThat(
                    it, allOf(
                        Matchers.iterableWithSize(2),
                        contains(*sampleEntitiesToSave.toTypedArray())
                    )
                )
                isGetComplete.set(true)
            }
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(isGetComplete)


    }
}