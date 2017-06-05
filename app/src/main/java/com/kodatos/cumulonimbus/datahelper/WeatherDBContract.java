package com.kodatos.cumulonimbus.datahelper;


import android.net.Uri;
import android.provider.BaseColumns;

public class WeatherDBContract {

    //Static strings for uri building
    public static final String AUTHORITY = "com.kodatos.cumulonimbus";
    public static final Uri BASE_URI = Uri.parse("content://"+AUTHORITY);
    public static final String PATH_WEATHERDATA = "weatherdata";

    public static final class WeatherDBEntry implements BaseColumns{

        // A Uri that can be used in ContentResolver calls
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_WEATHERDATA).build();

        public static final String TABLE_NAME = "weather_table";
        public static final String COLUMN_WEATHER_MAIN = "weather_main";
        public static final String COLUMN_WEATHER_DESC = "weather_desc";
        public static final String COLUMN_TEMP = "temp";
        public static final String COLUMN_TEMP_MIN = "temp_min";
        public static final String COLUMN_TEMP_MAX = "temp_max";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND = "wind";
        public static final String COLUMN_HUMIDITY = "humidity";

    }
}
