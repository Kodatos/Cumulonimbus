/*
 * MIT License
 *
 * Copyright (c) 2017 N Abhishek (aka Kodatos)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.kodatos.cumulonimbus.datahelper;


import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Objects;

import static com.kodatos.cumulonimbus.datahelper.WeatherDBContract.WeatherDBEntry;


public class CumuloContentProvider extends ContentProvider {
    /* Only supports query, insert and update
       any delete call or unsupported uri will lead to UnsupportedOperationException*/

    //Codes for supported uris
    public static final int WEATHER_DATA = 100;
    public static final int WEATHER_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildMatcher();
    private WeatherDBHelper mDBHelper;

    //URIMatcher builder for two uris
    public static UriMatcher buildMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WeatherDBContract.AUTHORITY, WeatherDBContract.PATH_WEATHER_DATA, WEATHER_DATA);
        uriMatcher.addURI(WeatherDBContract.AUTHORITY, WeatherDBContract.PATH_WEATHER_DATA +"/#", WEATHER_WITH_ID);
        uriMatcher.addURI(WeatherDBContract.AUTHORITY, WeatherDBContract.PATH_WEATHER_DATA + "/uvupdate", WEATHER_DATA);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDBHelper = new WeatherDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mDBHelper.getReadableDatabase();
        int code = sUriMatcher.match(uri);
        Cursor retCursor;
        switch (code){
            case WEATHER_DATA : retCursor = db.query(WeatherDBEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case WEATHER_WITH_ID :  String id = uri.getPathSegments().get(1);
                String mselection = "_id=?";
                String[] margs = new String[]{id};
                retCursor = db.query(WeatherDBEntry.TABLE_NAME, projection, mselection, margs, null, null, sortOrder);
                break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        retCursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        Uri retUri;
        switch (code){
            case WEATHER_DATA:
                db.beginTransaction();
                long id = db.insert(WeatherDBEntry.TABLE_NAME, null, values);
                if (id>0) {
                    db.setTransactionSuccessful();
                    retUri = WeatherDBEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                    db.endTransaction();
                } else {
                    db.endTransaction();
                    throw new SQLException("Insert Failed!");
                }
                break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri,null);
        return retUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        int result;
        switch (code) {
            case WEATHER_DATA:
                db.beginTransaction();
                try {
                    for (ContentValues cv : values) {
                        long id = db.insert(WeatherDBEntry.TABLE_NAME, null, cv);
                        if (id < 0)
                            throw new SQLException("Bulk Insert Failed!");
                    }
                    result = values.length;
                    db.setTransactionSuccessful();
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri.toString());
        }
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not Supported!");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int code = sUriMatcher.match(uri);
        int retno;
        switch(code){
            case WEATHER_DATA : retno = db.update(WeatherDBEntry.TABLE_NAME,values,selection,selectionArgs);
                if (retno < 0) {
                    Log.w("CONTENT_PROVIDER", "Update Failed!");
                }
                break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        return retno;
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int size = operations.size();
            ContentProviderResult[] results = new ContentProviderResult[size];
            for (int i = 0; i < size; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            if (operations.get(0).getUri().getLastPathSegment().equals("uvupdate"))
                Objects.requireNonNull(getContext()).getContentResolver().notifyChange(WeatherDBEntry.CONTENT_URI, null);
            return results;
        } finally {
            db.endTransaction();
        }
    }
}
