package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager extends SQLiteOpenHelper {
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    // SQLite database version number
    private static final int DB_VERSION = 1;
    private static final String EVENTS_TABLE_NAME = "events";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_ID = "id";
    private static final String UPDATED = "updated";

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

    /*
     * save individual messages to DB
     * */
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

    /*
     * delete event with single messageId
     * */
    void clearEventFromDB(int messageId) {
        RudderLogger.logInfo(String.format(Locale.US, "DBPersistentManager: clearEventFromDB: Deleting event with messageID: %d", messageId));
        List<Integer> messageIds = new ArrayList<>();
        messageIds.add(messageId);
        clearEventsFromDB(messageIds);
    }

    /**
     * flush the events from the database
     */
    void flushEvents() {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            String deleteSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: flushEvents: deleteSQL: %s", deleteSQL));
            database.execSQL(deleteSQL);
            RudderLogger.logInfo("DBPersistentManager: flushEvents: Messages deleted from DB");
        } else {
            RudderLogger.logError("DBPersistentManager: flushEvents: database is not writable");
        }
    }

    /*
     * remove selected events from persistence database storage
     * */
    void clearEventsFromDB(List<Integer> messageIds) {
        // get writable database
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            RudderLogger.logInfo(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: Clearing %d messages from DB", messageIds.size()));
            // format CSV string from messageIds list
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < messageIds.size(); index++) {
                builder.append(messageIds.get(index));
                builder.append(",");
            }
            // remove last "," character
            builder.deleteCharAt(builder.length() - 1);
            // remove events
            String deleteSQL = String.format(Locale.US, "DELETE FROM %s WHERE %s IN (%s)",
                    EVENTS_TABLE_NAME, MESSAGE_ID, builder.toString());
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
            database.execSQL(deleteSQL);
            RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB");
        } else {
            RudderLogger.logError("DBPersistentManager: clearEventsFromDB: database is not writable");
        }
    }

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    void fetchEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        // clear lists if not empty
        if (!messageIds.isEmpty()) messageIds.clear();
        if (!messages.isEmpty()) messages.clear();

        // get readable database instance
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, UPDATED, count);
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
            Cursor cursor = database.rawQuery(selectSQL, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                while (!cursor.isAfterLast()) {
                    messageIds.add(cursor.getInt(cursor.getColumnIndex(MESSAGE_ID)));
                    messages.add(cursor.getString(cursor.getColumnIndex(MESSAGE)));
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: DB is empty");
            }
            cursor.close();
        } else {
            RudderLogger.logError("DBPersistentManager: fetchEventsFromDB: database is not readable");
        }
    }

    int getDBRecordCount() {
        // initiate count
        int count = -1;

        // get readable database instance
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s;", EVENTS_TABLE_NAME);
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", countSQL));
            Cursor cursor = database.rawQuery(countSQL, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: getDBRecordCount: fetched count from DB");
                while (!cursor.isAfterLast()) {
                    // result will be in the first position
                    count = cursor.getInt(0);
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logInfo("DBPersistentManager: getDBRecordCount: DB is empty");
            }
            // release cursor
            cursor.close();
        } else {
            RudderLogger.logError("DBPersistentManager: getDBRecordCount: database is not readable");
        }

        return count;
    }

    private static DBPersistentManager instance;

    static DBPersistentManager getInstance(Application application) {
        if (instance == null) {
            RudderLogger.logInfo("DBPersistentManager: getInstance: creating instance");
            instance = new DBPersistentManager(application);
        }
        return instance;
    }

    private DBPersistentManager(Application application) {
        super(application, DB_NAME, null, DB_VERSION);
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // basically do nothing
    }

    public void deleteAllEvents() {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            // remove events
            String clearDBSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: deleteAllEvents: clearDBSQL: %s", clearDBSQL));
            database.execSQL(clearDBSQL);
            RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
        } else {
            RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
        }
    }
}
