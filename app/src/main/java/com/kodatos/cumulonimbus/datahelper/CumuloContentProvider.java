package com.kodatos.cumulonimbus.datahelper;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
                                    retCursor = db.query(WeatherDBEntry.TABLE_NAME,projection,mselection,margs,null,null,sortOrder);
                                    break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
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
            case WEATHER_DATA : long id = db.insert(WeatherDBEntry.TABLE_NAME,null,values);
                if (id>0) {
                    retUri = WeatherDBEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
                }
                else {
                    throw new SQLException("Insert Failed!");
                }
                break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return retUri;
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
                                if(retno<=0){
                                    Log.d("CONTENT_PROVIDER", "Update Failed!");
                                }
                                break;
            default: throw new UnsupportedOperationException("Unknown Uri "+uri.toString());
        }
        if (selectionArgs != null && values != null && ("33".equals(selectionArgs[0]) && !values.containsKey(WeatherDBEntry.COLUMN_WEATHER_DESC)))
            getContext().getContentResolver().notifyChange(uri, null);
        return retno;
    }
}
