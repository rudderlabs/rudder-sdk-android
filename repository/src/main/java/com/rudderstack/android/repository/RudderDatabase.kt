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

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 *  class to act as the Database helper
 */
private const val USE_CONTENT_PROVIDER_DEFAULT = false
@SuppressLint("StaticFieldLeak")
/**
 *
 *
 * @property context The context to create database
 * @property databaseName The database name to be used for the App
 * @property entityFactory Used to create entity from class name and values map
 * @property useContentProvider Use content provider to access database, required for apps with
 * multiple processes
 * @property databaseVersion database version
 * @property databaseUpgradeCallback If db upgrade is necessary, this is to be handled
 * @constructor
 *
 *
 * @param providedExecutorService
 * @param databaseCreatedCallback Can be used to prefill db on create
 */
class RudderDatabase(private val context: Context,
                     internal val databaseName: String,
                     private val entityFactory: EntityFactory,
                     private val useContentProvider: Boolean = USE_CONTENT_PROVIDER_DEFAULT,
                     private val databaseVersion: Int = 1,
                     providedExecutorService: ExecutorService? = null,
                     private val databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)? = null,
                     private var databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int,
                                                         newVersion:
                     Int) -> Unit)? = null,) {



    private val commonExecutor: ExecutorService =
        providedExecutorService.takeIf { it?.isShutdown == false } ?: ThreadPoolExecutor(
            0,
            Int.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            ThreadPoolExecutor.DiscardPolicy(),
        )
    private var sqliteOpenHelper: SQLiteOpenHelper?= null
    private var database: SQLiteDatabase? = null
    private var registeredDaoList: MutableMap<Class<out Entity>, Dao<out Entity>> =
        ConcurrentHashMap<Class<out Entity>, Dao<out Entity>>()
    private var dbDetailsListeners = listOf<
                (
        String,
        Int,
        databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)?,
        databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)?,
    ) -> Unit,
            >()
    init {
        synchronized(this) {
            dbDetailsListeners.forEach {
                it.invoke(
                    databaseName,
                    databaseVersion,
                    databaseCreatedCallback,
                    databaseUpgradeCallback
                )
            }
            if (!useContentProvider) {
                sqliteOpenHelper = initializeSqlOpenHelper(databaseCreatedCallback)
            }else{
                EntityContentProvider.registerDatabase(this)
            }
        }
    }

    private fun initializeSqlOpenHelper(databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)?) =
         object : SQLiteOpenHelper(context, databaseName, null, databaseVersion) {
            init {
                commonExecutor.execute {
                    this@RudderDatabase.database = writableDatabase
                    database?.let {
                        initDaoList(it, registeredDaoList.values.toList())
                    }
                }
            }

            override fun onCreate(database: SQLiteDatabase?) {
                databaseCreatedCallback?.invoke(database)
            }

            override fun onUpgrade(
                database: SQLiteDatabase?,
                oldVersion: Int,
                newVersion: Int,
            ) {
                databaseUpgradeCallback?.invoke(database, oldVersion, newVersion)
            }
        }

    /**
     * Get [Dao] for a particular [Entity]
     *
     * @param T The type of [Entity] provided
     * @param entityClass Class of [T]
     * @param executorService Defaults to a [Executors.newCachedThreadPool] In case a different
     * implementation is provided [RudderDatabase] won't be responsible for shutting it down.
     * @return A [Dao] based on the [entityClass]
     */
    fun <T : Entity> getDao(
        entityClass: Class<T>,
        executorService: ExecutorService = commonExecutor,

    ): Dao<T> {
        return registeredDaoList[entityClass]?.let {
            it as Dao<T>
        } ?: createNewDao(entityClass, executorService)
    }

    /**
     * Creates a new [Dao] object for an entity.
     * Usage of this method directly, is highly discouraged.
     *
     * @param T
     * @param entityClass
     * @param executorService
     * @return
     */
    internal fun <T : Entity> createNewDao(
        entityClass: Class<T>,
        executorService: ExecutorService,

    ): Dao<T> = Dao<T>(
        entityClass,
        useContentProvider,
        context
            ?: throw UninitializedPropertyAccessException("Did you call RudderDatabase.init?"),
        entityFactory,
        executorService,
        databaseName
    ).also {
        registeredDaoList[entityClass] = it
        database?.apply {
            initDaoList(this, listOf(it))
        }
    }

    /**
     * Get database name via a callback.
     * If name is available, the callback is called immediately,
     * else on being set
     *
     * @param callback
     */
    internal fun getDbDetails(
        callback: (
            String,
            Int,
            databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)?,
            databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)?,
        ) -> Unit,
    ) {
        databaseName?.let {
            callback.invoke(it, databaseVersion, databaseCreatedCallback, databaseUpgradeCallback)
        } ?: synchronized(this) { dbDetailsListeners = dbDetailsListeners + callback }
    }

    fun <T : Entity> Dao<T>.unregister() {
        registeredDaoList.remove(entityClass)
    }

    private fun initDaoList(database: SQLiteDatabase, daoList: List<Dao<out Entity>>) {
        database.apply {
            daoList.forEach {
                it.setDatabase(this)
            }
        }
    }

    fun shutDown() {
        registeredDaoList.iterator().forEach {
            it.value.setDatabase(null)
        }
        registeredDaoList.clear() // clearing all cached dao
        sqliteOpenHelper?.apply {
            // synchronizing on database allows other database users to synchronize on the same
            synchronized(this) {
                close()
                database = null
            }
        }?:run {
            database?.close()
            database = null
        }
        if(useContentProvider){
            EntityContentProvider.releaseDatabase(databaseName)
        }
        sqliteOpenHelper = null
        commonExecutor.shutdown()
        dbDetailsListeners = emptyList()
        databaseUpgradeCallback = null
    }

    /**
     * Deletes the database along with all the tables
     *
     */
    fun delete() {
        val file = sqliteOpenHelper?.readableDatabase?.path?.let { File(it) }?:return
        SQLiteDatabase.deleteDatabase(file)
    }
}
