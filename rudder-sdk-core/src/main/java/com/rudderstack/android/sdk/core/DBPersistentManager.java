package com.rudderstack.android.sdk.core;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/*
 * Helper class for SQLite operations
 * */
class DBPersistentManager extends SQLiteOpenHelper {
    // SQLite database file name
    private static final String DB_NAME = "rl_persistence.db";
    // SQLite database version number
    private static final int DB_VERSION = 1;
    private static final String EVENTS_TABLE_NAME = "events";
    private static final String EVENTS_FIELD_MESSAGE = "message";
    private static final String EVENTS_FIELD_MESSAGE_ID = "id";
    private static final String EVENTS_FIELD_UPDATED = "updated";

    static final String METRICS_EVENT_TABLE_NAME = "metrics_events";
    static final String METRICS_EVENT_FIELD_ID = "id";
    static final String METRICS_EVENT_FIELD_SIZE = "size";

    static final String METRICS_DATA_PLANE_TABLE_NAME = "metrics_data_plane";
    static final String METRICS_DATA_PLANE_FIELD_ID = "id";
    static final String METRICS_DATA_PLANE_FIELD_SIZE = "batch_size";
    static final String METRICS_DATA_PLANE_FIELD_SUCCESS = "success";
    static final String METRICS_DATA_PLANE_FIELD_RESPONSE_TIME = "response_time";

    static final String METRICS_CONFIG_PLANE_TABLE_NAME = "metrics_config_plane";
    static final String METRICS_CONFIG_PLANE_FIELD_ID = "id";
    static final String METRICS_CONFIG_PLANE_FIELD_SUCCESS = "success";
    static final String METRICS_CONFIG_PLANE_FIELD_RESPONSE_TIME = "response_time";

    private static final String METRICS_REQUEST_TABLE_NAME = "metrics_request";
    private static final String METRICS_REQUEST_FIELD_REQUEST = "request";
    private static final String METRICS_REQUEST_FIELD_REQUEST_ID = "id";
    private static final String METRICS_REQUEST_FIELD_UPDATED = "updated";

