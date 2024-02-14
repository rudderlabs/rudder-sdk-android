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
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Implements [ContentProvider] To be used by [Dao]
 *
 */
internal class EntityContentProvider : ContentProvider() {

    companion object {

        internal val ECP_NULL_HACK_COLUMN_CODE: String = "null_hack_column"
        internal const val ECP_ENTITY_CODE = "db_entity"
        internal const val ECP_LIMIT_CODE = "query_limit"
        internal const val ECP_DATABASE_CODE = "db_name"
        internal const val ECP_CONFLICT_RESOLUTION_CODE = "ecp_conflict_resolution"
        private const val ECP_TABLE_URI_MATCHER_CODE = 1
        private const val ECP_TABLE_SUB_QUERY_URI_MATCHER_CODE = 2

        private var uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private var authority: String? = null
        internal val AUTHORITY
            get() = authority
                    ?: throw UninitializedPropertyAccessException("onAttachInfo not called yet")
        private var nameToRudderDatabaseMap =
            ConcurrentHashMap<String, WeakReference<RudderDatabase>>()
        private var nameToCreateCommandMap =
            ConcurrentHashMap<String, MutableList<Array<String>>>()

        internal fun getContentUri(tableName: String, context: Context?): Uri {
            println("authority52: $authority")
            if (context != null && authority == null) {
                authority = context.packageName + "." + EntityContentProvider::class.java.simpleName
                println("authority: $authority")
            }
            val contentUri = Uri.parse("content://$authority/$tableName")
            try {
                // https://developer.android.com/guide/topics/providers/content-provider-creating

                uriMatcher.addURI(
                    authority,
                    tableName,
                    ECP_TABLE_URI_MATCHER_CODE,
                )
                uriMatcher.addURI(
                    authority,
                    "$tableName/*", // *: Matches a string of any valid characters of any length.
                    ECP_TABLE_SUB_QUERY_URI_MATCHER_CODE,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return contentUri
        }

        internal fun registerDatabase(database: RudderDatabase) {
            val ref = reference.get()
            if (ref != null) with(ref) {
                database.populateNameToSqliteMapping()
            }
            nameToRudderDatabaseMap[database.databaseName] = WeakReference(database)
        }

        internal fun registerTableCommands(
            databaseName: String,
            createTableStatement: String?,
            createIndexStatement: String?
        ) {
            val commands = arrayOf(createTableStatement, createIndexStatement).filterNotNull().toTypedArray()
            val ref = reference.get()
            if (ref != null) with(ref) {
                nameToSqLiteOpenHelperMap[databaseName]?.createTable(
                    commands
                )
            }else{
                val databaseCommands = nameToCreateCommandMap[databaseName]
                    ?: mutableListOf<Array<String>>().also { nameToCreateCommandMap[databaseName] = it }
                databaseCommands.add(commands)
            }
        }
        private fun SQLiteOpenHelper.createTable(commands: Array<String>){
            commands.forEach {
                writableDatabase.execSQL(it)
            }
        }

        internal fun releaseDatabase(databaseName: String) {
            nameToRudderDatabaseMap.remove(databaseName)
            val removedHelper = reference.get()?.nameToSqLiteOpenHelperMap?.remove(databaseName)
            if(removedHelper != null){
                try {
                    removedHelper.close()
                }catch (ex: Exception){
                    // ignore
                }
            }
        }

        private var reference = WeakReference<EntityContentProvider>(null)

    }

    private var nameToSqLiteOpenHelperMap = ConcurrentHashMap<String, SQLiteOpenHelper>()

    // we will be using this just to satisfy new Dao creation, however, the calls we make to Dao
    // should be synchronous.
    private val _commonExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(): Boolean {
        reference = WeakReference(this)
        nameToRudderDatabaseMap.forEach {
            val attachedDatabase = it.value.get()
            attachedDatabase?.populateNameToSqliteMapping()
        }
        nameToCreateCommandMap.forEach {
            val attachedSQLiteOpenHelper = nameToSqLiteOpenHelperMap[it.key]
            it.value.forEach {
                attachedSQLiteOpenHelper?.createTable(it)
            }
        }
        nameToCreateCommandMap.clear()
        return true
    }

    private fun RudderDatabase.populateNameToSqliteMapping() {
        getDbDetails { name, version, dbCreatedCb, dbUpgradeCb ->
            nameToSqLiteOpenHelperMap[name] = object : SQLiteOpenHelper(context, name, null, version) {
                init {
                    // listeners won't be fired else
                    writableDatabase
                }

                override fun onCreate(database: SQLiteDatabase?) {
                    dbCreatedCb?.invoke(database)
                }

                override fun onUpgrade(
                    database: SQLiteDatabase?,
                    oldVersion: Int,
                    newVersion: Int,
                ) {
                    dbUpgradeCb?.invoke(database, oldVersion, newVersion)
                }
            }

        }
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        authority = info?.authority
                    ?: info?.packageName?.let { it + "." + this@EntityContentProvider::class.java.simpleName }
        super.attachInfo(context, info)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        if (uriMatcher.match(uri) == -1) return null

        val tableName = uri.tableName ?: return null

        return uri.attachedSqliteOpenHelper?.writableDatabase?.query(
            tableName,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder,
            uri.limit,
        )
    }

    override fun getType(uri: Uri): String? {
        return null // no mime types allowed
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (uriMatcher.match(uri) == -1) return null
        val tableName = uri.tableName ?: return null

        val rowID = (uri.attachedSqliteOpenHelper?.writableDatabase?.insertWithOnConflict(
            tableName,
            uri.nullHackColumn,
            values,
            uri.conflictAlgorithm ?: SQLiteDatabase.CONFLICT_REPLACE,
        ) ?: -1)
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            val rowUri = ContentUris.withAppendedId(
                getContentUri(tableName, context),
                rowID,
            )
            context?.contentResolver?.notifyChange(rowUri, null)
            return rowUri
        }

        throw SQLException("Failed to add a record into $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uriMatcher.match(uri) == -1) return -1
        val tableName = uri.tableName ?: return -1

        return uri.attachedSqliteOpenHelper?.writableDatabase?.delete(
            tableName,
            selection,
            selectionArgs,
        ) ?: -1
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        if (uriMatcher.match(uri) == -1) return -1
        val tableName = uri.tableName ?: return -1

        return uri.attachedSqliteOpenHelper?.writableDatabase?.update(
            tableName,
            values,
            selection,
            selectionArgs,
        ) ?: -1
    }

    override fun onLowMemory() {
        _commonExecutor.shutdown()
        super.onLowMemory()
    }

    private val Uri.tableName: String?
        get() = pathSegments[0]
    private val Uri.attachedSqliteOpenHelper: SQLiteOpenHelper?
        get() = attachedDatabaseName?.let {
            nameToSqLiteOpenHelperMap[it] ?: run {
                attachedDatabase?.populateNameToSqliteMapping()
                 nameToSqLiteOpenHelperMap[it]
            }
        }
    private val Uri.attachedDatabaseName: String?
        get() = getQueryParameter(ECP_DATABASE_CODE)
    private val Uri.attachedDatabase: RudderDatabase?
        get() = attachedDatabaseName?.let {
            nameToRudderDatabaseMap[it]?.get()
        }
    private val Uri.limit: String?
        get() = getQueryParameter(ECP_LIMIT_CODE)
    private val Uri.nullHackColumn: String?
        get() = getQueryParameter(ECP_NULL_HACK_COLUMN_CODE)

    private val Uri.conflictAlgorithm: Int?
        get() = getQueryParameter(ECP_CONFLICT_RESOLUTION_CODE)?.toIntOrNull()
}
