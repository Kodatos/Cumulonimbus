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


import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherDBContract {

    //Static strings for uri building
    public static final String AUTHORITY = "com.kodatos.cumulonimbus";
    static final Uri BASE_URI = Uri.parse("content://"+AUTHORITY);
    public static final String PATH_WEATHER_DATA = "weatherdata";

    public static final class WeatherDBEntry implements BaseColumns{

        // A Uri that can be used in ContentResolver calls
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_WEATHER_DATA).build();

        public static final String TABLE_NAME = "weather_table";
        public static final String COLUMN_WEATHER_MAIN = "weather_main";
        public static final String COLUMN_WEATHER_DESC = "weather_desc";
        public static final String COLUMN_TEMP = "temperatures";
        public static final String COLUMN_TEMP_MIN = "temp_min";
        public static final String COLUMN_TEMP_MAX = "temp_max";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND = "wind";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_CLOUDS = "clouds";
        public static final String COLUMN_ICON_ID = "icon_ids";
        public static final String COLUMN_UV_INDEX = "uv_index";
        public static final String COLUMN_RAIN_3H = "rain_3h";

    }
}
