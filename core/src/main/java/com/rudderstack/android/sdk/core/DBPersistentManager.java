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
    static final String EVENTS_TABLE_NAME = "events";

    static final String EVENTS_ROW_ID_TRANSFORMATION_ID_TABLE_NAME = "events_transformer";
    static final String EVENTS_TRANSFORMER_ROW_ID_COL_NAME = "row_id";
    static final String EVENTS_TRANSFORMER_TRANSFORMATION_ID_COL_NAME = "transformation_id";
    private static final String EVENT_TRANSFORMATION_SCHEMA_CREATE_STMT = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                    "('%s' INTEGER NOT NULL, " +
                    "'%s' INTEGER NOT NULL)",
            EVENTS_ROW_ID_TRANSFORMATION_ID_TABLE_NAME, EVENTS_TRANSFORMER_ROW_ID_COL_NAME, EVENTS_TRANSFORMER_TRANSFORMATION_ID_COL_NAME);

    static final String TRANSFORMATION_IDS_BUNDLE_KEY = "transformation_ids";

    static final String MESSAGE = "message";
    private static final String MESSAGE_ID = "id";
    private static final String STATUS_COL = "status";
    static final String UPDATED = "updated";
    DBInsertionHandlerThread dbInsertionHandlerThread;
    final Queue<Message> queue = new LinkedList<Message>();
    private int _currentDbVersion = DB_VERSION;

    //status values for database version 2 =. Check createSchema documentation for details.
    private static final int STATUS_CLOUD_MODE_DONE = 0b10;
    private static final int STATUS_DEVICE_MODE_DONE = 0b01;
    private static final int STATUS_ALL_DONE = 0b11;
    private static final int STATUS_NEW = 0b00;


    //command to add status column. For details see onUpgrade or the documentation for createSchema
    //for version 1 to 2
    private static final String DATABASE_ALTER_ADD_STATUS = "ALTER TABLE "
            + EVENTS_TABLE_NAME + " ADD COLUMN " + STATUS_COL + " INTEGER NOT NULL DEFAULT " + STATUS_DEVICE_MODE_DONE;




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
                        EVENTS_TABLE_NAME, MESSAGE_ID, MESSAGE, UPDATED);
                break;
            default:
                createEventSchemaSQL = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                                "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL)",
                        EVENTS_TABLE_NAME, MESSAGE_ID, MESSAGE, UPDATED, STATUS_COL);
        }
        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createEventSchemaSQL: %s", createEventSchemaSQL));
        db.execSQL(createEventSchemaSQL);
        //creating event- transformation table
        db.execSQL(EVENT_TRANSFORMATION_SCHEMA_CREATE_STMT);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    /*
     * save individual messages to DB
     * */
    void saveEvent(String messageJson, List<String> transformationIds) {
        try {
            Message msg = new Message().obtain();
            msg.obj = messageJson;
//          transformation id
            Bundle transformBundle = new Bundle();
            transformBundle.putStringArrayList(TRANSFORMATION_IDS_BUNDLE_KEY,

                    (transformationIds instanceof ArrayList)? (ArrayList<String>)transformationIds :
                    new ArrayList<String>(transformationIds));
            msg.setData(transformBundle);
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
    void saveEventSync(String messageJson){
        ContentValues insertValues = new ContentValues();
        insertValues.put(DBPersistentManager.MESSAGE, messageJson.replaceAll("'", "\\\\\'"));
        insertValues.put(DBPersistentManager.UPDATED, System.currentTimeMillis());
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
     * returns messageIds and messages returned on executing the supplied SQL statement
     * */
    void getEventsFromDB(List<Integer> messageIds, List<String> messages,  String selectSQL) {
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
                Cursor cursor = database.rawQuery(selectSQL, null);
                if (cursor.moveToFirst()) {
                    RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                    while (!cursor.isAfterLast()) {
                        final int messageIdColIndex = cursor.getColumnIndex(MESSAGE_ID);
                        final int messageColIndex = cursor.getColumnIndex(MESSAGE);
                        final int statusColIndex = cursor.getColumnIndex(STATUS_COL);

                        if (messageIdColIndex > -1)
                            messageIdStatusMap.put(cursor.getInt(messageIdColIndex),
                                    statusColIndex > -1 ?cursor.getInt(statusColIndex) : STATUS_DEVICE_MODE_DONE);
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
    void fetchCloudEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {

        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDBForCloud: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }
    //unit test this
    void fetchDeviceModeEventsFromDb(ArrayList<Integer> messageIds, ArrayList<String> messages, int count){
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDBForTransformation: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }


    /*
     * retrieve all messages from DB and store messageIds and messages separately
     * */
    //unit test
    void fetchAllCloudEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME ,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    //unit test
    void fetchAllDeviceEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC", EVENTS_TABLE_NAME ,
                DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW, DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }


    int getCloudDBRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s IN (%d, %d);", EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL,
                DBPersistentManager.STATUS_DEVICE_MODE_DONE, DBPersistentManager.STATUS_NEW);
        return getCountForCommand(countSQL);
    }
    int getDeviceModeRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s IN (%d, %d);", EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, DBPersistentManager.STATUS_NEW);
        return getCountForCommand(countSQL);
    }
    private int getCountForCommand(String sql){
        // initiate count
        int count = -1;

        try {
            // get readable database instance
            SQLiteDatabase database = getReadableDatabase();
            if (database.isOpen()) {

                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", sql));
                Cursor cursor = database.rawQuery(sql, null);
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
                database.execSQL(clearDBSQL);
                RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
            } else {
                RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    @Nullable
    Map<Integer, List<String>> fetchTransformationIdsGroupByEventRowId(List<Integer> rowIds) {
        String query = createTransformationIdsGroupByEventsQuery(rowIds);
        if(query == null) return null;

        Map<Integer, List<String>> eventTransformerIdMap = new HashMap<>();
        Cursor cursor = getWritableDatabase().rawQuery(query, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){/*
            int colCount = cursor.getColumnCount();
            StringBuilder displayer = new StringBuilder();
            for (int i = 0; i < colCount; i++) {
                displayer.append("Col- ").append(cursor.getColumnName(i)).append("-").append(cursor.getString(i)).append("\n");
            }
            System.out.println(displayer.toString());
            Bundle ex = cursor.getExtras();
            if(ex != null) {
                for (String k :
                        ex.keySet()) {
                    System.out.println("Bundle -" + k + " = " + ex.get(k));
                }
            }*/
            int rowIdCol = cursor.getColumnIndex(DBPersistentManager.EVENTS_TRANSFORMER_ROW_ID_COL_NAME);
            if(rowIdCol > -1) {
                int rowId = cursor.getInt(rowIdCol);
                List<String> transformationIds = eventTransformerIdMap.get(rowId);
                if(transformationIds == null){
                    transformationIds = new ArrayList<>();
                    eventTransformerIdMap.put(rowId, transformationIds);
                }
                int transformerIdCol = cursor.getColumnIndex(DBPersistentManager.EVENTS_TRANSFORMER_TRANSFORMATION_ID_COL_NAME);
                if(transformerIdCol > -1) {
                    String transformationId = cursor.getString(transformerIdCol);
                    transformationIds.add(transformationId);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        return eventTransformerIdMap;
    }

    @Nullable
    private String createTransformationIdsGroupByEventsQuery(List<Integer> rowIds) {
        int size = rowIds.size();
        if (size <= 0) return null;
        StringBuilder rowIdsQueryBuilder = new StringBuilder("(" + rowIds.get(0));
        if (size > 1)
            for (int i = 1; i < size; i++) {
                rowIdsQueryBuilder.append(",").append(rowIds.get(i));
            }
        rowIdsQueryBuilder.append(")");

        return "SELECT * FROM " + DBPersistentManager.EVENTS_ROW_ID_TRANSFORMATION_ID_TABLE_NAME +
                " WHERE " + EVENTS_TRANSFORMER_ROW_ID_COL_NAME + " IN " + rowIdsQueryBuilder.toString() +
                " ORDER BY " + EVENTS_TRANSFORMER_ROW_ID_COL_NAME + ";";
    }
}

class DBInsertionHandlerThread extends HandlerThread {

    DBInsertionHandler dbInsertionHandler;
    SQLiteDatabase database;
    //message insertion listener will be called by dbInsertionHandler in it's own thread, and
    //not on main thread
//    private final MessageInsertionListener messageInsertionListener;

    public DBInsertionHandlerThread(String name, SQLiteDatabase database/*,
                                    MessageInsertionListener listener*/) {
        super(name);
        this.database = database;
//        this.messageInsertionListener = listener;
    }

    public void addMessage(Message message) {
        if (dbInsertionHandler == null) {
            dbInsertionHandler = new DBInsertionHandler(getLooper(), this.database);
        }
        dbInsertionHandler.sendMessage(message);
    }

    private class DBInsertionHandler extends Handler {
        SQLiteDatabase database;
//        private MessageInsertionListener listener = null;
        public DBInsertionHandler(Looper looper, SQLiteDatabase database/*, MessageInsertionListener listener*/) {
            super(looper);
            this.database = database;
//            this.listener = listener;
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
                long rowId = database.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues); //rowId will used

                //in event_id->transformId table and sent over to transformation server as orderNo.
//                listener.onMessageInsertedToDb(rowId);
                Bundle msgBundle = msg.getData();

                if(msgBundle != null) {
                    ArrayList<String> transformationIds = msgBundle.getStringArrayList(DBPersistentManager.TRANSFORMATION_IDS_BUNDLE_KEY);
                    if(transformationIds != null && !transformationIds.isEmpty()){
                        database.beginTransaction();

                        for ( String transformationId: transformationIds
                             ){

                            ContentValues eventTransformationValues = new ContentValues();
                            eventTransformationValues.put(DBPersistentManager.EVENTS_TRANSFORMER_ROW_ID_COL_NAME,
                                    rowId);
                            eventTransformationValues.put(DBPersistentManager.EVENTS_TRANSFORMER_TRANSFORMATION_ID_COL_NAME,
                                    transformationId);
                            database.insert(DBPersistentManager.EVENTS_ROW_ID_TRANSFORMATION_ID_TABLE_NAME,null, eventTransformationValues);
                        }
                        database.endTransaction();
                    }
                }
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            } else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }
        }
    }

}/*
interface MessageInsertionListener{
    void onMessageInsertedToDb(long rowId, List);
}*/
