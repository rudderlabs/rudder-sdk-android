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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Singleton class to act as the Database helper
 */
@SuppressLint("StaticFieldLeak")
object RudderDatabase {
    private var sqliteOpenHelper: SQLiteOpenHelper? = null
    private var database: SQLiteDatabase? = null
    private var registeredDaoList = HashMap<Class<out Entity>, Dao<out Entity>>(20)
    private var context : Context? = null
    private var useContentProvider = false
    private var dbDetailsListeners = listOf<(
        String, Int,
        databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)?
    ) -> Unit>()
    private var databaseName: String? = null
    private var databaseVersion: Int = 1

    private var databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)? = null

    private lateinit var commonExecutor : ExecutorService

    private lateinit var entityFactory: EntityFactory

    /**
     * Initialize database
     *
     * @param context The context to create database
     * @param databaseName The database name to be used for the App
     * @param entityFactory  Used to create entity from class name and values map
     * @param version database version
     * @param databaseCreatedCallback Can be used to prefill db on create
     * @param databaseUpgradeCallback If db upgrade is necessary, this is to be handled
     */
    fun init(
        context: Context, databaseName: String,
        entityFactory: EntityFactory,
        useContentProvider: Boolean = this.useContentProvider,
        version: Int = 1,
        databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)? = null,
        databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)? = null
    ) {
        commonExecutor = Executors.newCachedThreadPool()
        this.entityFactory = entityFactory
        if (sqliteOpenHelper != null)
            return
        this.useContentProvider = useContentProvider
        this.context = context
        this.databaseName = databaseName
        this.databaseVersion = version
        this.databaseUpgradeCallback = databaseUpgradeCallback
        //calling the database name listeners
        synchronized(this) {
            dbDetailsListeners.forEach { it.invoke(databaseName, version, databaseUpgradeCallback) }
        }
        sqliteOpenHelper = object : SQLiteOpenHelper(context, databaseName, null, version) {
            init {
                //listeners won't be fired else
                writableDatabase
            }

            override fun onCreate(database: SQLiteDatabase?) {
                this@RudderDatabase.database = database
                database?.let {
                    initDaoList(database, registeredDaoList.values.toList())
                }
                databaseCreatedCallback?.invoke(database)
            }

            override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                databaseUpgradeCallback?.invoke(database, oldVersion, newVersion)
            }

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
        entityClass: Class<T>, executorService: ExecutorService = commonExecutor

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
        entityClass: Class<T>, executorService: ExecutorService

    ): Dao<T> = Dao<T>(entityClass, useContentProvider, context?:
    throw UninitializedPropertyAccessException("Did you call RudderDatabase.init?"),
        entityFactory, executorService).also {
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
            String, Int,
            databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)?
        ) -> Unit
    ) {

        databaseName?.let {
            callback.invoke(it, databaseVersion, databaseUpgradeCallback)
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
        registeredDaoList.clear() //clearing all cached dao
        database?.apply{
            //synchronizing on database allows other database users to synchronize on the same
            synchronized(this) {
                sqliteOpenHelper?.close()
                database?.close()
                database = null
            }
        }
        sqliteOpenHelper = null
        commonExecutor.shutdown()
        dbDetailsListeners = emptyList()
        databaseUpgradeCallback = null
        useContentProvider = false
    }

}