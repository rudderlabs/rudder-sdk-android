package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

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
    static final String EVENTS_TABLE_NAME = "events";
    static final String MESSAGE = "message";
    private static final String MESSAGE_ID = "id";
    static final String UPDATED = "updated";
    DBInsertionHandlerThread dbInsertionHandlerThread;

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
        Message msg = new Message().obtain();
        msg.obj = messageJson;
        dbInsertionHandlerThread.addMessage(msg);
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
        try {
            SQLiteDatabase database = getWritableDatabase();
            if (database.isOpen()) {
                String deleteSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: flushEvents: deleteSQL: %s", deleteSQL));
                database.execSQL(deleteSQL);
                RudderLogger.logInfo("DBPersistentManager: flushEvents: Messages deleted from DB");
            } else {
                RudderLogger.logError("DBPersistentManager: flushEvents: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    /*
     * remove selected events from persistence database storage
     * */
    void clearEventsFromDB(List<Integer> messageIds) {
        try {
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
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
                database.execSQL(deleteSQL);
                RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB");
            } else {
                RudderLogger.logError("DBPersistentManager: clearEventsFromDB: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    void fetchEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        // clear lists if not empty
        if (!messageIds.isEmpty()) messageIds.clear();
        if (!messages.isEmpty()) messages.clear();

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {
                String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, UPDATED, count);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
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
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    int getDBRecordCount() {
        // initiate count
        int count = -1;

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {
                String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s;", EVENTS_TABLE_NAME);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", countSQL));
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
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DBPersistentManager.this.getWritableDatabase();
                    dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread", DBPersistentManager.this.getWritableDatabase());
                    dbInsertionHandlerThread.start();
                } catch (SQLiteDatabaseCorruptException ex) {
                    RudderLogger.logError(ex);
                }
            }
        }).start();
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
        try {
            SQLiteDatabase database = getWritableDatabase();
            if (database.isOpen()) {
                // remove events
                String clearDBSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: deleteAllEvents: clearDBSQL: %s", clearDBSQL));
                database.execSQL(clearDBSQL);
                RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
            } else {
                RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }
}

class DBInsertionHandlerThread extends HandlerThread {

    DBInsertionHandler dbInsertionHandler;
    SQLiteDatabase database;

    public DBInsertionHandlerThread(String name, SQLiteDatabase database) {
        super(name);
        this.database = database;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        dbInsertionHandler = new DBInsertionHandler(getLooper(), this.database);
    }

    public void addMessage(Message message) {
        if (dbInsertionHandler != null) {
            dbInsertionHandler.sendMessage(message);
        }
    }

    private class DBInsertionHandler extends Handler {
        SQLiteDatabase database;

        public DBInsertionHandler(Looper looper, SQLiteDatabase database) {
            super(looper);
            this.database = database;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.database.isOpen()) {
                String messageJson = (String) msg.obj;
                long updatedTime = System.currentTimeMillis();
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: Inserting Message %s into table %s as Updated at %d", messageJson.replaceAll("'", "\\\\\'"), DBPersistentManager.EVENTS_TABLE_NAME, updatedTime));
                ContentValues insertValues = new ContentValues();
                insertValues.put(DBPersistentManager.MESSAGE, messageJson.replaceAll("'", "\\\\\'"));
                insertValues.put(DBPersistentManager.UPDATED, updatedTime);
                database.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            } else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }
        }
    }
}
