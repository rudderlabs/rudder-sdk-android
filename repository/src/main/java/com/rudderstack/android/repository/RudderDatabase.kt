package com.rudderstack.android.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object RudderDatabase {
    private var sqliteOpenHelper: SQLiteOpenHelper? = null
    private var database: SQLiteDatabase? = null
    private var registeredDaoList = HashMap<Class<out Entity>, Dao<out Entity>>(20)

    fun init(
        context: Context, databaseName: String, version: Int = 1,
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
        } ?: Dao<T>(entityClass, executorService).also{
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