/*
 * Creator: Debanjan Chatterjee on 05/05/22, 1:49 PM Last modified: 05/05/22, 1:49 PM
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

import android.content.*
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import java.util.concurrent.Executors

/**
 * Implements [ContentProvider] To be used by [Dao]
 *
 */
internal class EntityContentProvider : ContentProvider() {


    companion object {

        internal const val ECP_TABLE_CODE = "db_table"
        internal const val ECP_ENTITY_CODE = "db_entity"
        private const val ECP_TABLE_URI_MATCHER_CODE = 1
        private const val ECP_TABLE_SUB_QUERY_URI_MATCHER_CODE = 2

        private var uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private var authority: String? = null

        private var sqLiteOpenHelper: SQLiteOpenHelper? = null


        internal fun getContentUri(tableName: String, context: Context?): Uri {
            if (context != null && authority == null) {
                authority = context.packageName + "." + EntityContentProvider::class.java.simpleName
            }
            val contentUri = Uri.parse("content://$authority/$tableName")
            try {
                //https://developer.android.com/guide/topics/providers/content-provider-creating

                uriMatcher.addURI(
                    authority,
                    tableName,
                    ECP_TABLE_URI_MATCHER_CODE
                )
                uriMatcher.addURI(
                    authority,
                    "$tableName/*", // *: Matches a string of any valid characters of any length.
                    ECP_TABLE_SUB_QUERY_URI_MATCHER_CODE
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return contentUri
        }
    }

    //we will be using this just to satisfy new Dao creation, however, the calls we make to Dao
    //should be synchronous.
    private val _commonExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(): Boolean {
        RudderDatabase.getDbDetails { name, version, dbUpgradeCb ->
            sqLiteOpenHelper = object : SQLiteOpenHelper(context, name, null, version) {
                init {
                    //listeners won't be fired else
                    writableDatabase
                }

                override fun onCreate(database: SQLiteDatabase?) {
                    /**
                     * empty implementation
                     */
                }

                override fun onUpgrade(
                    database: SQLiteDatabase?,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    dbUpgradeCb?.invoke(database, oldVersion, newVersion)
                }

            }
        }
        return true
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        authority =
            info?.packageName + "." + this@EntityContentProvider::class.java.simpleName
        uriMatcher =
            UriMatcher(UriMatcher.NO_MATCH)

        /*_uriMatcher?.addURI(
            _authority,
            EVENTS_TABLE_NAME,
            com.rudderstack.android.sdk.core.EventContentProvider.EVENT_CODE
        )
        _uriMatcher?.addURI(
            _authority,
            EVENTS_TABLE_NAME.toString() + "/#",
            com.rudderstack.android.sdk.core.EventContentProvider.EVENT_ID_CODE
        )*/
        super.attachInfo(context, info)

    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getType(uri: Uri): String? {
        return null //no mime types allowed
    }


    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) == -1) return null
        val dao = uri.initializedDao ?: return null

        val tableName = uri.tableName ?: return null

        val rowID = dao.insertContentValues(
            sqLiteOpenHelper?.writableDatabase ?: return null,
            tableName, values ?: return null,
            null, Dao.ConflictResolutionStrategy.CONFLICT_REPLACE
        )
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            val rowUri = ContentUris.withAppendedId(
                getContentUri(tableName, context),
                rowID
            )
            context?.contentResolver?.notifyChange(rowUri, null)
            return rowUri
        }

        throw SQLException("Failed to add a record into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uriMatcher.match(uri) == -1) return -1
        val dao = uri.initializedDao ?: return -1
        val tableName = uri.tableName ?: return -1

        return dao.deleteFromDb(sqLiteOpenHelper?.writableDatabase?:return -1,
           tableName, selection, selectionArgs)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (uriMatcher.match(uri) == -1) return -1
        val dao = uri.initializedDao ?: return -1
        val tableName = uri.tableName ?: return -1

        return dao.updateSync(sqLiteOpenHelper?.writableDatabase?:return -1,
            tableName,values, selection, selectionArgs)
    }

    override fun onLowMemory() {
        _commonExecutor.shutdown()
        super.onLowMemory()
    }

    private val Uri.initializedDao: Dao<out Entity>?
        get() {
            return (((getQueryParameter(ECP_ENTITY_CODE))?.let {
                Class.forName(it) as? Entity
            } ?: return null)::class.java).let {
                RudderDatabase.createNewDao(it, _commonExecutor)
            }.also { dao ->
                dao.setDatabase(sqLiteOpenHelper?.writableDatabase)
            }
        }
    private val Uri.tableName: String?
        get() = getQueryParameter(ECP_TABLE_CODE)
}