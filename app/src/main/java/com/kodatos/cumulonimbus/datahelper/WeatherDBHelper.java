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


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kodatos.cumulonimbus.datahelper.WeatherDBContract.WeatherDBEntry;

public class WeatherDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "weather_db.db";
    private static final int DATABASE_VERSION = 1;

    //Static string for SQL create statement
    private static final String CREATE_WEATHER_TABLE = "CREATE TABLE "+WeatherDBEntry.TABLE_NAME+" (" +
            WeatherDBEntry._ID+" INTEGER PRIMARY KEY, "+WeatherDBEntry.COLUMN_WEATHER_MAIN+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_WEATHER_DESC+" TEXT NOT NULL, "+WeatherDBEntry.COLUMN_TEMP+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_TEMP_MIN+" REAL NOT NULL, "+WeatherDBEntry.COLUMN_TEMP_MAX+" REAL NOT NULL, "+
            WeatherDBEntry.COLUMN_PRESSURE+" REAL NOT NULL, "+WeatherDBEntry.COLUMN_WIND+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_HUMIDITY+" INTEGER NOT NULL, "+
            WeatherDBEntry.COLUMN_CLOUDS+" INTEGER NOT NULL, "+
            WeatherDBEntry.COLUMN_ICON_ID+" TEXT NOT NULL, "+
            WeatherDBEntry.COLUMN_UV_INDEX+" REAL NOT NULL, "+
            WeatherDBEntry.COLUMN_RAIN_3H+" REAL NOT NULL);";

    WeatherDBHelper(Context context) {
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
