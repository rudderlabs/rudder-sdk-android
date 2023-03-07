package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.DBPersistentManager.BACKSLASH;
import static com.rudderstack.android.sdk.core.DBPersistentManager.EVENT;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.rudderstack.android.sdk.core.util.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager extends SQLiteOpenHelper {
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    // SQLite database version number
    private static final int DB_VERSION = 2;
    static final String EVENT = "EVENT";

    static final String EVENTS_TABLE_NAME = "events";
    private static final String MESSAGE_ID_COL = "id";
    static final String MESSAGE_COL = "message";
    static final String UPDATED_COL = "updated";
    private static final String STATUS_COL = "status";

    //status values for database version 2 =. Check createSchema documentation for details.
    private static final int STATUS_CLOUD_MODE_DONE = 0b10;
    private static final int STATUS_DEVICE_MODE_DONE = 0b01;
    private static final int STATUS_ALL_DONE = 0b11;
    private static final int STATUS_NEW = 0b00;

    //command to add status column. For details see onUpgrade or the documentation for createSchema
    //for version 1 to 2
    private static final String DATABASE_ALTER_ADD_STATUS = "ALTER TABLE "
            + EVENTS_TABLE_NAME + " ADD COLUMN " + STATUS_COL + " INTEGER NOT NULL DEFAULT " + STATUS_NEW;

    private static final String SET_STATUS_FOR_EXISTING = "UPDATE " + EVENTS_TABLE_NAME + " SET " + STATUS_COL + " = " + STATUS_DEVICE_MODE_DONE;

    static final String BACKSLASH = "\\\\'";

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

    DBInsertionHandlerThread dbInsertionHandlerThread;
    final Queue<Message> queue = new LinkedList<>();

    //synchronizing database access
    private static final Object DB_LOCK = new Object();

    public static final Object QUEUE_LOCK = new Object();

    /*
     * create table initially if not exists
     * version -2 : adding status for device mode
     * status will either be 2, 1 or 0.
     * The events in Db will be used by both cloud and device mode.
     * status -0x00 -> event is not used by any of cloud or device mode.
     * 0x10 → cloud done/ device mode is pending (1st bit)
     * 0x01 → device mode done/ cloud mode is pending (0th bit)
     * 0x11 → both done, needs to be deleted.
     * For checking if something is done we & and check for value < 1, meaning it's not done
     * For updating the status perform | with the associated binary

     * */
    private void createSchema(SQLiteDatabase db) {
        String createEventSchemaSQL;
        if (DB_VERSION == 1) {
            createEventSchemaSQL = DATABASE_EVENTS_TABLE_SCHEMA_V1;
        } else {
            createEventSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                            "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL DEFAULT %d)",
                    EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL, STATUS_COL, STATUS_NEW);
        }
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createEventSchemaSQL: %s", createEventSchemaSQL));
        db.execSQL(createEventSchemaSQL);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    /*
     * Receives message from Repository, and passes it to the Handler thread if it exists, else adds it to a queue for replay
     * once Handler thread is initialized.
     * */
    void saveEvent(String messageJson, EventInsertionCallback callback) {
        Message msg = createOsMessageFromJson(messageJson, callback);
        synchronized (DBPersistentManager.QUEUE_LOCK) {
            if (dbInsertionHandlerThread == null) {
                queue.add(msg);
                return;
            }
            addMessageToHandlerThread(msg);
        }

    }

    private Message createOsMessageFromJson(String messageJson, EventInsertionCallback callback){
        Message msg = Message.obtain();
        msg.obj = callback;
        Bundle eventBundle = new Bundle();
        eventBundle.putString(EVENT, messageJson);
        msg.setData(eventBundle);
        return msg;
    }

    /*
       Passes the input message to the Handler thread.
     */
    void addMessageToHandlerThread(Message msg) {
        dbInsertionHandlerThread.addMessage(msg);
    }
    @VisibleForTesting
    void saveEventSync(String messageJson) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replace("'", BACKSLASH));
        insertValues.put(DBPersistentManager.UPDATED_COL, System.currentTimeMillis());
        getWritableDatabase().insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues);
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
                synchronized (DB_LOCK) {
                    database.execSQL(deleteSQL);
                }
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
        Map<Integer, Integer> messageIdStatusMap = new HashMap<>();
        getEventsFromDB(messageIdStatusMap, messages, selectSQL);
        messageIds.addAll(messageIdStatusMap.keySet());
        Collections.sort(messageIds);
    }

    void getEventsFromDB(Map<Integer, Integer> messageIdStatusMap,//(id (row_id), status)
                         List<String> messages,
                         String selectSQL) {
        // clear lists if not empty
        if (!messageIdStatusMap.isEmpty()) messageIdStatusMap.clear();
        if (!messages.isEmpty()) messages.clear();

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {
                Cursor cursor;
                synchronized (DB_LOCK) {
                    cursor = database.rawQuery(selectSQL, null);
                }
                if (cursor.moveToFirst()) {
                    RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                    while (!cursor.isAfterLast()) {
                        final int messageIdColIndex = cursor.getColumnIndex(MESSAGE_ID_COL);
                        final int messageColIndex = cursor.getColumnIndex(MESSAGE_COL);
                        final int statusColIndex = cursor.getColumnIndex(STATUS_COL);

                        if (messageIdColIndex > -1)
                            messageIdStatusMap.put(cursor.getInt(messageIdColIndex),
                                    statusColIndex > -1 ? cursor.getInt(statusColIndex) : STATUS_DEVICE_MODE_DONE);
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
    //unit test this
    void fetchCloudModeEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED_COL, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchCloudModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    //unit test this
    void fetchDeviceModeEventsFromDb(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED_COL, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchDeviceModeEventsFromDb: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }


    void fetchAllCloudModeEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllCloudModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    @VisibleForTesting
    void fetchAllDeviceModeEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllDeviceModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    @VisibleForTesting
    void fetchAllEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC", EVENTS_TABLE_NAME, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    int getDBRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s ;", EVENTS_TABLE_NAME);
        return getCountForCommand(countSQL);
    }

    int getDeviceModeRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s IN (%d, %d);", EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, DBPersistentManager.STATUS_NEW);
        return getCountForCommand(countSQL);
    }

    private int getCountForCommand(String sql) {
        // initiate count
        int count = -1;

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {

                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", sql));
                Cursor cursor;
                synchronized (DB_LOCK) {
                    cursor = database.rawQuery(sql, null);
                }
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
    }

    /*
       Starts the Handler thread, which is responsible for storing the messages in its internal queue, and
       save them to the sqlite db sequentially.
     */
    void startHandlerThread() {
        // Need to perform db operations on a separate thread to support strict mode.
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (DBPersistentManager.QUEUE_LOCK) {
                        SQLiteDatabase database = DBPersistentManager.this.getWritableDatabase();
                        dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread", database);
                        dbInsertionHandlerThread.start();
                        for (Message msg : queue) {
                            addMessageToHandlerThread(msg);
                        }
                    }
                } catch (SQLiteDatabaseCorruptException ex) {
                    RudderLogger.logError(ex);
                } catch (ConcurrentModificationException ex) {
                    RudderLogger.logError(ex);
                } catch (NullPointerException ex) {
                    RudderLogger.logError(ex);
                }
            }
        };
        Future future = executor.submit(runnable);
        try {
            // todo: shall we add some timeout here ?
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            RudderLogger.logError("DBPersistentManager: constructor: Exception while initializing the DBInsertionHandlerThread due to " + e.getLocalizedMessage());
        }
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
    void checkForMigrations() {
        SQLiteDatabase database = getWritableDatabase();
        if (!checkIfStatusColumnExists(database)) {
            RudderLogger.logDebug("DBPersistentManager: checkForMigrations: Status column doesn't exist in the events table, hence performing the migration now");
            performMigration(database);
            return;
        }
        RudderLogger.logDebug("DBPersistentManager: checkForMigrations: Status column exists in the table already, hence no migration required");
    }

    private void performMigration(SQLiteDatabase database) {
        try {
            if (database.isOpen()) {
                RudderLogger.logDebug("DBPersistentManager: performMigration: Adding the status column to the events table");
                database.execSQL(DATABASE_ALTER_ADD_STATUS);
                RudderLogger.logDebug("DBPersistentManager: performMigration: Setting the status to DEVICE_MODE_PROCESSING_DONE for the events existing already in the DB");
                database.execSQL(SET_STATUS_FOR_EXISTING);
            } else {
                RudderLogger.logError("DBPersistentManager: performMigration: database is not readable, hence migration cannot be performed");
            }
        } catch (Exception e) {
            RudderLogger.logError("DBPersistentManager: performMigration: Exception while performing the migration due to " + e.getLocalizedMessage());
        }
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
                synchronized (DB_LOCK) {
                    database.execSQL(clearDBSQL);
                }
                RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
            } else {
                RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }


    void markDeviceModeDone(List<Integer> rowIds) {
        String rowIdsCSVString = Utils.getCSVString(rowIds);
        if (rowIdsCSVString == null) return;
        updateEventStatus(rowIdsCSVString, DBPersistentManager.STATUS_DEVICE_MODE_DONE);

    }

    void markCloudModeDone(List<Integer> rowIds) {
        String rowIdsCSVString = Utils.getCSVString(rowIds);
        if (rowIdsCSVString == null) return;
        updateEventStatus(rowIdsCSVString, DBPersistentManager.STATUS_CLOUD_MODE_DONE);
    }

    //unit test
    private void updateEventStatus(String rowIdsCSVString, int status) {
        String sql = "UPDATE " + DBPersistentManager.EVENTS_TABLE_NAME + " SET " +
                DBPersistentManager.STATUS_COL + " = (" + DBPersistentManager.STATUS_COL + " | " + status +
                ") WHERE " + MESSAGE_ID_COL + " IN "
                + rowIdsCSVString + ";";
        synchronized (DB_LOCK) {
            getWritableDatabase().execSQL(sql);
        }
    }

    // Delete all events from the DB which have status as STATUS_ALL_DONE
    void runGcForEvents() {
        deleteDoneEvents();
    }

    private void deleteDoneEvents() {
        synchronized (DB_LOCK) {
            getWritableDatabase().delete(EVENTS_TABLE_NAME,
                    DBPersistentManager.STATUS_COL + " = " + DBPersistentManager.STATUS_ALL_DONE,
                    null);
        }
    }

    void deleteFirstEvents(int count) {
        synchronized (DB_LOCK) {
            getWritableDatabase().delete(EVENTS_TABLE_NAME, MESSAGE_ID_COL + " IN ( " +
                    "SELECT " + MESSAGE_ID_COL + " FROM " + EVENTS_TABLE_NAME +
                    " ORDER BY " + UPDATED_COL + " LIMIT " + count + ");", null);
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
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (this.database.isOpen()) {
                EventInsertionCallback callback = (EventInsertionCallback) msg.obj;
                Bundle msgBundle = msg.getData();
                String messageJson = msgBundle.getString(EVENT);
                long updatedTime = System.currentTimeMillis();
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: Inserting Message %s into table %s as Updated at %d", messageJson.replace("'", BACKSLASH), DBPersistentManager.EVENTS_TABLE_NAME, updatedTime));
                ContentValues insertValues = new ContentValues();
                insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replace("'", BACKSLASH));
                insertValues.put(DBPersistentManager.UPDATED_COL, updatedTime);
                long rowId = database.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues); //rowId will used
                callback.onInsertion((int) rowId);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            } else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }
        }
    }

}
