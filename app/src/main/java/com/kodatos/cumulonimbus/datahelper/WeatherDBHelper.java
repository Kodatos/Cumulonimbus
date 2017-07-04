package com.kodatos.cumulonimbus.datahelper;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kodatos.cumulonimbus.datahelper.WeatherDBContract.*;

public class WeatherDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather_db.db";
    private static final int DATABASE_VERSION = 1;

    //Static string for SQL create statement
    private static final String CREATE_WEATHER_TABLE = "CREATE TABLE "+WeatherDBEntry.TABLE_NAME+" (" +
            WeatherDBEntry._ID+" INTEGER PRIMARY KEY, "+WeatherDBEntry.COLUMN_WEATHER_MAIN+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_WEATHER_DESC+" TEXT NOT NULL, "+WeatherDBEntry.COLUMN_TEMP+" REAL NOT NULL, "+
            WeatherDBEntry.COLUMN_TEMP_MIN+" REAL NOT NULL, "+WeatherDBEntry.COLUMN_TEMP_MAX+" REAL NOT NULL, "+
            WeatherDBEntry.COLUMN_PRESSURE+" REAL NOT NULL, "+WeatherDBEntry.COLUMN_WIND+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_HUMIDITY+" INTEGER NOT NULL, "+
            WeatherDBEntry.COLUMN_CLOUDS+" INTEGER NOT NULL);";
    public WeatherDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+WeatherDBEntry.TABLE_NAME+";");
        onCreate(db);
    }
}
