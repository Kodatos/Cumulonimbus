package com.kodatos.cumulonimbus.datahelper;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CumuloContentProvider extends ContentProvider {

    public static final int WEATHER_DATA = 100;
    public static final int WEATHER_WITH_ID = 101;

    private WeatherDBHelper mDBHelper;
    private static final UriMatcher sUriMatcher = buildMatcher();

    public static UriMatcher buildMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(WeatherDBContract.AUTHORITY, WeatherDBContract.PATH_WEATHERDATA, WEATHER_DATA);
        uriMatcher.addURI(WeatherDBContract.AUTHORITY, WeatherDBContract.PATH_WEATHERDATA+"/#", WEATHER_WITH_ID);
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
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not Supported!");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
