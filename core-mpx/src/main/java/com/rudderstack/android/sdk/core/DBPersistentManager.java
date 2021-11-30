package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.EventsDbHelper.MESSAGE;
import static com.rudderstack.android.sdk.core.EventsDbHelper.MESSAGE_ID;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.net.Uri;
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
            DBPersistentManager.initializeUri(application);

            application.getContentResolver().delete(EventContentProvider.getContentUri(DBPersistentManager.getUri(application)), null, null);
            RudderLogger.logInfo("DBPersistentManager: flushEvents: Messages deleted from DB");
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    /*
     * remove selected events from persistence database storage
     * */
    void clearEventsFromDB(List<Integer> messageIds) {
        try {
            RudderLogger.logInfo(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: Clearing %d messages from DB", messageIds.size()));
            // format CSV string from messageIds list
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < messageIds.size(); index++) {
                builder.append(messageIds.get(index));
                builder.append(",");
            }
            //  remove last "," character
            builder.deleteCharAt(builder.length() - 1);
            // remove events
            DBPersistentManager.initializeUri(application);
            int deleted = application.getContentResolver().delete(EventContentProvider.getContentUri(DBPersistentManager.getUri(application)), String.format(MESSAGE_ID + " IN (%s)", builder.toString()),
                    null);
            RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB " + deleted);
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
            Uri contentUri = EventContentProvider.getContentUri(DBPersistentManager.getUri(application)).buildUpon()
                    .appendQueryParameter(EventContentProvider.QUERY_PARAMETER_LIMIT,
                            String.valueOf(count)).build();
            DBPersistentManager.initializeUri(application);
            Cursor cursor = application.getContentResolver().query(contentUri,
                    null, null, null, EventsDbHelper.UPDATED + " ASC");
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
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    int getDBRecordCount() {
        // initiate count
        int count = -1;

        try {
            DBPersistentManager.initializeUri(application);
            Cursor cursor = application.getContentResolver().query(EventContentProvider.getContentUri(DBPersistentManager.getUri(application)),
                    new String[]{"count(*) AS count"},
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
            DBPersistentManager.initializeUri(application);
            application.getContentResolver().delete(EventContentProvider.getContentUri(DBPersistentManager.getUri(application)), null, null);
            RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
        }
    }

    private String getProcessName() {
        int mypid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == mypid) {
                return info.processName;
            }
        }
        // may never return null
        return null;
    }
    static void initializeUri(Context context){
        if(EventContentProvider.authority == null)
            EventContentProvider.authority = context.getApplicationContext().getPackageName()+"."+EventContentProvider.class.getSimpleName();
    }
    static String getUri(Context context){
//        if(EventContentProvider.authority == null)
            return context.getApplicationContext().getPackageName()+"."+EventContentProvider.class.getSimpleName();
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

        public DBInsertionHandler(Looper looper, Context context) {
            super(looper);
            this.context = context;

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String messageJson = (String) msg.obj;
            long updatedTime = System.currentTimeMillis();
            ContentValues insertValues = new ContentValues();
            insertValues.put(EventsDbHelper.MESSAGE, messageJson.replaceAll("'", "\\\\\'"));
            insertValues.put(EventsDbHelper.UPDATED, updatedTime);
            DBPersistentManager.initializeUri(context);
            context.getContentResolver().insert(EventContentProvider.getContentUri(DBPersistentManager.getUri(context)), insertValues);
            RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
        }
    }

}
