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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Singleton class to act as the Database helper
 */
object RudderDatabase {
    private var sqliteOpenHelper: SQLiteOpenHelper? = null
    private var database: SQLiteDatabase? = null
    private var registeredDaoList = HashMap<Class<out Entity>, Dao<out Entity>>(20)

    private val commonExecutor = Executors.newCachedThreadPool()

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
        version: Int = 1,
        databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)? = null,
        databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)? = null
    ) {
        this.entityFactory = entityFactory
        if (sqliteOpenHelper != null)
            return
//        context = application
//        this.databaseName = databaseName
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

    fun <T : Entity> getDao(
        entityClass: Class<T>, executorService: ExecutorService = commonExecutor

    ): Dao<T> {
        return registeredDaoList[entityClass]?./*.also {
//            it.executorService.shutdown()
            it.executorService = executorService
        }?*/let {
            it as Dao<T>
        } ?: Dao<T>(entityClass,  entityFactory, executorService).also{
            registeredDaoList[entityClass] = it
            database?.apply {
                initDaoList(this, listOf(it))
            }
        }
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
//        registeredDaoList.values.forEach {
//            it.close()
//        }
        sqliteOpenHelper?.close()
        sqliteOpenHelper = null
    }

}