/*
 * Creator: Debanjan Chatterjee on 27/04/22, 3:41 PM Last modified: 27/04/22, 3:41 PM
 * Copyright: All rights reserved Ⓒ 2022 http://rudderstack.com
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

import android.content.ContentValues
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import com.rudderstack.android.ruddermetricsreporterandroid.utils.TestExecutor
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
@RunWith(AndroidJUnit4::class)
@Config(sdk = [29])
class CustomEntityRudderDatabaseTest {

    // lets have a model class
    data class Model(val name: String, val values: Array<String>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Model

            if (name != other.name) return false
            if (!values.contentEquals(other.values)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + values.contentHashCode()
            return result
        }
    }
    // let's create an entity for the same

    @RudderEntity(
        "model_table",
        fields = [
            RudderField(RudderField.Type.TEXT, "model_name", primaryKey = true),
            RudderField(RudderField.Type.TEXT, "model_values"),

        ],
    )
    class ModelEntity(val model: Model) : Entity {
        companion object {
            fun create(values: Map<String, Any?>): ModelEntity {
                return ModelEntity(
                    Model(
                        values["model_name"] as String,
                        (values["model_values"] as String).split(',').toTypedArray(),
                    ),
                )
            }
        }

        override fun generateContentValues(): ContentValues {
            return ContentValues().also {
                it.put("model_name", model.name)
                it.put("model_values", model.values.reduce { acc, s -> "$acc,$s" })
            }
        }

        override fun getPrimaryKeyValues(): Array<String> {
            return arrayOf(model.name)
        }

        override fun equals(other: Any?): Boolean {
            return other is ModelEntity && other.model == model
        }

        override fun hashCode(): Int {
            return model.hashCode()
        }
    }

    // entity factory
    class ModelEntityFactory : EntityFactory {
        override fun <T : Entity> getEntity(entity: Class<T>, values: Map<String, Any?>): T? {
            return when (entity) {
                ModelEntity::class.java -> ModelEntity.create(values)
                else -> null
            } as T?
        }
    }
    lateinit var database: RudderDatabase
    @Before
    fun initialize() {
        database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
//            RuntimeEnvironment.application,
            "testDb",
            ModelEntityFactory(),
            false,
            executorService = TestExecutor(),
        )
    }

    @After
    fun tearDown() {
        database.shutDown()
    }

    @Test
    fun `test custom Entity`() {
        val sampleModelEntitiesToSave = listOf(
            Model("name-1", arrayOf("a", "b", "c")),
            Model("name-2", arrayOf("d", "e", "f")),
            Model("name-3", arrayOf("g", "h", "i")),
            Model("name-4", arrayOf("j", "k", "l")),
        ).map {
            ModelEntity(it)
        }
        val entityModelDao = database.getDao(ModelEntity::class.java)
        // save data
        val isCompleted = AtomicBoolean(false)
        with(entityModelDao) {
            val rowIds = sampleModelEntitiesToSave.insertSync()
            MatcherAssert.assertThat(rowIds, Matchers.iterableWithSize(4))
//            println("inserted: ${rowIds?.size}")
            val savedItems = getAllSync()?.map {
                it.model.name
            }
            val namesToBePresent = sampleModelEntitiesToSave.map {
                it.model.name
            }
            MatcherAssert.assertThat(
                savedItems,
                Matchers.allOf(
                    Matchers.iterableWithSize(4),
                    Matchers.contains(*namesToBePresent.toTypedArray()),
                ),
            )

            sampleModelEntitiesToSave.subList(0, 2).delete() {
                // number of deleted rows is 2
                MatcherAssert.assertThat(it, Matchers.equalTo(2))
                val items = getAllSync()?.map {
                    it.model.name
                }
                MatcherAssert.assertThat(
                    items,
                    Matchers.allOf(
                        Matchers.iterableWithSize(2),
                        Matchers.contains(*namesToBePresent.subList(2, 4).toTypedArray()),
                    ),
                )
                isCompleted.set(true)
            }
        }
        Awaitility.await().atMost(500, TimeUnit.SECONDS).untilTrue(isCompleted)
    }
}
