package com.kodatos.cumulonimbus.datahelper;


import android.provider.BaseColumns;

public class WeatherDBContract {

    public static final class WeatherDBEntry implements BaseColumns{
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
