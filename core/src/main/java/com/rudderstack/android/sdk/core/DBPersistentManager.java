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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager extends SQLiteOpenHelper {
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    // SQLite database version number
    private static final int DB_VERSION = 1;

    // events table and its details
    static final String EVENTS_TABLE_NAME = "events";
    private static final String MESSAGE_ID_COL = "id";
    static final String MESSAGE_COL = "message";
    static final String UPDATED_COL = "updated";
    private static final String STATUS_COL = "status";

    // command to create events table based for the database version 1.
    private static final String DATABASE_EVENTS_TABLE_SCHEMA_V1 = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                    "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)",
            EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL);

    private static final String OLD_EVENTS_TABLE = EVENTS_TABLE_NAME + "_old";
    // columns in the events table after the downgrade operation
    private static final String DOWNGRADED_EVENTS_TABLE_COLUMNS = MESSAGE_COL + ", " + UPDATED_COL;
    // command to rename the events table to events_old. This command is used as part of the db downgrade operation
    private static final String DATABASE_RENAME_EVENTS_TABLE = "ALTER TABLE " + EVENTS_TABLE_NAME + " RENAME TO " + OLD_EVENTS_TABLE;
    // command to copy the data from the old events table to the new table
    private static final String DATABASE_COPY_EVENTS_FROM_OLD_TO_NEW = "INSERT INTO " + EVENTS_TABLE_NAME + "(" + DOWNGRADED_EVENTS_TABLE_COLUMNS + ") SELECT " + DOWNGRADED_EVENTS_TABLE_COLUMNS + " FROM " + OLD_EVENTS_TABLE;
    // command to drop the old events table
    private static final String DATABASE_DROP_OLD_EVENTS_TABLE = "DROP TABLE " + OLD_EVENTS_TABLE;

    //synchronizing database access
    private static final Object DB_LOCK = new Object();

    DBInsertionHandlerThread dbInsertionHandlerThread;
    final Queue<Message> queue = new LinkedList<Message>();

    /*
     * create table initially if not exists
     * */
    private void createSchema(SQLiteDatabase db) {
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createSchemaSQL: %s", DATABASE_EVENTS_TABLE_SCHEMA_V1));
        db.execSQL(DATABASE_EVENTS_TABLE_SCHEMA_V1);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    /*
     * save individual messages to DB
     * */
    void saveEvent(String messageJson) {
        try {
            Message msg = new Message().obtain();
            msg.obj = messageJson;
            if (dbInsertionHandlerThread == null) {
                queue.add(msg);
                return;
            }
            synchronized (this) {
                dbInsertionHandlerThread.addMessage(msg);
            }
        } catch (Exception e) {
            RudderLogger.logError(e.getCause());
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
                        EVENTS_TABLE_NAME, MESSAGE_ID_COL, builder.toString());
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
     * returns messageIds and messages returned on executing the supplied SQL statement
     * */
    void getEventsFromDB(List<Integer> messageIds, List<String> messages, String selectSQL) {
        // clear lists if not empty
        if (!messageIds.isEmpty()) messageIds.clear();
        if (!messages.isEmpty()) messages.clear();

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {
                Cursor cursor = database.rawQuery(selectSQL, null);
                if (cursor.moveToFirst()) {
                    RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                    while (!cursor.isAfterLast()) {
                        final int messageIdColIndex = cursor.getColumnIndex(MESSAGE_ID_COL);
                        final int messageColIndex = cursor.getColumnIndex(MESSAGE_COL);
                        if (messageIdColIndex > -1)
                            messageIds.add(cursor.getInt(messageIdColIndex));
                        if (messageColIndex > -1)
                            messages.add(cursor.getString(messageColIndex));
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

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    void fetchEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, UPDATED_COL, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    /*
     * retrieve all messages from DB and store messageIds and messages separately
     * */
    void fetchAllEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC", EVENTS_TABLE_NAME, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
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

        // Need to perform db operations on a separate thread to support strict mode.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DBPersistentManager.this.getWritableDatabase();
                    synchronized (DBPersistentManager.this) {
                        dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread", DBPersistentManager.this.getWritableDatabase());
                        dbInsertionHandlerThread.start();
                        for (Message msg : queue) {
                            dbInsertionHandlerThread.addMessage(msg);
                        }
                    }
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (checkIfStatusColumnExists(db)) {
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: onDowngrade: DB got downgraded from version: %d to the version: %d, hence running migration to remove the status column", oldVersion, newVersion));
            deleteStatusColumn(db);
        }
    }

    private void deleteStatusColumn(SQLiteDatabase database) {
        if (database.isOpen()) {
            try {
                synchronized (DB_LOCK) {
                    database.beginTransaction();
                    // 1. renaming the existing events table to events_old
                    database.execSQL(DATABASE_RENAME_EVENTS_TABLE);
                    // 2. creating new events table which doesn't have the status column
                    database.execSQL(DATABASE_EVENTS_TABLE_SCHEMA_V1);
                    // 3. copying the data from events_old to events
                    database.execSQL(DATABASE_COPY_EVENTS_FROM_OLD_TO_NEW);
                    // 4. dropping the events_old table
                    database.execSQL(DATABASE_DROP_OLD_EVENTS_TABLE);
                    database.setTransactionSuccessful();
                }
                RudderLogger.logDebug("DBPersistentManager: deleteStatusColumn: status column is deleted successfully");
            } catch (SQLiteDatabaseCorruptException ex) {
                RudderLogger.logError("DBPersistentManager: deleteStatusColumn: Exception while deleting the status column due to " + ex.getLocalizedMessage());
            } finally {
                database.endTransaction();
            }
        } else {
            RudderLogger.logError("DBPersistentManager: deleteStatusColumn: database is not readable, hence status column cannot be deleted");
        }
    }

    private boolean checkIfStatusColumnExists(SQLiteDatabase database) {
        String checkIfStatusExistsSqlString = "PRAGMA table_info(events)";
        try {
            // get readable database instance
            if (database.isOpen()) {
                Cursor allRows = database.rawQuery(checkIfStatusExistsSqlString, null);
                if (allRows.moveToFirst()) {
                    do {
                        int index = allRows.getColumnIndex("name");
                        if (index > -1) {
                            String columnName = allRows.getString(index);
                            if (columnName.equals(STATUS_COL))
                                return true;
                        }
                    } while (allRows.moveToNext());
                }
                allRows.close();
            } else {
                RudderLogger.logError("DBPersistentManager: checkIfStatusColumnExists: database is not readable, hence we cannot check the existence of status column");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError("DBPersistentManager: checkIfStatusColumnExists: Exception while checking the presence of status column due to " + ex.getLocalizedMessage());
        }
        return false;
    }

    @Override
    public synchronized void close() {
        super.close();
        instance = null;
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

    public void addMessage(Message message) {
        if (dbInsertionHandler == null) {
            dbInsertionHandler = new DBInsertionHandler(getLooper(), this.database);
        }
        dbInsertionHandler.sendMessage(message);
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
                insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replaceAll("'", "\\\\\'"));
                insertValues.put(DBPersistentManager.UPDATED_COL, updatedTime);
                database.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            } else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }
        }
    }
}
