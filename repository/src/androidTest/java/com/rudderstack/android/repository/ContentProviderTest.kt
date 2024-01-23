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
import android.content.UriMatcher
import android.net.Uri
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

@RunWith(AndroidJUnit4::class)
class ContentProviderTest {
    companion object {
        private const val DB_NAME = "test_db"
        private const val TABLE_NAME = "test_table"
        private const val FIELD_NAME_1 = "model_name"
        private const val FIELD_NAME_2 = "model_values"
//        private const val DB_INSERT_COMMAND_1 = "INSERT INTO $TABLE_NAME ('COL1', 'COL2') VALUES ('col1_val', 'col') "
    }

    private lateinit var database: RudderDatabase

    @get:Rule
    var mProviderRule: ProviderTestRule = ProviderTestRule.Builder(
        EntityContentProvider::class.java,
        "com.rudderstack.android.repository.test.EntityContentProvider",
    ).setDatabaseCommands(DB_NAME).build()

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
        TABLE_NAME,
        fields = [
            RudderField(RudderField.Type.TEXT, FIELD_NAME_1, primaryKey = true),
            RudderField(RudderField.Type.TEXT, FIELD_NAME_2),

        ],
    )
    class ModelEntity(val model: Model) : Entity {
        companion object {
            fun create(values: Map<String, Any?>): ModelEntity {
                return ModelEntity(
                    Model(
                        values[FIELD_NAME_1] as String,
                        (values[FIELD_NAME_2] as String).split(',').toTypedArray(),
                    ),
                )
            }
        }

        override fun generateContentValues(): ContentValues {
            return ContentValues().also {
                it.put(FIELD_NAME_1, model.name)
                it.put(FIELD_NAME_2, model.values.reduce { acc, s -> "$acc,$s" })
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

    private val testUri
        get() = EntityContentProvider.getContentUri(
            TABLE_NAME,
            ApplicationProvider.getApplicationContext()
        )


    @Before
    fun initialize() {
        database = RudderDatabase(
            ApplicationProvider.getApplicationContext(),
//            RuntimeEnvironment.application,
            DB_NAME, ModelEntityFactory(), true
        )
        //create table for ModelEntity
        EntityContentProvider.registerTableCommands(
            DB_NAME,
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($FIELD_NAME_1 TEXT PRIMARY KEY, $FIELD_NAME_2 TEXT)",
            null

        )

    }

    @After
    fun tearDown() {
        database.shutDown()
    }

    @Test
    fun testUriMatcherSuccess() {
        val uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        val authority = "com.rudderstack.android.repository.test.EntityContentProvider"
        val tableName = "test_table"
//        val contentUri = Uri.parse("content://$authority/$tableName")
        uriMatcher.addURI(authority, tableName, 1)
        uriMatcher.addURI(authority, "$tableName/*", 2)
        val testUri = Uri.parse(
            "content://com.rudderstack.android.repository.test.EntityContentProvider" + "/test_table?db_entity=com.rudderstack.android.repository.ContentProviderTest%24ModelEntity&db_name=test_db"
        )
        assertThat(uriMatcher.match(testUri), org.hamcrest.Matchers.`is`(1))

    }

    @Test
    fun testContentProviderWithTwoEntities() {
        // let's start with insertion
        val modelEntity1 = ModelEntity(Model("event-1", arrayOf("1", "2")))
        val modelEntity2 = ModelEntity(Model("event-2", arrayOf("2", "3")))

        val modelTestUriBuilder = testUri.buildUpon().appendQueryParameter(
            EntityContentProvider.ECP_ENTITY_CODE,
            ModelEntity::class.java.name,
        ).appendQueryParameter(
            EntityContentProvider.ECP_DATABASE_CODE, DB_NAME
        )
        val contentResolver = mProviderRule.resolver

        println("modelTestUriBuilder: $modelTestUriBuilder")
        // insert
        val uri1 = contentResolver.insert(
            modelTestUriBuilder.build(),
            modelEntity1.generateContentValues()
        )
        println("uri1: $uri1")
        assertThat(
            uri1,
            allOf(
                notNullValue(),
            ),
        )
        val uri2 = contentResolver.insert(modelTestUriBuilder.build(), modelEntity2.generateContentValues())
        assertThat(
            uri2,
            allOf(
                notNullValue(),
            ),
        )
//        // Two elements present
        val cursor = contentResolver.query(modelTestUriBuilder.build(), null, null, null, null)
        assertThat(
            cursor?.count,
            allOf(
                notNullValue(),
                `is`(2),
            ),
        )
        cursor?.close()
        // fetch with limit 1
        val cursor2 = contentResolver.query(
            modelTestUriBuilder
                .appendQueryParameter(EntityContentProvider.ECP_LIMIT_CODE, "1")
                .build(),
            null,
            null,
            null,
            null,
        )
        assertThat(
            cursor2?.count,
            allOf(
                notNullValue(),
                `is`(1),
            ),
        )
        cursor2?.close()
        // delete both
        val delCount = contentResolver.delete(modelTestUriBuilder.build(), null, null)
        // del count should be 2
        assertThat(delCount, `is`(2))
    }
}
