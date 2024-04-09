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
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import com.rudderstack.android.repository.annotation.RudderEntity
import com.rudderstack.android.repository.annotation.RudderField
import com.rudderstack.android.repository.utils.getInsertedRowIdForConflictIgnore
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Dao for accessing entities
 *
 * @param T The type of entity this dao is associated to
 * @property entityClass
 * @property entityFactory An implementation of EntityFactory to provide Entities based on database stored values
 * @property executorService An executor service to run the database queries.
 * TODO create separate objects for database and content provider
 */
class Dao<T : Entity>(
    internal val entityClass: Class<T>,
    private val useContentProvider: Boolean,
    private val context: Context,
    private val entityFactory: EntityFactory,
    private val executorService: ExecutorService,
    private val databaseName: String
) {

    companion object {
        private val DB_LOCK = Any()
    }

    private val todoLock = ReentrantLock(true)
    private val insertionLock = ReentrantLock(true)

    private // create fields statement
    val fields =
        entityClass.getAnnotation(RudderEntity::class.java)?.fields?.takeIf { it.isNotEmpty() }
        ?: throw IllegalArgumentException("There should be at least one field in @Entity")

    private val tableName: String = entityClass.getAnnotation(RudderEntity::class.java)?.tableName
                                    ?: throw IllegalArgumentException(
                                        "${entityClass.simpleName} is being used to generate Dao, " + "but missing @RudderEntity annotation",
                                    )

    private var _db: SQLiteDatabase? = null
        get() = if (field?.isOpen == true) field else null
    private var todoTransactions: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val _dataChangeListeners = HashSet<DataChangeListener<T>>()

    private val entityContentProviderUri by lazy {
        EntityContentProvider.getContentUri(tableName, context).buildUpon().appendQueryParameter(
            EntityContentProvider.ECP_ENTITY_CODE,
            entityClass.name,
        ).appendQueryParameter(EntityContentProvider.ECP_DATABASE_CODE, databaseName)
    }

    init {
        if (useContentProvider) EntityContentProvider.registerTableCommands(
            databaseName, createTableStmt(
                tableName, fields
            ), createIndexStmt(tableName, fields)
        )
    }

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
        insertCallback: ((rowIds: List<Long>) -> Unit)? = null,
    ) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            val (rowIds, _) = insertData(db, this, conflictResolutionStrategy)
            insertCallback?.invoke(rowIds)
        }
    }

    /**
     * Same as above, except this calls back with a map of rowId to entity
     * In case the row id is -1, i.e the entity could not be inserted, we
     * will return the original event mapped to -1
     *
     * @param conflictResolutionStrategy
     * @param insertCallback
     */
    fun List<T>.insertWithDataCallback(
        conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.CONFLICT_NONE,
        insertCallback: ((rowIds: List<Long>, data: List<T?>) -> Unit)? = null,
    ) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            val (rowIds, insertedData) = insertData(db, this, conflictResolutionStrategy)
            insertCallback?.invoke(rowIds, insertedData)
        }
    }

    // will return null if db is not yet ready
    fun List<T>.insertSync(
        conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.CONFLICT_NONE,
    ): List<Long>? {
        awaitDbInitialization()
        return (_db?.let { db ->
            insertData(db, this, conflictResolutionStrategy).first
        })
    }

    fun List<T>.deleteSync(): Int {
        val fields = entityClass.getAnnotation(RudderEntity::class.java)?.fields

        val args = findArgumentsFromEntities()
        val whereClause = deleteClauseFromFields(fields, args)

        return deleteSync(whereClause, null).also {
            notifyDelete(it)
        }
    }

    fun List<T>.delete(deleteCallback: ((numberOfRows: Int) -> Unit)? = null) {
        runTransactionOrDeferToCreation { _ ->
            val fields = entityClass.getAnnotation(RudderEntity::class.java)?.fields

            val args = findArgumentsFromEntities()
            val whereClause = deleteClauseFromFields(fields, args)

            // receives the number of deleted rows and fires callback
            val extendedDeleteCb = { numberOfRows: Int ->
                deleteCallback?.invoke(numberOfRows)
                notifyDelete(numberOfRows)
            }
            delete(whereClause, null, extendedDeleteCb)
        }
    }

    private fun List<T>.notifyDelete(numberOfRows: Int) {
        if (_dataChangeListeners.isNotEmpty()) {
            val allData = getAllSync() ?: listOf()
            _dataChangeListeners.forEach {
                it.onDataDeleted(this.subList(0, numberOfRows), allData)
            }
        }
    }

    private fun deleteClauseFromFields(
        fields: Array<RudderField>?, args: List<String>
    ) = fields?.takeIf {
        it.isNotEmpty()
    }?.filter {
        it.primaryKey
    }?.mapIndexed { index, it ->
        "${it.fieldName} IN (${args[index]})"
    }?.reduce { acc, s -> "$acc AND $s" }

    private fun List<T>.findArgumentsFromEntities() = map {
        it.getPrimaryKeyValues().map {
            "\"${it}\""
        }
    }.reduce { acc, strings ->
        acc.mapIndexed { index, s ->

            "$s, ${strings[index]}"
        }
    }
    // delete
    /**
     * Delete based on where clause
     *
     * @param whereClause Used for selecting items for deletion
     * @param args Substituting arguments if any, else null
     * @return number of rows deleted
     */
    fun deleteSync(
        whereClause: String?, args: Array<out String>?
    ): Int {
        awaitDbInitialization()
        return _db?.let { db ->
            deleteFromDb(db, tableName, whereClause, args)
        } ?: -1
    }
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
        deleteCallback: ((numberOfRows: Int) -> Unit)? = null,
    ) {
        runTransactionOrDeferToCreation { db ->
            val deletedRows = deleteFromDb(db, tableName, whereClause, args)
            deleteCallback?.invoke(deletedRows)
        }
    }

    internal fun deleteFromDb(
        database: SQLiteDatabase,
        tableName: String,
        whereClause: String?,
        args: Array<out String>?,
    ): Int {
        return if (useContentProvider) {
            context.contentResolver.delete(
                entityContentProviderUri.build(),
                whereClause,
                args,
            )
        } else synchronized(DB_LOCK) {
            database.openDatabase?.delete(tableName, whereClause, args)
        } ?: -1
    }

    internal fun updateSync(
        database: SQLiteDatabase,
        tableName: String,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        return if (useContentProvider) {
            context.contentResolver.update(
                entityContentProviderUri.build(),
                values,
                selection,
                selectionArgs,
            )
        } else synchronized(DB_LOCK) {
            database.openDatabase?.update(tableName, values, selection, selectionArgs)
        } ?: -1
    }

    fun getAll(callback: (List<T>) -> Unit) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            callback.invoke(getItems(db, null))
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
        awaitDbInitialization()
        return (_db?.let { getItems(it) })
    }

    private fun awaitDbInitialization() {
        while (_db == null){
            // busy wait until _db is assigned
        }
        todoLock.lock()
        todoLock.unlock()
    }

    fun runGetQuery(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        orderBy: String? = null,
        limit: String? = null,
        offset: String? = null,
        callback: (List<T>) -> Unit,
    ) {
        runTransactionOrDeferToCreation { _: SQLiteDatabase ->
            callback.invoke(
                runGetQuerySync(columns, selection, selectionArgs, orderBy, limit, offset)
                ?: listOf(),
            )
        }
    }

    /**
     * Get all in sync
     *
     * @return all data and null if database is not ready yet
     */
    fun runGetQuerySync(
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        orderBy: String? = null,
        limit: String? = null,
        offset: String? = null,
    ): List<T>? {
        awaitDbInitialization()
        return getItems(
            _db ?: return null,
            columns,
            selection,
            selectionArgs,
            orderBy,
            limit,
            offset,
        )
    }

    fun getCount(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        callback: (Long) -> Unit,
    ) {
        runTransactionOrDeferToCreation { db ->
            getCountSync(db, selection, selectionArgs).apply(callback)
        }
    }

    private fun getCountSync(
        db: SQLiteDatabase,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
    ): Long {
        awaitDbInitialization()
        return if (useContentProvider) {
            (context.contentResolver.query(
                entityContentProviderUri.build(),
                arrayOf("count(*)"),
                selection,
                selectionArgs,
                null,
            )?.use { cursor ->
                cursor.moveToFirst()
                cursor.getLong(0)
            } ?: -1L)
        } else {
            synchronized(DB_LOCK) {
                DatabaseUtils.queryNumEntries(
                    db,
                    tableName,
                    selection,
                    selectionArgs,
                )
            }
        }
    }

    // create/update

    private fun insertData(
        db: SQLiteDatabase,
        items: List<T>,
        conflictResolutionStrategy: ConflictResolutionStrategy,
    ): Pair<List<Long>, List<T?>> {
        synchronized(DB_LOCK) {
            if (!db.isOpen) return emptyList<Long>() to emptyList()
        }
        insertionLock.lock()
        val rowIdsToInsertedItems = processEntityInsertion(db, conflictResolutionStrategy, items)
        insertionLock.unlock()
        return rowIdsToInsertedItems
    }

    private fun processEntityInsertion(
        db: SQLiteDatabase,
        conflictResolutionStrategy: ConflictResolutionStrategy,
        items: List<T>,
    ): Pair<List<Long>, List<T?>> {
        var (autoIncrementFieldName: String?, nextValue: Long) = getAutoIncrementFieldToNextValue(db)
        var dbCount =
            if (conflictResolutionStrategy == ConflictResolutionStrategy.CONFLICT_IGNORE) {
                getCountSync(
                    db,
                )
            } else {
                0L
            }
        var rowIds = listOf<Long>()
        var returnedItems = listOf<T?>()
        synchronized(DB_LOCK) {
            items.forEach {
                val contentValues = it.generateContentValues()
                if (autoIncrementFieldName != null) {
                    contentValues.put(autoIncrementFieldName, nextValue)
                }

                val insertedRowId = insertContentValues(
                    db,
                    tableName,
                    contentValues,
                    null,
                    conflictResolutionStrategy.type,
                ).let {
                    if (conflictResolutionStrategy == ConflictResolutionStrategy.CONFLICT_IGNORE) {
                        getInsertedRowIdForConflictIgnore(dbCount, it)
                    } else {
                        it
                    }
                }.also {
                    if (it >= 0) {
                        nextValue++
                        dbCount++
                    }
                }
                rowIds = rowIds + insertedRowId
                returnedItems = returnedItems + (if (insertedRowId < 0) {
                    it
                } else contentValues.toEntity(
                    entityClass,
                ))
            }
        }

        if (returnedItems.isNotEmpty() && _dataChangeListeners.isNotEmpty()) {
            val allData = getAllSync() ?: listOf()
            _dataChangeListeners.forEach {
                it.onDataInserted(returnedItems.filterNotNull(), allData)
            }
        }
        return rowIds to returnedItems
    }

    // we consider one key which is auto increment.
    // consider only one auto increment key
    private fun getAutoIncrementFieldToNextValue(db: SQLiteDatabase) = fields.firstOrNull {
        it.type == RudderField.Type.INTEGER && it.isAutoInc /*&& !it.primaryKey*/
    }?.let { autoIncField ->
        autoIncField.fieldName to getMaxIntValueForColumn(
            db,
            tableName,
            autoIncField.fieldName,
        ) + 1L
    } ?: (null to 0L)

    private fun <T : Entity> ContentValues.toEntity(classOfT: Class<T>): T? {
        return entityFactory.getEntity(classOfT, this.toMap())
    }

    private fun ContentValues.toMap(): Map<String, Any?> {
        return keySet().associateWith { get(it) }
    }

    // this method considers database is open and is available for query
    // -1 if no value present
    private fun getMaxIntValueForColumn(
        db: SQLiteDatabase,
        tableName: String,
        column: String,
    ): Long {
        return (if (useContentProvider) {
            context.contentResolver.query(
                entityContentProviderUri.build(),
                arrayOf("IFNULL(MAX($column), 0)"),
                null,
                null,
                null
            )
        } else synchronized(DB_LOCK) {
            db.query(
                tableName,
                arrayOf("IFNULL(MAX($column), 0)"),
                null,
                null,
                null,
                null,
                null,
            )
        })?.let { cursor ->
            (if (cursor.moveToFirst()) {
                cursor.getLong(0)
            } else {
                -1
            }).also {
                cursor.close()
            }
        } ?: -1
    }

    internal fun insertContentValues(
        database: SQLiteDatabase,
        tableName: String,
        contentValues: ContentValues,
        nullHackColumn: String?,
        conflictAlgorithm: Int,
    ): Long {
        return if (useContentProvider) {
            (context.contentResolver.insert(
                entityContentProviderUri.appendQueryParameter(
                    EntityContentProvider.ECP_CONFLICT_RESOLUTION_CODE,
                    conflictAlgorithm.toString(),
                ).appendQueryParameter(
                    EntityContentProvider.ECP_NULL_HACK_COLUMN_CODE,
                    conflictAlgorithm.toString(),
                ).build(),
                contentValues,
            )?.let {
                it.lastPathSegment?.toLongOrNull()
            } ?: -1)
        } else {
            (database.openDatabase?.insertWithOnConflict(
                tableName,
                nullHackColumn,
                contentValues,
                conflictAlgorithm,
            ) ?: -1)
        }
    }

    // read
    private fun getItems(
        db: SQLiteDatabase,
        columns: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        orderBy: String? = null,
        limit: String? = null,
        offset: String? = null,
    ): List<T> {
        // have to use factory
        val fields = entityClass.getAnnotation(RudderEntity::class.java)?.fields
                     ?: throw IllegalArgumentException("RudderEntity must have at least one field")
        val cursor = (if (useContentProvider) {
            context.contentResolver.query(
                entityContentProviderUri.appendQueryParameter(
                    EntityContentProvider.ECP_LIMIT_CODE, limit
                ).build(),
                columns,
                selection,
                selectionArgs,
                orderBy,
            )
        } else {
            synchronized(DB_LOCK) {
                db.openDatabase?.query(
                    tableName,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    orderBy,
                    if (offset != null) "$offset,$limit" else limit,
                )
            }
        }) ?: return listOf()

        val items = ArrayList<T>(cursor.count)

        if (cursor.moveToFirst()) {
            do {
                fields.associate {
                    Pair(it.fieldName, it.findValue(cursor))
                }.let {
                    entityFactory.getEntity(entityClass, it)
                }?.apply {
                    items.add(this)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        return items
//        }
    }

    private fun runTransactionOrDeferToCreation(queryTransaction: (SQLiteDatabase) -> Unit) {
        _db?.let { db ->
            awaitDbInitialization()
            executorService.takeUnless { it.isShutdown }?.execute {
                queryTransaction.invoke(db)
            }
        } ?: run {
            Runnable {
                _db?.let {
                    queryTransaction.invoke(it)
                }
            }.also {
                todoTransactions.put(it)
            }
        }
    }

    fun setDatabase(sqLiteDatabase: SQLiteDatabase?) {
        if (sqLiteDatabase == null) return

        // run all pending tasks
        executorService.execute {
            synchronized(DB_LOCK) {
                val tableStmt = createTableStmt(tableName, fields)
                sqLiteDatabase.openDatabase?.execSQL(tableStmt)
                createIndexStmt(tableName, fields)?.apply {
                    sqLiteDatabase.openDatabase?.execSQL(this)
                }
                _db = sqLiteDatabase
                todoLock.lock()
            }
            while (todoTransactions.isNotEmpty()) {
                try {
                    executorService.takeUnless { it.isShutdown }?.submit(
                        todoTransactions.poll(
                            50,
                            TimeUnit.MILLISECONDS,
                        ),
                    )
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
            }
            todoLock.unlock()
        }
    }

    fun execSqlSync(command: String) {
        awaitDbInitialization()
        synchronized(DB_LOCK) {
            _db?.openDatabase?.execSQL(command)
        }
    }

    private fun beginTransaction() {
        awaitDbInitialization()
        _db?.openDatabase?.beginTransaction()
    }

    private fun setTransactionSuccessful() {
        _db?.openDatabase?.setTransactionSuccessful()
    }

    private fun endTransaction() {
        _db?.openDatabase?.endTransaction()
    }

    fun execTransaction(transaction: () -> Unit) {
        synchronized(DB_LOCK) {
            beginTransaction()
            transaction.invoke()
            setTransactionSuccessful()
            endTransaction()
        }
    }

    fun execSql(command: String, callback: (() -> Unit)? = null) {
        runTransactionOrDeferToCreation { db: SQLiteDatabase ->
            synchronized(DB_LOCK) {
                db.openDatabase?.execSQL(command)
                callback?.invoke()
            }
        }
    }

    private fun createTableStmt(tableName: String, fields: Array<RudderField>): String? {
        val fieldsStmt = fields.map {
            "'${it.fieldName}' ${it.type.notation}" + // field name and type
            // if primary and auto increment
            /*if (it.primaryKey && it.isAutoInc && it.type == RudderField.Type.INTEGER) " PRIMARY KEY AUTOINCREMENT" else "" +*/
            if (!it.isNullable || it.primaryKey) " NOT NULL" else "" // specifying nullability, primary key cannot be null
        }.reduce { acc, s -> "$acc, $s" }
        val primaryKeyStmt =
            // auto increment is only available for one primary key
            fields.filter { it.primaryKey }.takeIf { !it.isNullOrEmpty() }?.map {
                it.fieldName
            }?.reduce { acc, s -> "$acc,$s" }?.let {
                "PRIMARY KEY ($it)"
            } ?: ""
        val uniqueKeyStmt =
            fields.filter { it.isUnique }.takeIf { it.isNotEmpty() }?.joinToString(",") {
                it.fieldName
            }?.let {
                "UNIQUE($it)"
            }

        return ("CREATE TABLE IF NOT EXISTS '$tableName' ($fieldsStmt ${if (primaryKeyStmt.isNotEmpty()) ", $primaryKeyStmt" else ""}" + "${if (!uniqueKeyStmt.isNullOrEmpty()) ", $uniqueKeyStmt" else ""})")
    }

    private fun createIndexStmt(tableName: String, fields: Array<RudderField>): String? {
        val indexedFields = fields.filter {
            it.isIndex
        }.takeIf {
            it.isNotEmpty()
        } ?: return null
        val indexFieldsStmt = indexedFields.map {
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
        return "CREATE INDEX IF NOT EXISTS $indexName ON $tableName $indexFieldsStmt"
    }

    private fun RudderField.findValue(cursor: Cursor) = when (type) {
        RudderField.Type.INTEGER -> if (isNullable) {
            cursor.getLongOrNull(
                cursor.getColumnIndex(fieldName).takeIf { it >= 0 }
                ?: -1,
            )
        } else cursor.getLong(
            cursor.getColumnIndex(fieldName).takeIf { it >= 0 }
            ?: throw IllegalArgumentException("No such column $fieldName"),
        )

        RudderField.Type.TEXT -> if (isNullable) {
            cursor.getStringOrNull(
                cursor.getColumnIndex(fieldName).takeIf { it >= 0 }
                ?: -1,
            )
        } else cursor.getString(
            cursor.getColumnIndex(fieldName).takeIf { it >= 0 }
            ?: throw IllegalArgumentException("No such column $fieldName"),
        )
    }

    private val SQLiteDatabase.openDatabase
        get() = this.takeIf {
            it.isOpen
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

private fun Cursor.getStringOrNull(columnIndex: Int): String? {
    return try {
        getString(columnIndex)
    } catch (_: Exception) {
        null
    }
}

private fun Cursor.getLongOrNull(colIndex: Int): Long? {
    return try {
        getLong(colIndex)
    } catch (_: Exception) {
        null
    }
}
