package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Locale;

final class EventsDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "rl_persistence.db";
    private static final int DB_VERSION = 1;
    static final String EVENTS_TABLE_NAME = "events";
    static final String MESSAGE = "message";
    static final String MESSAGE_ID = "id";
    static final String UPDATED = "updated";

     EventsDbHelper(Context context){
        this(context, DB_NAME, null, DB_VERSION);
    }
    private EventsDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        this(context, name, factory, version, null);
    }

    private EventsDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private EventsDbHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // basically do nothing
    }
    /*
     * create table initially if not exists
     * */
    private void createSchema(SQLiteDatabase db) {
        String createSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)",
                EVENTS_TABLE_NAME, MESSAGE_ID, MESSAGE, UPDATED);
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createSchemaSQL: %s", createSchemaSQL));
        db.execSQL(createSchemaSQL);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }
}
