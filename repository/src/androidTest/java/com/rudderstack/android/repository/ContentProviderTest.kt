/*
 * Creator: Debanjan Chatterjee on 17/05/22, 1:05 PM Last modified: 16/05/22, 8:11 PM
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
import androidx.test.rule.provider.ProviderTestRule
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Exception

@RunWith(AndroidJUnit4::class)
class ContentProviderTest  {
    companion object{
        private const val DB_NAME = "test_db"
        private const val TABLE_NAME = "test_table"
//        private const val DB_INSERT_COMMAND_1 = "INSERT INTO $TABLE_NAME ('COL1', 'COL2') VALUES ('col1_val', 'col') "
    }
    @get:Rule
    var mProviderRule: ProviderTestRule =
        ProviderTestRule.Builder(EntityContentProvider::class.java,
                "com.rudderstack.android.repository.test.EntityContentProvider"
        )
            .build()
    //lets have a model class
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
    //let's create an entity for the same

    @RudderEntity(
        TABLE_NAME, fields = [
            RudderField(RudderField.Type.TEXT, "model_name", primaryKey = true),
            RudderField(RudderField.Type.TEXT, "model_values"),

        ]
    )
    class ModelEntity(val model: Model) : Entity {
        companion object {
            fun create(values: Map<String, Any?>): ModelEntity {
                return ModelEntity(
                    Model(
                        values["model_name"] as String,
                        (values["model_values"] as String).split(',').toTypedArray()
                    )
                )
            }
        }

        override fun generateContentValues(): ContentValues {
            return ContentValues(
            ).also {
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

    //entity factory
    class ModelEntityFactory : EntityFactory {
        override fun <T : Entity> getEntity(entity: Class<T>, values: Map<String, Any?>): T? {
            return when (entity) {
                ModelEntity::class.java -> ModelEntity.create(values)
                else -> null
            } as T?

        }

    }

    private val testUri by lazy {
        EntityContentProvider.getContentUri(TABLE_NAME, ApplicationProvider.getApplicationContext())
    }
    @Before
    fun initialize() {
        RudderDatabase.init(
            ApplicationProvider.getApplicationContext(),
//            RuntimeEnvironment.application,
            DB_NAME, ModelEntityFactory()
        )

    }

    @After
    fun tearDown() {
        RudderDatabase.shutDown()
    }
    @Test
    fun testContentProvider(){
        //let's start with insertion
        val modelEntity1 = ModelEntity(Model("event-1", arrayOf("1", "2")))
        val model2 = ModelEntity( Model("event-2", arrayOf("2", "3")))

        val contentResolver = mProviderRule.resolver
        //insert
        val uri1 = contentResolver.insert(testUri.buildUpon().appendQueryParameter(
            EntityContentProvider.ECP_ENTITY_CODE, ModelEntity::class.java.name
        ).build(), modelEntity1.generateContentValues())
        assertThat(uri1, allOf(
            notNullValue(),
        ))
    }

}