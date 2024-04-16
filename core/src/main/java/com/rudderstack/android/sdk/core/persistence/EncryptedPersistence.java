package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.rudderstack.android.sdk.core.RudderLogger;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Encrypted persistence implementation of {@link Persistence}
 * Uses net.sqlcipher.database.* classes
 */
// START-NO-SONAR-SCAN
public class EncryptedPersistence extends SQLiteOpenHelper implements Persistence {
    private final List<DbCloseListener> dbCloseListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final DbCreateListener dbCreateListener;
    private SQLiteDatabase initialDatabase = null;


    EncryptedPersistence(Application application, DbParams params, @Nullable DbCreateListener dbCreateListener) {
        super(application, params.dbName, params.encryptPassword,null, params.dbVersion,
                0, null, null, false);
        this.dbCreateListener = dbCreateListener;
    }

    @NonNull
    public SQLiteDatabase getWritableDatabase() {
        if (initialDatabase != null) {
            return initialDatabase;
        }
        return super.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.initialDatabase = db;
        if(dbCreateListener != null){
            dbCreateListener.onDbCreate();
        }
        this.initialDatabase = null;
    }

    private void createSchema(SQLiteDatabase db, String eventSchemaSQL) {

        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createEventSchemaSQL: %s", eventSchemaSQL));
        db.execSQL(eventSchemaSQL);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    @Override
    public synchronized void close() {
        super.close();
        for (DbCloseListener listener : dbCloseListeners) {
            listener.onDbClose();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // We have decided not to implement this method currently
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no-op
    }

    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return getWritableDatabase().insert(table, nullColumnHack, values);
    }

    @Override
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException {
        return getWritableDatabase().insertOrThrow(table, nullColumnHack, values);
    }

    @Override
    public long replace(String table, String nullColumnHack, ContentValues initialValues) {
        return getWritableDatabase().replace(table, nullColumnHack, initialValues);
    }

    @Override
    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues)
            throws SQLException {
        return getWritableDatabase().replaceOrThrow(table, nullColumnHack, initialValues);
    }

    @Override
    public long insertWithOnConflict(String table, String nullColumnHack,
                                     ContentValues initialValues, int conflictAlgorithm) {
        return getWritableDatabase().insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return getWritableDatabase().delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return getWritableDatabase().update(table, values, whereClause, whereArgs);
    }

    @Override
    public int updateWithOnConflict(String table, ContentValues values, String whereClause, String[] whereArgs, int conflictAlgorithm) {
        return getWritableDatabase().updateWithOnConflict(table, values, whereClause, whereArgs, conflictAlgorithm);

    }

    @Override
    public void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        getWritableDatabase().setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        getWritableDatabase().endTransaction();
    }

    @Override
    public void execSQL(String sql) {
        getWritableDatabase().execSQL(sql);
    }

    @Override
    public void execSQL(String sql, Object[] bindArgs) {
        getWritableDatabase().execSQL(sql, bindArgs);
    }

    @Override
    public void deleteDatabase(File file) {
        android.database.sqlite.SQLiteDatabase.deleteDatabase(file);
    }

    @Override
    public void beginTransactionNonExclusive() {
        getWritableDatabase().beginTransactionNonExclusive();
    }


    @Override
    public Cursor query(boolean distinct, String table, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {
        return getWritableDatabase().query(distinct, table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }


    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return getWritableDatabase().query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy);
    }

    @Override
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return getWritableDatabase().query(table, columns, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return getWritableDatabase().rawQuery(sql, selectionArgs);
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs,
                           CancellationSignal cancellationSignal) {
        return rawQuery(sql, selectionArgs);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void validateSql(@NonNull String sql, @Nullable CancellationSignal cancellationSignal) {
        //not available in net.sqlcipher.database.SQLiteDatabase
    }

    @Override
    public boolean isReadOnly() {
        return getWritableDatabase().isReadOnly();
    }


    @Override
    public boolean needUpgrade(int newVersion) {
        return getWritableDatabase().needUpgrade(newVersion);
    }

    @Override
    public void setLocale(Locale locale) {
        getWritableDatabase().setLocale(locale);
    }

    @Override
    public void setMaxSqlCacheSize(int cacheSize) {
        getWritableDatabase().setMaxSqlCacheSize(cacheSize);
    }

    @Override
    public void setForeignKeyConstraintsEnabled(boolean enable) {
        getWritableDatabase().setForeignKeyConstraintsEnabled(enable);
    }

    @Override
    public boolean enableWriteAheadLogging() {
        return getWritableDatabase().enableWriteAheadLogging();
    }

    @Override
    public void disableWriteAheadLogging() {
        getWritableDatabase().disableWriteAheadLogging();
    }

    @Override
    public boolean isWriteAheadLoggingEnabled() {
        return getWritableDatabase().isWriteAheadLoggingEnabled();
    }

    @Override
    public List<Pair<String, String>> getAttachedDbs() {
        return getWritableDatabase().getAttachedDbs();
    }

    @Override
    public boolean isDatabaseIntegrityOk() {
        return getWritableDatabase().isDatabaseIntegrityOk();
    }


    @Override
    public void addDbCloseListener(DbCloseListener listener) {
        dbCloseListeners.add(listener);
    }

    @Override
    public boolean isAccessible() {
        return getWritableDatabase().isOpen();
    }

    static class DbParams {
        final String dbName;
        final int dbVersion;
        final @Nullable String encryptPassword;

        public DbParams(String dbName, int dbVersion) {
            this(dbName, dbVersion, null);
        }

        public DbParams(String dbName, int dbVersion,
                        @Nullable String encryptPassword) {
            this.dbName = dbName;
            this.dbVersion = dbVersion;
            this.encryptPassword = encryptPassword;
        }
    }
}
//END-NO-SONAR-SCAN