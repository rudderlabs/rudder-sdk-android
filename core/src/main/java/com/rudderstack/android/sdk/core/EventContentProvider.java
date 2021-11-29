package com.rudderstack.android.sdk.core;

import static android.provider.Telephony.BaseMmsColumns.MESSAGE_ID;
import static com.rudderstack.android.sdk.core.EventsDbHelper.EVENTS_TABLE_NAME;
import static com.rudderstack.android.sdk.core.EventsDbHelper.UPDATED;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class EventContentProvider extends ContentProvider {
    static final String AUTHORITY =
            EventContentProvider.class.getCanonicalName();

    static final Uri CONTENT_URI_EVENTS =
            Uri.parse("content://" + AUTHORITY + "/" + EVENTS_TABLE_NAME);

    static final UriMatcher uriMatcher;
    static final String QUERY_PARAMETER_LIMIT = "limit";
    private final static int EVENT_CODE = 1;
    private final static int EVENT_ID_CODE = 2;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME, EVENT_CODE);
        uriMatcher.addURI(AUTHORITY, EVENTS_TABLE_NAME + "/#", EVENT_ID_CODE);
    }

    private EventsDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new EventsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT);
        return dbHelper.getWritableDatabase().query(EVENTS_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder, limit);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        long rowID = dbHelper.getWritableDatabase().insert(EVENTS_TABLE_NAME, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI_EVENTS, rowID);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case EVENT_CODE:
                count = dbHelper.getWritableDatabase().delete(EVENTS_TABLE_NAME, selection, selectionArgs);
                break;

            case EVENT_ID_CODE:
                String id = uri.getPathSegments().get(1);
                count = dbHelper.getWritableDatabase().delete(EVENTS_TABLE_NAME, MESSAGE_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case EVENT_CODE:
                count = dbHelper.getWritableDatabase().update(EVENTS_TABLE_NAME, values, selection, selectionArgs);
                break;

            case EVENT_ID_CODE:
                count = dbHelper.getWritableDatabase().update(EVENTS_TABLE_NAME, values,
                        MESSAGE_ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
        }
        return count;
    }


}