    /*
     * create table initially if not exists
     * */
    private void createSchema(SQLiteDatabase db) {
        String eventTableSchemaSql = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)", EVENTS_TABLE_NAME, EVENTS_FIELD_MESSAGE_ID, EVENTS_FIELD_MESSAGE, EVENTS_FIELD_UPDATED);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: createSchema: eventTableSchemaSql: %s", eventTableSchemaSql));
        db.execSQL(eventTableSchemaSql);

        String metricsEventTableSchemaSql = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' INTEGER NOT NULL)", METRICS_EVENT_TABLE_NAME, METRICS_EVENT_FIELD_ID, METRICS_EVENT_FIELD_SIZE);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: createSchema: metricsEventTableSchemaSql: %s", metricsEventTableSchemaSql));
        db.execSQL(metricsEventTableSchemaSql);

        String metricsDataTableSchemaSql = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL)", METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_ID, METRICS_DATA_PLANE_FIELD_SIZE, METRICS_DATA_PLANE_FIELD_SUCCESS, METRICS_DATA_PLANE_FIELD_RESPONSE_TIME);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: createSchema: metricsDataTableSchemaSql: %s", metricsDataTableSchemaSql));
        db.execSQL(metricsDataTableSchemaSql);

        String metricsConfigTableSchemaSql = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' INTEGER NOT NULL, '%s' INTEGER NOT NULL)", METRICS_CONFIG_PLANE_TABLE_NAME, METRICS_CONFIG_PLANE_FIELD_ID, METRICS_CONFIG_PLANE_FIELD_SUCCESS, METRICS_CONFIG_PLANE_FIELD_RESPONSE_TIME);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: createSchema: metricsConfigTableSchemaSql: %s", metricsConfigTableSchemaSql));
        db.execSQL(metricsConfigTableSchemaSql);

        String metricsRequestTableSchemaSql = String.format(Locale.US, "CREATE TABLE IF NOT EXISTS '%s' ('%s' INTEGER PRIMARY KEY AUTOINCREMENT, '%s' TEXT NOT NULL, '%s' INTEGER NOT NULL)", METRICS_REQUEST_TABLE_NAME, METRICS_REQUEST_FIELD_REQUEST_ID, METRICS_REQUEST_FIELD_REQUEST, METRICS_REQUEST_FIELD_UPDATED);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: createSchema: metricsRequestTableSchemaSql: %s", metricsRequestTableSchemaSql));
        db.execSQL(metricsRequestTableSchemaSql);

        RudderLogger.logInfo("DBPersistentManager: createSchema: DB Schema created");
    }

    /*
     * save individual messages to DB
     * */
    void saveEvent(String messageJson, boolean recordSize) {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            String saveEventSQL = String.format(Locale.US, "INSERT INTO %s (%s, %s) VALUES ('%s', %d)",
                    EVENTS_TABLE_NAME, EVENTS_FIELD_MESSAGE, EVENTS_FIELD_UPDATED, messageJson.replaceAll("'", "\\\\\'"), System.currentTimeMillis());
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: saveEventSQL: %s", saveEventSQL));
            database.execSQL(saveEventSQL);
            RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");

            if (recordSize) {
                RudderLogger.logInfo("DBPersistentManager: saving event size");
                String saveEventSizeSql = String.format(Locale.US, "INSERT INTO %s (%s) VALUES (%d)", METRICS_EVENT_TABLE_NAME, METRICS_EVENT_FIELD_SIZE, messageJson.length());
                RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: saveEventSizeSql: %s", saveEventSizeSql));
                database.execSQL(saveEventSizeSql);
                RudderLogger.logInfo("DBPersistentManager: saveEvent: Event size saved to DB");
            }
        } else {
            RudderLogger.logError("DBPersistentManager: saveEvent: database is not writable");
        }
    }

    /*
     * remove selected events from persistence database storage
     * */
    void clearEventsFromDB(SparseArray<String> events) {
        // get writable database
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            RudderLogger.logInfo(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: Clearing %d messages from DB", events.size()));
            // format CSV string from messageIds list
            StringBuilder builder = new StringBuilder();
            for (int index = 0; index < events.size(); index++) {
                builder.append(events.keyAt(index));
                builder.append(",");
            }
            // remove last "," character
            builder.deleteCharAt(builder.length() - 1);
            // remove events
            String deleteSQL = String.format(Locale.US, "DELETE FROM %s WHERE %s IN (%s)",
                    EVENTS_TABLE_NAME, EVENTS_FIELD_MESSAGE_ID, builder.toString());
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearEventsFromDB: deleteSQL: %s", deleteSQL));
            database.execSQL(deleteSQL);
            RudderLogger.logInfo("DBPersistentManager: clearEventsFromDB: Messages deleted from DB");
        } else {
            RudderLogger.logError("DBPersistentManager: clearEventsFromDB: database is not writable");
        }
    }

    /*
     * retrieve `count` number of messages from DB and store messageIds and messages separately
     * */
    SparseArray<String> fetchEventsFromDB(int count) {
        SparseArray<String> events = new SparseArray<>();

        // get readable database instance
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC LIMIT %d", EVENTS_TABLE_NAME, EVENTS_FIELD_UPDATED, count);
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
            Cursor cursor = database.rawQuery(selectSQL, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                while (!cursor.isAfterLast()) {
                    events.put(cursor.getInt(cursor.getColumnIndex(EVENTS_FIELD_MESSAGE_ID)), cursor.getString(cursor.getColumnIndex(EVENTS_FIELD_MESSAGE)));
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: DB is empty");
            }
            cursor.close();
        } else {
            RudderLogger.logError("DBPersistentManager: fetchEventsFromDB: database is not readable");
        }
        return events;
    }

    int getDBRecordCount() {
        // initiate count
        return this.getDBCount(String.format(Locale.US, "SELECT count(*) FROM %s;", EVENTS_TABLE_NAME));
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
        this.deleteTableData(EVENTS_TABLE_NAME);
    }

    void recordDataPlaneRequest(int batchSize, int success, long responseTime) {
        SQLiteDatabase database = getWritableDatabase();
        String saveDataPlaneRequestSQL = String.format(Locale.US, "INSERT INTO %s (%s, %s, %s) VALUES ('%d', %d, %d)",
                METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_SIZE, METRICS_DATA_PLANE_FIELD_SUCCESS, METRICS_DATA_PLANE_FIELD_RESPONSE_TIME, batchSize, success, responseTime);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: saveEventSQL: %s", saveDataPlaneRequestSQL));
        database.execSQL(saveDataPlaneRequestSQL);
        RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
    }

    void recordConfigPlaneRequest(int success, long responseTime) {
        SQLiteDatabase database = getWritableDatabase();
        String saveConfigPlaneRequestSQL = String.format(Locale.US, "INSERT INTO %s (%s, %s) VALUES ('%d', %d)",
                METRICS_CONFIG_PLANE_TABLE_NAME, METRICS_CONFIG_PLANE_FIELD_SUCCESS, METRICS_CONFIG_PLANE_FIELD_RESPONSE_TIME, success, responseTime);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveEvent: saveConfigPlaneRequestSQL: %s", saveConfigPlaneRequestSQL));
        database.execSQL(saveConfigPlaneRequestSQL);
        RudderLogger.logInfo("DBPersistentManager: saveEvent: Event saved to DB");
    }

    SparseArray<String> getMetricsRequests() {
        SparseArray<String> requests = new SparseArray<>();
        // get readable database instance
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            String selectSQL = String.format(Locale.US, "SELECT * FROM %s ORDER BY %s ASC", METRICS_REQUEST_TABLE_NAME, METRICS_REQUEST_FIELD_UPDATED);
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: fetchEventsFromDB: selectSQL: %s", selectSQL));
            Cursor cursor = database.rawQuery(selectSQL, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: fetched messages from DB");
                while (!cursor.isAfterLast()) {
                    requests.put(cursor.getInt(cursor.getColumnIndex(METRICS_REQUEST_FIELD_REQUEST_ID)), cursor.getString(cursor.getColumnIndex(METRICS_REQUEST_FIELD_REQUEST)));
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logInfo("DBPersistentManager: fetchEventsFromDB: DB is empty");
            }
            cursor.close();
        } else {
            RudderLogger.logError("DBPersistentManager: fetchEventsFromDB: database is not readable");
        }
        return requests;
    }

    void clearMetricsRequestFromDB(String idListCsv) {
        SQLiteDatabase database = getWritableDatabase();
        String deleteSQL = String.format(Locale.US, "DELETE FROM %s WHERE %s IN (%s)",
                METRICS_REQUEST_TABLE_NAME, METRICS_REQUEST_FIELD_REQUEST_ID, idListCsv);
        RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: clearRequestFromDB: deleteSQL: %s", deleteSQL));
        database.execSQL(deleteSQL);
    }

    List<Integer> getStats(String sql) {
        // return a sorted list
        List<Integer> dataList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            RudderLogger.logVerbose(String.format(Locale.US, "DBPersistentManager: getStats: SQL: %s", sql));
            Cursor cursor = database.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: getDBRecordCount: fetched count from DB");
                while (!cursor.isAfterLast()) {
                    // result will be in the first position
                    dataList.add(cursor.getInt(0));
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logVerbose("DBPersistentManager: getStats: DB is empty");
            }
            // release cursor
            cursor.close();
            Collections.sort(dataList);
            return dataList;
        } else {
            RudderLogger.logError("DBPersistentManager: getStats: database is not readable");
            return null;
        }
    }

    private int getDBCount(String countSQL) {
        int count = -1;

        // get readable database instance
        SQLiteDatabase database = getReadableDatabase();
        if (database.isOpen()) {
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: getDBCount: countSQL: %s", countSQL));
            Cursor cursor = database.rawQuery(countSQL, null);
            if (cursor.moveToFirst()) {
                RudderLogger.logInfo("DBPersistentManager: getDBCount: fetched count from DB");
                while (!cursor.isAfterLast()) {
                    // result will be in the first position
                    count = cursor.getInt(0);
                    cursor.moveToNext();
                }
            } else {
                RudderLogger.logInfo("DBPersistentManager: getDBCount: DB is empty");
            }
            // release cursor
            cursor.close();
        } else {
            RudderLogger.logError("DBPersistentManager: getDBRecordCount: database is not readable");
        }

        return count;
    }

    int getRetryCountConfigPlane() {
        return this.getDBCount(String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s=0;", METRICS_CONFIG_PLANE_TABLE_NAME, METRICS_CONFIG_PLANE_FIELD_SUCCESS));
    }

    int getRetryCountDataPlane() {
        return this.getDBCount(String.format(Locale.US, "SELECT count(*) FROM %s WHERE %s=0;", METRICS_DATA_PLANE_TABLE_NAME, METRICS_DATA_PLANE_FIELD_SUCCESS));
    }

    void saveStatsRequest(String url) {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            String saveRequestSQL = String.format(Locale.US, "INSERT INTO %s (%s, %s) VALUES ('%s', %d)", METRICS_REQUEST_TABLE_NAME, METRICS_REQUEST_FIELD_REQUEST, METRICS_REQUEST_FIELD_UPDATED, url, System.currentTimeMillis());
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: saveRequest: saveRequestSQL: %s", saveRequestSQL));
            database.execSQL(saveRequestSQL);
            RudderLogger.logInfo("DBPersistentManager: saveRequest: Event saved to DB");
        } else {
            RudderLogger.logError("DBPersistentManager: saveRequest: database is not writable");
        }
    }

    void deleteAllMetrics() {
        deleteTableData(METRICS_EVENT_TABLE_NAME);
        deleteTableData(METRICS_CONFIG_PLANE_TABLE_NAME);
        deleteTableData(METRICS_DATA_PLANE_TABLE_NAME);
    }

    private void deleteTableData(String tableName) {
        SQLiteDatabase database = getWritableDatabase();
        if (database.isOpen()) {
            // remove events
            String clearDBSQL = String.format(Locale.US, "DELETE FROM %s", tableName);
            RudderLogger.logDebug(String.format(Locale.US, "DBPersistentManager: deleteAllEvents: clearDBSQL: %s", clearDBSQL));
            database.execSQL(clearDBSQL);
            RudderLogger.logInfo("DBPersistentManager: deleteAllEvents: deleted all events");
        } else {
            RudderLogger.logError("DBPersistentManager: deleteAllEvents: database is not writable");
        }
    }
}
