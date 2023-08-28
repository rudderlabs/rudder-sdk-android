package com.rudderstack.android.sdk.core.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.os.CancellationSignal;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * An interface with all methods from @link{android.database.sqlite.SQLiteDatabase}
 */
public interface Persistence {


    void addDbCloseListener(DbCloseListener listener);


    boolean isAccessible();

    long insert(String table, String nullColumnHack, ContentValues values);

    long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException;

    long replace(String table, String nullColumnHack, ContentValues initialValues);

    long replaceOrThrow(String table, String nullColumnHack,
                        ContentValues initialValues) throws SQLException;

    long insertWithOnConflict(String table, String nullColumnHack,
                              ContentValues initialValues, int conflictAlgorithm);

    int delete(String table, String whereClause, String[] whereArgs);

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    int updateWithOnConflict(String table, ContentValues values,
                             String whereClause, String[] whereArgs, int conflictAlgorithm);

    void close();

    void beginTransaction();

    void setTransactionSuccessful();

    void endTransaction();

    void execSQL(String sql);

    void execSQL(String sql, Object[] bindArgs);

    void deleteDatabase(File file);

    void beginTransactionNonExclusive();

    Cursor query(boolean distinct, String table, String[] columns,
                 String selection, String[] selectionArgs, String groupBy,
                 String having, String orderBy, String limit);


    Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy);

    Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having,
                 String orderBy, String limit);

    Cursor rawQuery(String sql, String[] selectionArgs);

    Cursor rawQuery(String sql, String[] selectionArgs,
                    CancellationSignal cancellationSignal);


    void validateSql(@NonNull String sql, @Nullable CancellationSignal cancellationSignal);

    boolean isReadOnly();

    boolean needUpgrade(int newVersion);

    void setLocale(Locale locale);

    void setMaxSqlCacheSize(int cacheSize);

    void setForeignKeyConstraintsEnabled(boolean enable);

    boolean enableWriteAheadLogging();

    void disableWriteAheadLogging();

    boolean isWriteAheadLoggingEnabled();

    List<Pair<String, String>> getAttachedDbs();

    boolean isDatabaseIntegrityOk();

    interface DbCloseListener {
        void onDbClose();
    }

    interface DbCreateListener {
        void onDbCreate();
    }

}
