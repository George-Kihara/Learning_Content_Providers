package com.ninja.learn.learn_cp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URI;
import java.util.HashMap;

public class EplTeamsProvider extends ContentProvider {
    static final String PROVIDER_NAME = "epl";
    static final String URL = "content://" + PROVIDER_NAME + "/teams";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String _ID = "_id";
    static final String TEAM_NAME = "team_name";
    static final String TEAM_POINTS = "team_points";

    private static HashMap<String, String> TEAMS_PROJECTION_MAP;

    static final int TEAMS = 1;
    static final int TEAMS_ID = 2;

    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "teams", TEAMS);
        uriMatcher.addURI(PROVIDER_NAME, "teams/#", TEAMS_ID);
    }

    /**
     * Database specific constant declarations
     */

    private SQLiteDatabase db;
    static final String DB_NAME = "epl";
    static final String TEAMS_TABLE_NAME = "teams";
    static final int DB_VERSION = 1;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + TEAMS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " team_name TEXT NOT NULL, " +
                    " team_points TEXT NOT NULL);";


    /**
     * Helper class that creates and manages provider's data repository
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DB_NAME,null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(" DROP TABLE IF EXISTS " + TEAMS_TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    public EplTeamsProvider() {}

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        //create a writable db
        db = dbHelper.getWritableDatabase();
        return db != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TEAMS_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case TEAMS:
                qb.setProjectionMap(TEAMS_PROJECTION_MAP);
                break;
            case TEAMS_ID:
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                // do nothing
        }

        // sorting
        if (sortOrder == null || sortOrder.equals("")) {
            // sort using team names
            sortOrder = TEAM_NAME;
        }

        // register to watch a content URI for changes
        Cursor c = qb.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch(uriMatcher.match(uri)) {
            // get all team records
            case TEAMS:
                return "vnd.android.cursor.dir/teams";
            // get a particular team
            case TEAMS_ID:
                return "vnd.android.cursor.item/teams";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        // add a new record
        long rowId = db.insert(TEAMS_TABLE_NAME, "", contentValues);

        // check if record was added successfully
        if (rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case TEAMS:
                count = db.delete(TEAMS_TABLE_NAME, selection, selectionArgs);
                break;
            case TEAMS_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(TEAMS_TABLE_NAME, _ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ?
                                " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case TEAMS:
                count = db.update(TEAMS_TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case TEAMS_ID:
                db.update(TEAMS_TABLE_NAME, contentValues,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ?
                                        " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
