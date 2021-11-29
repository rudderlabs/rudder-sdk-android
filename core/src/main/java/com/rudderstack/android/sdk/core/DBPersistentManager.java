package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.EventsDbHelper.MESSAGE;
import static com.rudderstack.android.sdk.core.EventsDbHelper.MESSAGE_ID;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager {

    DBInsertionHandlerThread dbInsertionHandlerThread;
    final Queue<Message> queue = new LinkedList<Message>();



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
//            SQLiteDatabase database = getWritableDatabase();
//            if (database.isOpen()) {
//                String deleteSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: flushEvents: deleteSQL: %s", deleteSQL));
//                database.execSQL(deleteSQL);
            application.getContentResolver().delete(EventContentProvider.CONTENT_URI_EVENTS, null, null);
                RudderLogger.logInfo("DBPersistentManager: flushEvents: Messages deleted from DB");
            /*} else {
                RudderLogger.logError("DBPersistentManager: flushEvents: database is not writable");
            }*/
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
//            SQLiteDatabase database = getWritableDatabase();
//            if (database.isOpen()) {
                RudderLogger.logInfo(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: Clearing %d messages from DB", messageIds.size()));
                // format CSV string from messageIds list
//                StringBuilder builder = new StringBuilder();
//                for (int index = 0; index < messageIds.size(); index++) {
//                    builder.append(messageIds.get(index));
//                    builder.append(",");
//                }
                // remove last "," character
//                builder.deleteCharAt(builder.length() - 1);
                // remove events
            String[] args = new String[messageIds.size()];
            for (int i = 0; i < messageIds.size(); i++) {
                args[i] = messageIds.get(i).toString();
            }
                application.getContentResolver().delete(EventContentProvider.CONTENT_URI_EVENTS, MESSAGE_ID + "=?",
                        args);
                /*String deleteSQL = String.format(Locale.US, "DELETE FROM %s WHERE %s IN (%s)",
                        EVENTS_TABLE_NAME, MESSAGE_ID, builder.toString());*/
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
//                database.execSQL(deleteSQL);
                RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB");
            /*} else {
                RudderLogger.logError("DBPersistentManager: clearEventsFromDB: database is not writable");
            }*/
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
//            SQLiteDatabase database = getReadableDatabase();
//            if (database.isOpen()) {
//                String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, UPDATED, count);
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
//                Cursor cursor = database.rawQuery(selectSQL, null);
                Uri contentUri = EventContentProvider.CONTENT_URI_EVENTS.buildUpon()
                        .appendQueryParameter(EventContentProvider.QUERY_PARAMETER_LIMIT,
                                String.valueOf(count)).build();
                Cursor cursor = application.getContentResolver().query(contentUri,
                        null,null,null, EventsDbHelper.UPDATED + "ASC");
                if (cursor.moveToFirst()) {
                    RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                    while (!cursor.isAfterLast()) {
                        final int messageIdColIndex = cursor.getColumnIndex(MESSAGE_ID);
                        final int messageColIndex = cursor.getColumnIndex(MESSAGE);
                        if(messageIdColIndex > -1)
                            messageIds.add(cursor.getInt(messageIdColIndex));
                        if(messageColIndex > -1)
                            messages.add(cursor.getString(messageColIndex));
                        cursor.moveToNext();
                    }
                } else {
                    RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: DB is empty");
                }
                cursor.close();
            /*} else {
                RudderLogger.logError("DBPersistentManager: fetchEventsFromDB: database is not readable");
            }*/
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    void fetchEventsFromDB(ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, UPDATED, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    /*
     * retrieve all messages from DB and store messageIds and messages separately
     * */
    void fetchAllEventsFromDB(List<Integer> messageIds, List<String> messages) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC", EVENTS_TABLE_NAME, UPDATED);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchAllEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }


    int getDBRecordCount() {
        // initiate count
        int count = -1;

        try {
            // get readable database instance
//            SQLiteDatabase database = getReadableDatabase();
//            if (database.isOpen()) {
//                String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s;", EVENTS_TABLE_NAME);
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", countSQL));
//                Cursor cursor = database.rawQuery(countSQL, null);
                Cursor cursor = application.getContentResolver().query(EventContentProvider.CONTENT_URI_EVENTS,
                        new String[] {"count(*) AS count"},
                        null,
                        null,
                        null);
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
            /*} else {
                RudderLogger.logError("DBPersistentManager: getDBRecordCount: database is not readable");
            }*/
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }

        return count;
    }

    private static DBPersistentManager instance;
    private Application application;

    static DBPersistentManager getInstance(Application application) {
        if (instance == null) {
            RudderLogger.logInfo("DBPersistentManager: getInstance: creating instance");
            instance = new DBPersistentManager(application);
        }
        return instance;
    }


    private DBPersistentManager(final Application application) {
        this.application = application;
        // Need to perform db operations on a separate thread to support strict mode.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//                    DBPersistentManager.this.getWritableDatabase();
                    synchronized (this) {
                        dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread", application);
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





    public void deleteAllEvents() {
        try {
//            SQLiteDatabase database = getWritableDatabase();
//            if (database.isOpen()) {
                // remove events
//                String clearDBSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: deleteAllEvents: clearDBSQL: %s", clearDBSQL));
//                database.execSQL(clearDBSQL);
            application.getContentResolver().delete(EventContentProvider.CONTENT_URI_EVENTS,null,null);
                RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
//            } else {
//                RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
//            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }
    private String getProcessName() {
        int mypid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == mypid) {
                return info.processName;
            }
        }
        // may never return null
        return null;
    }
}

class DBInsertionHandlerThread extends HandlerThread {

    DBInsertionHandler dbInsertionHandler;
    private Context context;

    public DBInsertionHandlerThread(String name, Context context) {
        super(name);
        this.context = context;
    }

    public void addMessage(Message message) {
        if (dbInsertionHandler == null) {
            dbInsertionHandler = new DBInsertionHandler(getLooper(), context);
        }
        dbInsertionHandler.sendMessage(message);
    }

    private class DBInsertionHandler extends Handler {

        private Context context;
        public DBInsertionHandler(Looper looper,Context context) {
            super(looper);
            this.context = context;

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            if (this.database.isOpen()) {
                String messageJson = (String) msg.obj;
                long updatedTime = System.currentTimeMillis();
//                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: Inserting Message %s into table %s as Updated at %d", messageJson.replaceAll("'", "\\\\\'"), DBPersistentManager.EVENTS_TABLE_NAME, updatedTime));
                ContentValues insertValues = new ContentValues();
                insertValues.put(EventsDbHelper.MESSAGE, messageJson.replaceAll("'", "\\\\\'"));
                insertValues.put(EventsDbHelper.UPDATED, updatedTime);

                context.getContentResolver().insert(EventContentProvider.CONTENT_URI_EVENTS, insertValues);
//                database.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            /*} else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }*/
        }
    }
}
