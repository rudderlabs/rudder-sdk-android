package com.rudderstack.android.sdk.core;

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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager extends SQLiteOpenHelper {
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    // SQLite database version number
    private static final int DB_VERSION = 2;
    private int _currentDbVersion = DB_VERSION;

    static final String EVENTS_DESTINATION_ID_TABLE_NAME = "events_destination";
    static final String EVENTS_DESTINATION_ROW_ID_COL_NAME = "row_id";
    static final String EVENTS_DESTINATION_DESTINATION_ID_COL_NAME = "destination_id";
    private static final String EVENT_DESTINATION_SCHEMA_CREATE_STMT = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                    "('%s' INTEGER NOT NULL, " +
                    "'%s' INTEGER NOT NULL)",
            EVENTS_DESTINATION_ID_TABLE_NAME, EVENTS_DESTINATION_ROW_ID_COL_NAME, EVENTS_DESTINATION_DESTINATION_ID_COL_NAME);

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
            + EVENTS_TABLE_NAME + " ADD COLUMN " + STATUS_COL + " INTEGER NOT NULL DEFAULT " + STATUS_DEVICE_MODE_DONE;


    DBInsertionHandlerThread dbInsertionHandlerThread;
    final Queue<Message> queue = new LinkedList<Message>();

    //synchronizing database access
    private static final Object DB_LOCK = new Object();

    static final String EVENT = "EVENT";

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
    private void createSchemas(SQLiteDatabase db) {
        String createEventSchemaSQL;
        switch (_currentDbVersion) {
            case 1:
                createEventSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                                "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)",
                        EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL);
                break;
            default:
                createEventSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                                "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL DEFAULT %d)",
                        EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL, STATUS_COL, STATUS_NEW);
        }
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createEventSchemaSQL: %s", createEventSchemaSQL));
        db.execSQL(createEventSchemaSQL);
        //creating event- destination table
        db.execSQL(EVENT_DESTINATION_SCHEMA_CREATE_STMT);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    /*
     * save individual messages to DB
     * */
    void saveEvent(String messageJson, EventRepository.EventInsertionCallback callback) {
        try {
            Message msg = new Message().obtain();
            msg.obj = callback;
            Bundle eventBundle = new Bundle();
            eventBundle.putString(EVENT, messageJson);
            msg.setData(eventBundle);
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

    @VisibleForTesting
    void saveEventSync(String messageJson) {
        ContentValues insertValues = new ContentValues();
        insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replaceAll("'", "\\\\\'"));
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

                String messageIdsCSVString = Utils.getCSVString(messageIds);
                if (messageIdsCSVString == null) return;
                // remove events
                String deleteSQL = String.format(Locale.US, "DELETE FROM %s WHERE %s IN %s",
                        EVENTS_TABLE_NAME, MESSAGE_ID_COL, messageIdsCSVString);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
                synchronized (DB_LOCK) {
                    database.execSQL(deleteSQL);
                }
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
    }

    void getEventsFromDB(Map<Integer, Integer> messageIdStatusMap,//(id (row_id), status)
                         List<String> messages,
                         String selectSQL) {
        // clear lists if not empty
        if (!messageIdStatusMap.isEmpty()) messageIdStatusMap.clear();
        if (!messages.isEmpty()) messages.clear();

        try {
            // get readable database instance
            SQLiteDatabase database = getWritableDatabase();
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
                            messages.add(cursor.getString(messageColIndex)
                            );
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


    /*
     * retrieve all messages from DB and store messageIds and messages separately
     * */
    //unit test
    void fetchAllCloudModeEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllCloudModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    //unit test
    void fetchAllDeviceModeEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED_COL);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllDeviceModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }


    int getCloudModeDBRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s IN (%d, %d);", EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL,
                DBPersistentManager.STATUS_DEVICE_MODE_DONE, DBPersistentManager.STATUS_NEW);
        return getCountForCommand(countSQL);
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    static DBPersistentManager getInstance(Application application, int dbVersion) {
        instance = new DBPersistentManager(application, dbVersion);
        return instance;
    }

    static DBPersistentManager getInstance(Application application) {
        if (instance == null) {
            RudderLogger.logInfo("DBPersistentManager: getInstance: creating instance");
            return getInstance(application, DB_VERSION);
        }
        return instance;
    }

    private DBPersistentManager(Application application, int version) {
        super(application, DB_NAME, null, version);
        _currentDbVersion = version;
        // Need to perform db operations on a separate thread to support strict mode.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DBPersistentManager.this.getWritableDatabase();
                    synchronized (DBPersistentManager.this) {
                        dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread",
                                DBPersistentManager.this.getWritableDatabase());
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
        createSchemas(db);
    }

    /**
     * Details of method is provided in {@link SQLiteOpenHelper}
     * We maintain here the updated versions.
     * previous versions = none
     * current version = 1
     * updated version = 2 added status field values range from 0 to 0x11
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //replace with switch when more upgrades creep in
        if (oldVersion == 1 && newVersion >= 2) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_ADD_STATUS);
        }
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

    @Nullable
    Map<Integer, List<String>> fetchDestinationIdsGroupByEventRowId(List<Integer> rowIds) {
        String query = createDestinationIdsGroupByEventsQuery(rowIds);
        if (query == null) return null;

        Map<Integer, List<String>> eventDestinationIdMap = new HashMap<>();
        Cursor cursor;
        synchronized (DB_LOCK) {
            cursor = getWritableDatabase().rawQuery(query, null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int rowIdCol = cursor.getColumnIndex(DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME);
            if (rowIdCol > -1) {
                int rowId = cursor.getInt(rowIdCol);
                List<String> destinationIds = eventDestinationIdMap.get(rowId);
                if (destinationIds == null) {
                    destinationIds = new ArrayList<>();
                    eventDestinationIdMap.put(rowId, destinationIds);
                }
                int destinationIdCol = cursor.getColumnIndex(DBPersistentManager.EVENTS_DESTINATION_DESTINATION_ID_COL_NAME);
                if (destinationIdCol > -1) {
                    String destinationId = cursor.getString(destinationIdCol);
                    destinationIds.add(destinationId);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return eventDestinationIdMap;
    }

    @Nullable
    private String createDestinationIdsGroupByEventsQuery(List<Integer> rowIds) {
        String rowIdsCSVString = Utils.getCSVString(rowIds);
        if (rowIdsCSVString == null)
            return null;

        return "SELECT * FROM " + DBPersistentManager.EVENTS_DESTINATION_ID_TABLE_NAME +
                " WHERE " + EVENTS_DESTINATION_ROW_ID_COL_NAME + " IN " + rowIdsCSVString +
                " ORDER BY " + EVENTS_DESTINATION_DESTINATION_ID_COL_NAME + ";";
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
                + rowIdsCSVString +";";
        synchronized (DB_LOCK) {
            getWritableDatabase().execSQL(sql);
        }
    }

    void deleteFromRowIdDestinationIdTable(String destinationId, List<TransformationResponse.TransformedEvent> transformedEvents) {
        String rowIdsCSVString = Utils.getCSVString(getOrderNos(transformedEvents));
        if (rowIdsCSVString == null) return;
        getWritableDatabase().delete(DBPersistentManager.EVENTS_DESTINATION_ID_TABLE_NAME,
                DBPersistentManager.EVENTS_DESTINATION_DESTINATION_ID_COL_NAME + " = \""
                        + destinationId + "\" AND " + DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME + " IN " + rowIdsCSVString, null);
    }

    void saveEventToDestinationIdMapping(int rowId, String destinationId) {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            ContentValues eventDestinationValues = new ContentValues();
            eventDestinationValues.put(DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME,
                    rowId);
            eventDestinationValues.put(DBPersistentManager.EVENTS_DESTINATION_DESTINATION_ID_COL_NAME,
                    destinationId);
            database.insert(DBPersistentManager.EVENTS_DESTINATION_ID_TABLE_NAME, null, eventDestinationValues);
        }
    }

    List<Integer> getOrderNos(List<TransformationResponse.TransformedEvent> transformedEvents) {
        List<Integer> orderNos = new ArrayList<>();
        if (transformedEvents == null || transformedEvents.size() == 0)
            return orderNos;
        for (TransformationResponse.TransformedEvent transformedEvent : transformedEvents) {
            orderNos.add(transformedEvent.orderNo);
        }
        return orderNos;
    }

    Map<Integer, Integer> getDestinationCountMapForEvents(List<Integer> messageIds) {
        Map<Integer, Integer> rowDestinationCountMap = new HashMap<>();


        if (messageIds.size() == 0) return rowDestinationCountMap;

        String orderNosCSVString = Utils.getCSVString(messageIds);
        String sql = "SELECT " + DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME +
                ", COUNT(*) as count  FROM " + DBPersistentManager.EVENTS_DESTINATION_ID_TABLE_NAME +
                " WHERE " + DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME + "  IN " + orderNosCSVString + " GROUP BY " + DBPersistentManager.EVENTS_DESTINATION_DESTINATION_ID_COL_NAME + ";";

        Cursor destinationCountCursor = getWritableDatabase().rawQuery(sql, null);
        destinationCountCursor.moveToFirst();

        while (!destinationCountCursor.isAfterLast()) {
            int rowIDCol = destinationCountCursor.getColumnIndex(DBPersistentManager.EVENTS_DESTINATION_ROW_ID_COL_NAME);
            int countCol = destinationCountCursor.getColumnIndex("count");
            if (rowIDCol == -1 || countCol == -1) continue;
            int rowId = destinationCountCursor.getInt(rowIDCol);
            int count = destinationCountCursor.getInt(countCol);
            rowDestinationCountMap.put(rowId, count);
            destinationCountCursor.moveToNext();
        }
        destinationCountCursor.close();
        return rowDestinationCountMap;
    }

    void deleteDoneEvents() {
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
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.database.isOpen()) {
                EventRepository.EventInsertionCallback callback = (EventRepository.EventInsertionCallback) msg.obj;
                Bundle msgBundle = msg.getData();
                String messageJson = msgBundle.getString(DBPersistentManager.EVENT);
                long updatedTime = System.currentTimeMillis();
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: Inserting Message %s into table %s as Updated at %d", messageJson.replaceAll("'", "\\\\\'"), DBPersistentManager.EVENTS_TABLE_NAME, updatedTime));
                ContentValues insertValues = new ContentValues();
                insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replaceAll("'", "\\\\\'"));
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
