package com.rudderstack.android.sdk.core.persistence;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Default implementation of {@link Persistence}
 * Uses android.database.* classes
 */
public class DefaultPersistence extends SQLiteOpenHelper implements Persistence {
    private final List<DbCloseListener> dbCloseListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<DbCreateListener> dbCreateListeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    private SQLiteDatabase initialDatabase = null;


     DefaultPersistence(Application application, DbParams params) {
        super(application, params.dbName, null, params.dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         initialDatabase = db;
    for (DbCreateListener listener : dbCreateListeners) {
            listener.onDbCreate();
        }
        initialDatabase = null;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
         if(initialDatabase != null){
             return initialDatabase;
         }
        return super.getWritableDatabase();
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
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {
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

    // ... (Previous implementation)

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
        SQLiteDatabase.deleteDatabase(file);
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
        return getWritableDatabase().rawQuery(sql, selectionArgs, cancellationSignal);
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void validateSql(@NonNull String sql, @Nullable CancellationSignal cancellationSignal) {
        getWritableDatabase().validateSql(sql, cancellationSignal);
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
    public void addDbCreateListener(DbCreateListener listener) {
        dbCreateListeners.add(listener);
    }

    @Override
    public boolean isAccessible() {
        return getWritableDatabase().isOpen();
    }
    static class DbParams {
        final String dbName;
        final int dbVersion;

        public DbParams(String dbName, int dbVersion) {
            this.dbName = dbName;
            this.dbVersion = dbVersion;
        }

    }
}
