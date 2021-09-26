/*
 * Creator: Debanjan Chatterjee on 24/09/21, 11:09 PM Last modified: 24/09/21, 8:31 PM
 * Copyright: All rights reserved Ⓒ 2021 http://hiteshsahu.com
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
import com.rudderstack.android.rudderjsonadapter.JsonAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object RudderDatabase {
    private var sqliteOpenHelper: SQLiteOpenHelper? = null
    private var database: SQLiteDatabase? = null
    private var registeredDaoList = HashMap<Class<out Entity>, Dao<out Entity>>(20)
    private lateinit var jsonAdapter : JsonAdapter
    fun init(
        context: Context, databaseName: String,
        jsonAdapter: JsonAdapter,
        version: Int = 1,
        databaseCreatedCallback: ((SQLiteDatabase?) -> Unit)? = null,
        databaseUpgradeCallback: ((SQLiteDatabase?, oldVersion: Int, newVersion: Int) -> Unit)? = null
    ) {
        if (sqliteOpenHelper != null)
            return
//        context = application
//        this.databaseName = databaseName

        sqliteOpenHelper = object : SQLiteOpenHelper(context, databaseName, null, version) {
            override fun onCreate(database: SQLiteDatabase?) {
                this@RudderDatabase.database = database
                initDaoList(registeredDaoList.values.toList())
                databaseCreatedCallback?.invoke(database)
            }

            override fun onUpgrade(database: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
                databaseUpgradeCallback?.invoke(database, oldVersion, newVersion)
            }
        }

        this.jsonAdapter = jsonAdapter
    }

    fun <T : Entity> getDao(
        entityClass: Class<T>, executorService: ExecutorService =
            Executors.newCachedThreadPool()
    ): Dao<T> {
        return registeredDaoList[entityClass]?.also {
            it.executorService.shutdown()
            it.executorService = executorService
        }?.let {
            it as Dao<T>
        } ?: Dao<T>(entityClass,  jsonAdapter, executorService).also{
            registeredDaoList[entityClass] = it
        }
    }


    fun <T : Entity> Dao<T>.unregister() {
        registeredDaoList.remove(entityClass)?.apply {
            close()
        }
    }

    private fun initDaoList(daoList: List<Dao<out Entity>>) {
        sqliteOpenHelper?.writableDatabase?.apply {
            daoList.forEach {
                it.setDatabase(this)
            }
        }
    }

}