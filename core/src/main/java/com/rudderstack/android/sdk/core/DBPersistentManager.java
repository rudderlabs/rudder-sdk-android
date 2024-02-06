package com.rudderstack.android.sdk.core;

import static com.rudderstack.android.sdk.core.DBPersistentManager.BACKSLASH;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.rudderstack.android.ruddermetricsreporterandroid.RudderReporter;
import com.rudderstack.android.sdk.core.persistence.DefaultPersistenceProviderFactory;
import com.rudderstack.android.sdk.core.persistence.Persistence;
import com.rudderstack.android.sdk.core.persistence.PersistenceProvider;
import com.rudderstack.android.sdk.core.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/*
 * Helper class for SQLite operations
 * */

// error handling needs to be implemented
class DBPersistentManager/* extends SQLiteOpenHelper*/ {

    public static final String DBPERSISTENT_MANAGER_CHECK_FOR_MIGRATIONS_TAG = "DBPersistentManager: checkForMigrations: ";
    public static final Object QUEUE_LOCK = new Object();
    public static final ExecutorService executor = Executors.newSingleThreadExecutor();
    static final String EVENT = "EVENT";

    static final String EVENTS_TABLE_NAME = "events";
    static final String MESSAGE_COL = "message";
    static final String UPDATED_COL = "updated";
    static final String BACKSLASH = "\\\\'";
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    private static final String ENCRYPTED_DB_NAME = "rl_persistence_encrypted.db";
    // SQLite database version number
    private static final int DB_VERSION = 3;
    private static final String MESSAGE_ID_COL = "id";
    private static final String STATUS_COL = "status";
    //status values for database version 2 =. Check createSchema documentation for details.
    private static final int STATUS_CLOUD_MODE_DONE = 0b10;
    private static final int STATUS_DEVICE_MODE_DONE = 0b01;
    private static final int STATUS_ALL_DONE = 0b11;
    private static final int STATUS_NEW = 0b00;
    // This column purpose is to identify if an event is dumped to device mode destinations without transformations or not.
    private static final String DM_PROCESSED_COL = "dm_processed";
    // status value for DM_PROCESSED column
    private static final int DM_PROCESSED_PENDING = 0;
    private static final int DM_PROCESSED_DONE = 1;
    //command to add status column. For details see onUpgrade or the documentation for createSchema
    //for version 1 to 2
    private static final String DATABASE_ALTER_ADD_STATUS = "ALTER TABLE "
            + EVENTS_TABLE_NAME + " ADD COLUMN " + STATUS_COL + " INTEGER NOT NULL DEFAULT " + STATUS_NEW;
    private static final String DATABASE_ALTER_ADD_DM_PROCESSED = "ALTER TABLE "
            + EVENTS_TABLE_NAME + " ADD COLUMN " + DM_PROCESSED_COL + " INTEGER NOT NULL DEFAULT " + DM_PROCESSED_PENDING;
    private static final String SET_STATUS_FOR_EXISTING = "UPDATE " + EVENTS_TABLE_NAME + " SET " + STATUS_COL + " = " + STATUS_DEVICE_MODE_DONE;
    private static final String SET_DM_PROCESSED_AND_STATUS_FOR_EXISTING = "UPDATE " + EVENTS_TABLE_NAME +
            " SET " + DM_PROCESSED_COL + " = " + DM_PROCESSED_DONE +
            ", " + STATUS_COL + " = (" + STATUS_COL + " | " + STATUS_DEVICE_MODE_DONE + ") ";
    // command to create events table based for the database version 1.
    private static final String DATABASE_EVENTS_TABLE_SCHEMA_V1 = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                    "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)",
            EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL);
    // command to create events table based for the database version 3
    private static final String DATABASE_EVENTS_TABLE_SCHEMA_V3 = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' " +
                    "('%s' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'%s' TEXT NOT NULL, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL DEFAULT %d, '%s' INTEGER NOT NULL DEFAULT %s)",
            EVENTS_TABLE_NAME, MESSAGE_ID_COL, MESSAGE_COL, UPDATED_COL, STATUS_COL, STATUS_NEW, DM_PROCESSED_COL, DM_PROCESSED_PENDING);
    private static final String OLD_EVENTS_TABLE = EVENTS_TABLE_NAME + "_old";
    // command to rename the events table to events_old. This command is used as part of the db downgrade operation
    private static final String DATABASE_RENAME_EVENTS_TABLE = "ALTER TABLE " + EVENTS_TABLE_NAME + " RENAME TO " + OLD_EVENTS_TABLE;
    // command to drop the old events table
    private static final String DATABASE_DROP_OLD_EVENTS_TABLE = "DROP TABLE " + OLD_EVENTS_TABLE;
    // columns in the events table after the downgrade operation
    private static final String DOWNGRADED_EVENTS_TABLE_COLUMNS = MESSAGE_COL + ", " + UPDATED_COL;
    // command to copy the data from the old events table to the new table
    private static final String DATABASE_COPY_EVENTS_FROM_OLD_TO_NEW = "INSERT INTO " + EVENTS_TABLE_NAME + "(" + DOWNGRADED_EVENTS_TABLE_COLUMNS + ") SELECT " + DOWNGRADED_EVENTS_TABLE_COLUMNS + " FROM " + OLD_EVENTS_TABLE;
    //synchronizing database access
    private static final Object DB_LOCK = new Object();
    private static DBPersistentManager instance;
    final Queue<Message> queue = new LinkedList<>();
    DBInsertionHandlerThread dbInsertionHandlerThread;
    private Persistence persistence;

    private DBPersistentManager(Application application,
                                PersistenceProvider.Factory persistenceProviderFactory) {
        PersistenceProvider persistenceProvider = persistenceProviderFactory.create(application);
        persistence = persistenceProvider.get(this::onCreate);

        persistence.addDbCloseListener(() -> instance = null);
    }

    private void onCreate() {
        String eventSchemaSQL = getSchemaStatement();
        createSchema(eventSchemaSQL);
    }


    private String getSchemaStatement() {
        if (DB_VERSION == 1) {
            return DATABASE_EVENTS_TABLE_SCHEMA_V1;
        } else {
            return DATABASE_EVENTS_TABLE_SCHEMA_V3;
        }
    }

    static DBPersistentManager getInstance(Application application, DbManagerParams params) {
        PersistenceProvider.Factory persistenceFactory = createPersistenceFactory(params);
        if (instance == null) {
            RudderLogger.logInfo("DBPersistentManager: getInstance: creating instance");
            if (persistenceFactory != null) {
                instance = new DBPersistentManager(application, persistenceFactory);
            } else {
                RudderLogger.logError("DBPersistentManager: Initialization failed. PersistenceFactory is null");

            }
        }
        return instance;
    }

    static @Nullable DBPersistentManager getInstance() {
        return instance;
    }

    private static @Nullable PersistenceProvider.Factory createPersistenceFactory(DbManagerParams params) {
        try {
            String persistenceProviderFactoryClassName = params.persistenceProviderFactoryClassName;
            if (Utils.isEmpty(persistenceProviderFactoryClassName)) {
                RudderLogger.logDebug("DBPersistentManager: persistenceProviderFactoryClassName is null or empty. Switching to default persistence provider");
                persistenceProviderFactoryClassName = DefaultPersistenceProviderFactory.class.getName();

            }
            PersistenceProvider.Factory factory = (PersistenceProvider.Factory) Class.forName(persistenceProviderFactoryClassName).newInstance();
            factory.setDbName(DB_NAME);
            factory.setDbVersion(DB_VERSION);
            factory.setEncryptedDbName(ENCRYPTED_DB_NAME);
            factory.setIsEncrypted(params.isDBEncryptionEnabled);
            factory.setEncryptionKey(params.encryptionKey);
            return factory;
        } catch (Exception e) {
            RudderLogger.logError("DBPersistentManager: createPersistenceFactory: Failed to instantiate class: " + params.persistenceProviderFactoryClassName);
            ReportManager.reportError(e);
        }
        return null;

    }

    private void createSchema(String eventSchemaSQL) {

        RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: createSchema: createEventSchemaSQL: %s", eventSchemaSQL));
        persistence.execSQL(eventSchemaSQL);
        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

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

    private Message createOsMessageFromJson(String messageJson, EventInsertionCallback callback) {
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
        persistence.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues);
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

            if (persistence.isAccessible()) {
                waitTillMigrationsAreDone();
                String deleteSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: flushEvents: deleteSQL: %s", deleteSQL));
                synchronized (DB_LOCK) {
                    persistence.execSQL(deleteSQL);
                }
                RudderLogger.logInfo("DBPersistentManager: flushEvents: Messages deleted from DB");
            } else {
                RudderLogger.logError("DBPersistentManager: flushEvents: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
            ReportManager.reportError(ex);
        }
    }

    /*
     * remove selected events from persistence database storage
     * */
    void clearEventsFromDB(List<Integer> messageIds) {
        try {
            // get writable database
            if (persistence.isAccessible()) {
                waitTillMigrationsAreDone();
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
                        EVENTS_TABLE_NAME, MESSAGE_ID_COL, builder);
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
                persistence.execSQL(deleteSQL);
                RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB");
            } else {
                RudderLogger.logError("DBPersistentManager: clearEventsFromDB: database is not writable");
            }
        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
            ReportManager.reportError(ex);
        }
    }

    /*
     * returns messageIds and messages returned on executing the supplied SQL statement
     * */
    void getEventsFromDB(List<Integer> messageIds, List<String> messages, String
            selectSQL) {
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
            if (!persistence.isAccessible()) {
                RudderLogger.logError("DBPersistentManager: fetchEventsFromDB: database is not readable");
                return;
            }
            Cursor cursor;
            synchronized (DB_LOCK) {
                waitTillMigrationsAreDone();
                cursor = persistence.rawQuery(selectSQL, null);
            }
            if (!cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: DB is empty");
                cursor.close();
                return;
            }
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
            cursor.close();

        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
            ReportManager.reportError(ex);
        }
    }

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    //unit test this
    void fetchCloudModeEventsFromDB
    (ArrayList<Integer> messageIds, ArrayList<String> messages, int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_DEVICE_MODE_DONE, UPDATED_COL, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchCloudModeEventsFromDB: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    //unit test this
    void fetchDeviceModeEventsFromDb(List<Integer> messageIds, List<String> messages,
                                     int count) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, UPDATED_COL, count);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchDeviceModeEventsFromDb: selectSQL: %s", selectSQL));
        getEventsFromDB(messageIds, messages, selectSQL);
    }

    public void fetchDeviceModeWithProcessedPendingEventsFromDb
            (List<Integer> messageIds, List<String> messages, int limit) {
        String selectSQL = String.format(Locale.US, "SELECT * FROM %s WHERE %s IN (%d, %d) AND %s = %d ORDER BY %s ASC LIMIT %d",
                EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL, DBPersistentManager.STATUS_NEW,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, DM_PROCESSED_COL, DM_PROCESSED_PENDING, UPDATED_COL, limit);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchDeviceModeWithProcessedPendingEventsFromDb: selectSQL: %s", selectSQL));
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

    int getDeviceModeWithProcessedPendingEventsRecordCount() {
        String countSQL = String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s IN (%d, %d) AND %s = %d;", EVENTS_TABLE_NAME, DBPersistentManager.STATUS_COL,
                DBPersistentManager.STATUS_CLOUD_MODE_DONE, DBPersistentManager.STATUS_NEW, DBPersistentManager.DM_PROCESSED_COL, DBPersistentManager.DM_PROCESSED_PENDING);
        return getCountForCommand(countSQL);
    }

    private int getCountForCommand(String sql) {
        // initiate count
        int count = -1;

        try {
            // get readable database instance
            if (!persistence.isAccessible()) {
                RudderLogger.logError("DBPersistentManager: getDBRecordCount: database is not readable");
                return count;
            }

            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBRecordCount: countSQL: %s", sql));
            Cursor cursor;
            synchronized (DB_LOCK) {
                cursor = persistence.rawQuery(sql, null);
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

        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError(ex);
            ReportManager.reportError(ex);
        }

        return count;
    }

    /*
       Starts the Handler thread, which is responsible for storing the messages in its internal queue, and
       save them to the sqlite db sequentially.
     */
    void startHandlerThread() {
        Runnable runnable = () -> {
            try {
                synchronized (DBPersistentManager.QUEUE_LOCK) {
                    dbInsertionHandlerThread = new DBInsertionHandlerThread("db_insertion_thread", persistence);
                    dbInsertionHandlerThread.start();
                    for (Message msg : queue) {
                        addMessageToHandlerThread(msg);
                    }
                }
            } catch (SQLiteDatabaseCorruptException | ConcurrentModificationException |
                     NullPointerException ex) {
                RudderLogger.logError(ex);
                ReportManager.reportError(ex);

            }
        };
        // Need to perform db operations on a separate thread to support strict mode.
        executor.execute(runnable);
    }


    private boolean checkIfColumnExists(String newColumn) {
        String checkIfStatusExistsSqlString = "PRAGMA table_info(events)";
        if (!persistence.isAccessible()) {
            RudderLogger.logError("DBPersistentManager: checkIfStatusColumnExists: database is not readable, hence we cannot check the existence of status column");
            return false;
        }
        try (Cursor allRows = persistence.rawQuery(checkIfStatusExistsSqlString, null)) {
            if (allRows == null || !allRows.moveToFirst()) {
                return false;
            }
            do {
                int index = allRows.getColumnIndex("name");
                if (index == -1) {
                    return false;
                }
                String columnName = allRows.getString(index);
                if (columnName.equals(newColumn))
                    return true;

            } while (allRows.moveToNext());

        } catch (SQLiteDatabaseCorruptException ex) {
            RudderLogger.logError("DBPersistentManager: checkIfStatusColumnExists: Exception while checking the presence of status column due to " + ex.getLocalizedMessage());
        }
        return false;
    }

    private final Semaphore migrationSemaphore = new Semaphore(1);

    void checkForMigrations() {
        acquireSemaphore();
        Runnable runnable = () -> {
            try {
                boolean isNewColumnAdded = false;
                if (!checkIfColumnExists(STATUS_COL)) {
                    RudderLogger.logDebug("DBPersistentManager: checkForMigrations: Status column doesn't exist in the events table, hence performing the migration now");
                    performMigration(STATUS_COL);
                    isNewColumnAdded = true;
                }
                if (!checkIfColumnExists(DM_PROCESSED_COL)) {
                    RudderLogger.logDebug("DBPersistentManager: checkForMigrations: dm_processed column doesn't exist in the events table, hence performing the migration now");
                    performMigration(DM_PROCESSED_COL);
                    isNewColumnAdded = true;
                }
                if (!isNewColumnAdded) {
                    RudderLogger.logDebug("DBPersistentManager: checkForMigrations: Status and dm_processed column exists in the table already, hence no migration required");
                }
            } catch (SQLiteDatabaseCorruptException | ConcurrentModificationException |
                     NullPointerException ex) {
                RudderLogger.logError(DBPERSISTENT_MANAGER_CHECK_FOR_MIGRATIONS_TAG + ex.getLocalizedMessage());
            } finally {
                migrationSemaphore.release();
            }
        };
        // Need to perform db operations on a separate thread to support strict mode.
        executor.execute(runnable);
    }

    private void acquireSemaphore() {
        try {
            migrationSemaphore.acquire();
        } catch (InterruptedException e) {
            ReportManager.reportError(e);
            Thread.currentThread().interrupt();
        }
    }
    private void waitTillMigrationsAreDone() {
        if(migrationSemaphore.availablePermits() == 1 ){
            return;
        }
        acquireSemaphore();
        migrationSemaphore.release();
    }

    private void performMigration(String columnName) {
        try {
            if (persistence.isAccessible()) {
                if (columnName.equals(STATUS_COL)) {
                    RudderLogger.logDebug("DBPersistentManager: performMigration: Adding the status column to the events table");
                    persistence.execSQL(DATABASE_ALTER_ADD_STATUS);
                    RudderLogger.logDebug("DBPersistentManager: performMigration: Setting the status to DEVICE_MODE_PROCESSING_DONE for the events existing already in the DB");
                    persistence.execSQL(SET_STATUS_FOR_EXISTING);
                } else if (columnName.equals(DM_PROCESSED_COL)) {
                    RudderLogger.logDebug("DBPersistentManager: performMigration: Adding the dm_processed column to the events table");
                    persistence.execSQL(DATABASE_ALTER_ADD_DM_PROCESSED);
                    // Status also needs to be set to DEVICE_MODE_PROCESSING_DONE for the events already existing already in the DB, otherwise they will be sent again to device mode factories
                    RudderLogger.logDebug("DBPersistentManager: performMigration: Setting the status to DEVICE_MODE_PROCESSING_DONE and the dm_processed to DM_PROCESSED_DONE for the events existing already in the DB");
                    persistence.execSQL(SET_DM_PROCESSED_AND_STATUS_FOR_EXISTING);
                }
            } else {
                RudderLogger.logError("DBPersistentManager: performMigration: persistence is not readable, hence migration cannot be performed");
            }
        } catch (Exception e) {
            RudderLogger.logError("DBPersistentManager: performMigration: Exception while performing the migration due to " + e.getLocalizedMessage());
        }
    }


    public void deleteAllEvents() {
        try {
            if (!persistence.isAccessible()) {
                RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
                return;
            }
            // remove events
            String clearDBSQL = String.format(Locale.US, "DELETE FROM %s", EVENTS_TABLE_NAME);
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: deleteAllEvents: clearDBSQL: %s", clearDBSQL));
            synchronized (DB_LOCK) {
                persistence.execSQL(clearDBSQL);
            }
            RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");

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

    void markDeviceModeTransformationAndDMProcessedDone(List<Integer> rowIds) {
        String rowIdsCSVString = Utils.getCSVString(rowIds);
        if (rowIdsCSVString == null) return;
        String sql = "UPDATE " + DBPersistentManager.EVENTS_TABLE_NAME + " SET " +
                DBPersistentManager.STATUS_COL + " = " + "(" + DBPersistentManager.STATUS_COL + " | " + DBPersistentManager.STATUS_DEVICE_MODE_DONE + ")" +
                ", " + DBPersistentManager.DM_PROCESSED_COL + " = " + DBPersistentManager.DM_PROCESSED_DONE +
                " WHERE " + MESSAGE_ID_COL + " IN "
                + rowIdsCSVString + ";";
        synchronized (DB_LOCK) {
            waitTillMigrationsAreDone();
            persistence.execSQL(sql);
        }
    }

    public void markDeviceModeProcessedDone(Integer rowId) {
        String sql = "UPDATE " + DBPersistentManager.EVENTS_TABLE_NAME + " SET " +
                DBPersistentManager.DM_PROCESSED_COL + " = " + DBPersistentManager.DM_PROCESSED_DONE +
                " WHERE " + MESSAGE_ID_COL + " = " + rowId + ";";
        synchronized (DB_LOCK) {
            waitTillMigrationsAreDone();
            persistence.execSQL(sql);
        }
    }

    //unit test
    private void updateEventStatus(String rowIdsCSVString, int status) {
        String sql = "UPDATE " + DBPersistentManager.EVENTS_TABLE_NAME + " SET " +
                DBPersistentManager.STATUS_COL + " = (" + DBPersistentManager.STATUS_COL + " | " + status +
                ") WHERE " + MESSAGE_ID_COL + " IN "
                + rowIdsCSVString + ";";
        synchronized (DB_LOCK) {
            waitTillMigrationsAreDone();
            persistence.execSQL(sql);
        }
    }

    // Delete all events from the DB which have status as STATUS_ALL_DONE
    void runGcForEvents() {
        deleteDoneEvents();
    }

    private void deleteDoneEvents() {
        synchronized (DB_LOCK) {
            waitTillMigrationsAreDone();
            persistence.delete(EVENTS_TABLE_NAME,
                    DBPersistentManager.STATUS_COL + " = " + DBPersistentManager.STATUS_ALL_DONE,
                    null);
        }
    }

    void deleteFirstEvents(int count) {
        synchronized (DB_LOCK) {
            persistence.delete(EVENTS_TABLE_NAME, MESSAGE_ID_COL + " IN ( " +
                    "SELECT " + MESSAGE_ID_COL + " FROM " + EVENTS_TABLE_NAME +
                    " ORDER BY " + UPDATED_COL + " LIMIT " + count + ");", null);
        }
    }

    public void close() {
        persistence.close();
    }

    static class DbManagerParams {
        final boolean isDBEncryptionEnabled;
        final String persistenceProviderFactoryClassName;
        final String encryptionKey;

        public DbManagerParams(boolean isDBEncryptionEnabled, String persistenceFactoryClassName, String encryptionKey) {
            this.isDBEncryptionEnabled = isDBEncryptionEnabled;
            this.persistenceProviderFactoryClassName = persistenceFactoryClassName;
            this.encryptionKey = encryptionKey;
        }
    }
}

class DBInsertionHandlerThread extends HandlerThread {

    DBInsertionHandler dbInsertionHandler;
    Persistence persistence;

    public DBInsertionHandlerThread(String name, Persistence persistence) {
        super(name);
        this.persistence = persistence;
    }

    public void addMessage(Message message) {
        if (dbInsertionHandler == null) {
            dbInsertionHandler = new DBInsertionHandler(getLooper(), this.persistence);
        }
        dbInsertionHandler.sendMessage(message);
    }

    private static class DBInsertionHandler extends Handler {
        Persistence persistence;

        public DBInsertionHandler(Looper looper, Persistence persistence) {
            super(looper);
            this.persistence = persistence;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (this.persistence.isAccessible()) {
                EventInsertionCallback callback = (EventInsertionCallback) msg.obj;
                Bundle msgBundle = msg.getData();
                String messageJson = msgBundle.getString(DBPersistentManager.EVENT);
                long updatedTime = System.currentTimeMillis();
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: " +
                                "saveEvent: Inserting Message %s into table %s as Updated at %d",
                        messageJson.replace("'", BACKSLASH), DBPersistentManager.EVENTS_TABLE_NAME, updatedTime));
                ContentValues insertValues = new ContentValues();
                insertValues.put(DBPersistentManager.MESSAGE_COL, messageJson.replace("'", BACKSLASH));
                insertValues.put(DBPersistentManager.UPDATED_COL, updatedTime);
                long rowId = persistence.insert(DBPersistentManager.EVENTS_TABLE_NAME, null, insertValues); //rowId will used
                callback.onInsertion((int) rowId);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
            } else {
                RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
            }
        }
    }

}
