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

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * Dao for accessing entities
 *
 * @param T The type of entity this dao is associated to
 * @property entityClass
 * @property entityFactory An implementation of EntityFactory to provide Entities based on database stored values
 * @property executorService An executor service to run the database queries.
 */
class Dao<T : Entity> internal constructor(
    internal val entityClass: Class<T>,
    private val useContentProvider : Boolean,
    private val entityFactory: EntityFactory,
    private val executorService: ExecutorService
) {

    private val tableName: String = entityClass.getAnnotation(RudderEntity::class.java)?.tableName
        ?: throw IllegalArgumentException(
            "${entityClass.simpleName} is being used to generate Dao, " +
                    "but missing @RudderEntity annotation"
        )

    private var _db: SQLiteDatabase? = null
    private var todoTransactions: MutableList<Future<*>> = ArrayList(5)
    private val _dataChangeListeners = HashSet<DataChangeListener<T>>()


    /**
     * usage
     * with(dao){
     *  entity.insert(){ rowIds ->
     *  }
     * }
     *
     * @param conflictResolutionStrategy Strategy to follow in case of insertion conflict
     */

    fun List<T>.insert(
        conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.CONFLICT_NONE,
        insertCallback: ((rowIds: List<Long>) -> Unit)? = null
    ) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            val rowIds = insertDbSync(db, this, conflictResolutionStrategy)
            insertCallback?.invoke(rowIds)
        }

    }

    //will return null if db is not yet ready
    fun List<T>.insertSync(
        conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.CONFLICT_NONE
    ): List<Long>? {
        return _db?.let { db ->
            insertDbSync(db, this, conflictResolutionStrategy)
        }

    }



    fun List<T>.delete(deleteCallback: ((numberOfRows: Int) -> Unit)? = null) {

        runTransactionOrDeferToCreation { _ ->
            val fields = entityClass.getAnnotation(RudderEntity::class.java)?.fields

            val args = map {
                it.getPrimaryKeyValues()
            }.reduce { acc, strings ->
                acc.mapIndexed { index, s ->

                    "\"$s\", \"${strings[index]}\""
                }.toTypedArray()
            }
            val whereClause = fields?.takeIf {
                it.isNotEmpty()
            }?.filter {
                it.primaryKey
            }?.mapIndexed { index, it ->
                "${it.fieldName} IN (${args[index]})"
            }?.reduce { acc, s -> "$acc AND $s" }

            // receives the number of deleted rows and fires callback
           val extendedDeleteCb = { numberOfRows :Int ->
               deleteCallback?.invoke(numberOfRows)
               val allData = getAllSync() ?: listOf()
               _dataChangeListeners.forEach {
                   it.onDataDeleted(this.subList(0,numberOfRows), allData)
               }
           }
            delete(whereClause, null, extendedDeleteCb)


        }
    }
    //delete
    /**
     * val args = map {
    it.getPrimaryKeyValues()
    }
    val whereClause = fields?.takeIf {
    it.isNotEmpty()
    }?.filter {
    it.primaryKey
    }?.mapIndexed { index, it ->
    "${it.fieldName} IN (${args[index]})"
    }?.reduce { acc, s -> "$acc AND $s" }
    val numberOfRowsDel = db.delete(tableName,whereClause, null)
    deleteCallback?.invoke(numberOfRowsDel)

    val allData = getAllSync() ?: listOf()
    _dataChangeListeners.forEach {
    it.onDataDeleted(this.subList(0,numberOfRowsDel), allData)
    }
     */
    /**
     * Delete based on where clause
     *
     * @param whereClause Used for selecting items for deletion
     * @param args Substituting arguments if any, else null
     * @param deleteCallback
     */
    fun delete(
        whereClause: String?,
        args: Array<out String>?,
        deleteCallback: ((numberOfRows: Int) -> Unit)? = null
    ) {
        runTransactionOrDeferToCreation { db ->
            val deletedRows = synchronized(this){ deleteFromDb(db, tableName, whereClause, args) }
            deleteCallback?.invoke(deletedRows)
        }
    }
    internal fun deleteFromDb(database: SQLiteDatabase,
                              tableName: String, whereClause: String?,args: Array<out String>? ) : Int{
        return database.delete(tableName, whereClause, args)
    }

    internal fun updateSync(database: SQLiteDatabase, tableName: String, values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<out String>?) : Int{
        return database.update(tableName, values, selection, selectionArgs)
    }

    fun getAll(callback: (List<T>) -> Unit) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            callback.invoke(getItems(db, "SELECT * FROM $tableName"))
        }

    }

    /**
     * Listen to any data change, like add or delete data
     *
     * @param listener An instance of DataChangeListener
     * @see DataChangeListener
     */
    fun addDataChangeListener(listener: DataChangeListener<T>) {
        _dataChangeListeners.add(listener)
    }

    /**
     * Remove Data Change Listener
     *
     * @param listener An instance of DataChangeListener
     * @see DataChangeListener
     */
    fun removeDataChangeListener(listener: DataChangeListener<T>) {
        _dataChangeListeners.remove(listener)
    }

    /**
     * Clear all data change listeners.
     *
     *
     * @see DataChangeListener
     */
    fun removeAllDataChangeListeners() {
        _dataChangeListeners.clear()
    }

    /**
     * Get all in sync
     *
     * @return all data and null if database is not ready yet
     */
    fun getAllSync(): List<T>? {
        return _db?.let { getItems(it, "SELECT * FROM $tableName") }
    }

    fun runGetQuery(query: String, callback: (List<T>) -> Unit) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            callback.invoke(getItems(db, query))
        }
    }

    /**
     * Get all in sync
     *
     * @return all data and null if database is not ready yet
     */
    fun runGetQuerySync(query: String): List<T>? {
        return getItems(_db ?: return null, query)
    }

    //create/update
    private fun insertDbSync(
        db: SQLiteDatabase,
        items: List<T>,
        conflictResolutionStrategy: ConflictResolutionStrategy
    ): List<Long> {
        return items.map {
            insertContentValues(db, tableName, it.generateContentValues(),null, conflictResolutionStrategy )
        }.also {
            val allData = getAllSync() ?: listOf()
            _dataChangeListeners.forEach {
                it.onDataInserted(items, allData)
            }
        }
    }

    internal fun insertContentValues(database: SQLiteDatabase,
                                    tableName: String, contentValues: ContentValues, nullHackColumn: String?,
                                    conflictResolutionStrategy: Dao.ConflictResolutionStrategy) : Long{
        return synchronized(this) {
            database.insertWithOnConflict(
                tableName,
                nullHackColumn,
                contentValues,
                conflictResolutionStrategy.type
            )
        }

    }

    //read
    private fun getItems(db: SQLiteDatabase, query: String): List<T> {
        //have to use factory
        val fields = entityClass.getAnnotation(RudderEntity::class.java)?.fields
            ?: throw IllegalArgumentException("RudderEntity must have at least one field")

        val cursor = synchronized(this) { db.rawQuery(query, arrayOf()) }
        val items = ArrayList<T>(cursor.count)

        if (cursor.moveToFirst()) {
            do {
                fields.associate {
                    val value = when (it.type) {
                        RudderField.Type.INTEGER -> if(it.isNullable)cursor.getIntOrNull(
                            cursor.getColumnIndex(it.fieldName).takeIf { it >= 0 }
                                ?: throw IllegalArgumentException("No such column ${it.fieldName}")
                        )else cursor.getInt(
                            cursor.getColumnIndex(it.fieldName).takeIf { it >= 0 }
                                ?: throw IllegalArgumentException("No such column ${it.fieldName}")
                        )
                        RudderField.Type.TEXT -> if(it.isNullable)cursor.getStringOrNull(
                            cursor.getColumnIndex(it.fieldName).takeIf { it >= 0 }
                                ?: throw IllegalArgumentException("No such column ${it.fieldName}"))
                        else cursor.getString(
                            cursor.getColumnIndex(it.fieldName).takeIf { it >= 0 }
                                ?: throw IllegalArgumentException("No such column ${it.fieldName}"))
                    }
                    Pair(it.fieldName, value)
                }.let {
                    entityFactory.getEntity(entityClass, it)
                }?.apply {
                    items.add(this)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    private fun runTransactionOrDeferToCreation(queryTransaction: (SQLiteDatabase) -> Unit) {
        _db?.let { db ->
            executorService.execute {
                synchronized(this) {
                    queryTransaction.invoke(db)
                }
            }
        } ?: run {
            executorService.submit {
                _db?.let {
                    synchronized(this) {
                        queryTransaction.invoke(it)
                    }
                }
            }.also {
                todoTransactions.add(it)
            }
        }
    }

    internal fun setDatabase(sqLiteDatabase: SQLiteDatabase?) {
        //create fields statement
        val fields =
            entityClass.getAnnotation(RudderEntity::class.java)?.fields?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("There should be at least one field in @Entity")
        //create table if not exist
        val createTableStmt = createTableStmt(tableName, fields)
        val indexStmt = createIndexStmt(tableName, fields)
        _db = sqLiteDatabase
        //run all pending tasks
        executorService.execute {
            synchronized(this) {
                sqLiteDatabase?.execSQL(createTableStmt)
                indexStmt?.apply {
                    sqLiteDatabase?.execSQL(indexStmt)
                }
            }
            todoTransactions.forEach {
                it.get()
            }
        }
    }

    private fun createTableStmt(tableName: String, fields: Array<RudderField>): String? {

        var isAutoIncKeyPresent = false
        val fieldsStmt = fields.map {
            if (it.isAutoInc)
                isAutoIncKeyPresent = true
            "'${it.fieldName}' ${it.type.notation} " + //field name and type
                    // if primary and auto increment
                    if (it.primaryKey && it.isAutoInc && it.type == RudderField.Type.INTEGER) " PRIMARY KEY AUTOINCREMENT" else "" +
                            if (!it.isNullable && !it.primaryKey) " NOT NULL" else "" //specifying nullability, primary key cannot be null
        }.reduce { acc, s -> "$acc, $s" }
        val primaryKeyStmt =
            if (isAutoIncKeyPresent) "" else { //auto increment is only available for one primary key
                fields.filter { it.primaryKey }.takeIf { !it.isNullOrEmpty() }?.map {
                    it.fieldName
                }?.reduce { acc, s -> "$acc,$s" }?.let {
                    "PRIMARY KEY ($it)"
                } ?: ""
            }

        return "CREATE TABLE IF NOT EXISTS '$tableName' ($fieldsStmt ${if (primaryKeyStmt.isNotEmpty()) ", $primaryKeyStmt" else ""})"

    }

    private fun createIndexStmt(tableName: String, fields: Array<RudderField>): String? {
        val indexedFields = fields.filter {
            it.isIndex
        }.takeIf {
            it.isNotEmpty()
        } ?: return null
        val indexFieldsStmt = indexedFields.map {
//            it.indexName.takeIf { it.isNotEmpty() }?:"${it.fieldName}_idx"
            it.fieldName
        }.reduce { acc, s ->
            "$acc,$s"
        }.let {
            "($it)"
        }
        val indexName = indexedFields.map {
            "${it.fieldName}_"
        }.reduce { acc, s -> "$acc$s" }.let {
            "${it}_idx"
        }
        return "CREATE INDEX $indexName ON $tableName $indexFieldsStmt"
    }

    enum class ConflictResolutionStrategy(val type: Int) {

        /**
         * When a constraint violation occurs, an immediate ROLLBACK occurs,
         * thus ending the current transaction, and the command aborts with a
         * return code of SQLITE_CONSTRAINT. If no transaction is active
         * (other than the implied transaction that is created on every command)
         * then this algorithm works the same as ABORT.
         */
        CONFLICT_ROLLBACK(SQLiteDatabase.CONFLICT_ROLLBACK),

        /**
         * When a constraint violation occurs,no ROLLBACK is executed
         * so changes from prior commands within the same transaction
         * are preserved. This is the default behavior.
         */
        CONFLICT_ABORT(SQLiteDatabase.CONFLICT_ABORT),

        /**
         * When a constraint violation occurs, the command aborts with a return
         * code SQLITE_CONSTRAINT. But any changes to the database that
         * the command made prior to encountering the constraint violation
         * are preserved and are not backed out.
         */
        CONFLICT_FAIL(SQLiteDatabase.CONFLICT_FAIL),

        /**
         * When a constraint violation occurs, the one row that contains
         * the constraint violation is not inserted or changed.
         * But the command continues executing normally. Other rows before and
         * after the row that contained the constraint violation continue to be
         * inserted or updated normally. No error is returned.
         */
        CONFLICT_IGNORE(SQLiteDatabase.CONFLICT_IGNORE),

        /**
         * When a UNIQUE constraint violation occurs, the pre-existing rows that
         * are causing the constraint violation are removed prior to inserting
         * or updating the current row. Thus the insert or update always occurs.
         * The command continues executing normally. No error is returned.
         * If a NOT NULL constraint violation occurs, the NULL value is replaced
         * by the default value for that column. If the column has no default
         * value, then the ABORT algorithm is used. If a CHECK constraint
         * violation occurs then the IGNORE algorithm is used. When this conflict
         * resolution strategy deletes rows in order to satisfy a constraint,
         * it does not invoke delete triggers on those rows.
         * This behavior might change in a future release.
         */
        CONFLICT_REPLACE(SQLiteDatabase.CONFLICT_REPLACE),

        /**
         * Use the following when no conflict action is specified.
         */
        CONFLICT_NONE(SQLiteDatabase.CONFLICT_NONE),
    }

    interface DataChangeListener<T : Any> {
        fun onDataInserted(inserted: List<T>, allData: List<T>) {
            /**
             * Implementation can be ignored
             */
        }

        fun onDataDeleted(deleted: List<T>, allData: List<T>) {
            /**
             * Implementation can be ignored
             */
        }

    }
}