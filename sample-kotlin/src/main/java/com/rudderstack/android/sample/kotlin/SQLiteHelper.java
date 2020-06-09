package com.rudderstack.android.sample.kotlin;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.rudderstack.android.sdk.core.RudderLogger;

import java.util.Locale;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "rl_persistence_dummy.db";
    // SQLite database version number
    private static final int DB_VERSION = 1;
    private static final String EVENTS_TABLE_NAME = "events";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_ID = "id";
    private static final String UPDATED = "updated";

    public SQLiteHelper(@Nullable Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createSchema(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private void createSchema(SQLiteDatabase db) {
        String createSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)",
                EVENTS_TABLE_NAME, MESSAGE_ID, MESSAGE, UPDATED);
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createSchemaSQL: %s", createSchemaSQL));
        db.execSQL(createSchemaSQL);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    void saveEvent(String messageJson) {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            String saveEventSQL = String.format(Locale.US, "INSERT INTO %s (%s, %s) VALUES ('%s', %d)",
                    EVENTS_TABLE_NAME, MESSAGE, UPDATED, messageJson.replaceAll("'", "\\\\\'"), System.currentTimeMillis());
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: saveEvent: saveEventSQL: %s", saveEventSQL));
            database.execSQL(saveEventSQL);
            RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
        } else {
            RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
        }
    }
}
