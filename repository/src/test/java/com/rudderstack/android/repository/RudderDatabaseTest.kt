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
import com.rudderstack.android.repository.models.SampleAutoGenEntity
import com.rudderstack.android.repository.models.SampleEntity
import com.rudderstack.android.repository.models.TestEntityFactory
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

//@RunWith(RobolectricTestRunner::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class RudderDatabaseTest {
    //    private lateinit var
//    private val delayedExecutor = Executors.newSingleThreadExecutor()
    @Before
    fun initialize() {
        RudderDatabase.init(
            ApplicationProvider.getApplicationContext(),
            "testDb",
            TestEntityFactory,
            false,
            executorService = TestExecutor()
        )

    }

    @After
    fun tearDown() {
        RudderDatabase.shutDown()
    }

    @Test
    fun `test race condition in dao list initialisation`() {

        val sampleDao = RudderDatabase.createNewDao(SampleEntity::class.java, TestExecutor())
        val sampleAutoGenDao = RudderDatabase.createNewDao(
            SampleAutoGenEntity::class.java, TestExecutor()
        )
        val sampleDaoCheck = RudderDatabase.getDao(SampleEntity::class.java)
        MatcherAssert.assertThat(sampleDao, Matchers.equalTo(sampleDaoCheck))
        MatcherAssert.assertThat(sampleAutoGenDao, Matchers.equalTo(sampleAutoGenDao))
        Thread.sleep(5000)
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
                Matchers.iterableWithSize(2), contains(*sampleEntitiesToSave.toTypedArray())
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
                        Matchers.iterableWithSize(2), contains(*sampleEntitiesToSave.toTypedArray())
                    )
                )
                isGetComplete.set(true)
            }
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(isGetComplete)


    }

    @Test
    fun testDeletion() {
        val sampleEntitiesToSave = listOf(
            SampleEntity("abc", 10, listOf("12", "34", "56")),
            SampleEntity("fgh", 10, listOf("34", "56", "78")),
            SampleEntity("def", 20, listOf("78", "90", "12"))
        )
        val sampleDao = RudderDatabase.getDao(SampleEntity::class.java)
        //save data
        val isCompleted = AtomicBoolean(false)
        with(sampleDao) {
            val rowIds = sampleEntitiesToSave.insertSync()
            assertThat(rowIds, iterableWithSize(3))
            println("inserted: ${rowIds?.size}")

            sampleEntitiesToSave.subList(0, 2).delete() {
                //number of deleted rows is 2
                assertThat(it, equalTo(2))
                val items = getAllSync()
                assertThat(
                    items, allOf(
                        iterableWithSize(1), contains(sampleEntitiesToSave[2])
                    )
                )
                isCompleted.set(true)
            }
        }
        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(isCompleted)

    }

    @Test
    fun testAutoGenEntities() {
        val entitiesToSave = listOf(
            SampleAutoGenEntity("abc"), SampleAutoGenEntity("fgh"), SampleAutoGenEntity("def")
        )
        val sampleDao = RudderDatabase.getDao(SampleAutoGenEntity::class.java)
        //save data
        val isCompleted = AtomicBoolean(false)
        with(sampleDao) {
            val rowIds = entitiesToSave.insertSync()
            assertThat(rowIds, iterableWithSize(3))
            println("inserted: ${rowIds?.size}")
            assertThat(rowIds, iterableWithSize(3))
            //entities in db should have autogenerated ids
            val savedEntities = getAllSync()
            assertThat(savedEntities, allOf(notNullValue(), iterableWithSize(3)))
            assertThat(savedEntities?.get(0), allOf(notNullValue(), hasProperty("id", not(0))))
            savedEntities?.subList(0, 2)?.delete() {
                //number of deleted rows is 2
                assertThat(it, equalTo(2))
                val items = getAllSync()
                assertThat(
                    items, allOf(
                        iterableWithSize(1), contains(savedEntities[2])
                    )
                )
                isCompleted.set(true)
            }
        }
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilTrue(isCompleted)

    }

}
