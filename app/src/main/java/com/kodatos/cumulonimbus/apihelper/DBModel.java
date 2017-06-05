package com.kodatos.cumulonimbus.apihelper;


import android.content.ContentValues;
import static com.kodatos.cumulonimbus.datahelper.WeatherDBContract.*;

public class DBModel {

    private long id;
    private String weather_main;
    private String weather_desc;
    private float temp;
    private float temp_min;
    private float temp_max;
    private float pressure;
    private long humidity;
    private String wind;

    public DBModel(long id, String weather_main, String weather_desc, float temp, float temp_min, float temp_max, float pressure, long humidity, String wind) {
        this.id = id;
        this.weather_main = weather_main;
        this.weather_desc = weather_desc;
        this.temp = temp;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.pressure = pressure;
        this.humidity = humidity;
        this.wind = wind;
    }

    // This function builds and provides a ContentValues object required for the database's update and insert operations
    public ContentValues getEquivalentCV(){
        ContentValues cv = new ContentValues();
        cv.put(WeatherDBEntry._ID,this.id);
        cv.put(WeatherDBEntry.COLUMN_WEATHER_MAIN,this.weather_main);
        cv.put(WeatherDBEntry.COLUMN_WEATHER_DESC,this.weather_desc);
        cv.put(WeatherDBEntry.COLUMN_TEMP,this.temp);
        cv.put(WeatherDBEntry.COLUMN_TEMP_MIN,this.temp_min);
        cv.put(WeatherDBEntry.COLUMN_TEMP_MAX,this.temp_max);
        cv.put(WeatherDBEntry.COLUMN_PRESSURE,this.pressure);
        cv.put(WeatherDBEntry.COLUMN_HUMIDITY,this.humidity);
        cv.put(WeatherDBEntry.COLUMN_WIND,this.wind);
        return cv;
    }
}
