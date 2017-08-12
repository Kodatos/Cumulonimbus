package com.kodatos.cumulonimbus.apihelper;


import android.content.ContentValues;
import android.text.Spanned;

import com.kodatos.cumulonimbus.utils.MiscUtils;

import static com.kodatos.cumulonimbus.datahelper.WeatherDBContract.*;

/*
    A model class that contains data for transactions with the database. This class holds all the columns in the database.
    The object will be populated with data from Cursors.
 */
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
    private long clouds;
    private String icon_id;
    private double uvIndex;

    public String getIcon_id() {
        return icon_id;
    }

    public long getClouds() {
        return clouds;
    }

    public long getId() {
        return id;
    }

    public String getWeather_main() {
        return weather_main;
    }

    public String getWeather_desc() {
        return weather_desc;
    }

    public float getTemp() {
        return temp;
    }

    public float getTemp_min() {
        return temp_min;
    }

    public float getTemp_max() {
        return temp_max;
    }

    public float getPressure() {
        return pressure;
    }

    public long getHumidity() {
        return humidity;
    }

    public String getWind() {
        return wind;
    }

    public DBModel(long id, String weather_main, String weather_desc, float temp, float temp_min, float temp_max, float pressure, long humidity, String wind, long clouds, String icon_id) {
        this.id = id;
        this.weather_main = weather_main;
        //Capitalize first word of description
        weather_desc = weather_desc.substring(0,1).toUpperCase()+weather_desc.substring(1);
        this.weather_desc = weather_desc;
        this.temp = temp;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.pressure = pressure;
        this.humidity = humidity;
        this.wind = wind;
        this.clouds = clouds;
        this.icon_id = icon_id;
    }

    /**
     *
     * @return A ContentValues object representing model data for database transactions
     */
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
        cv.put(WeatherDBEntry.COLUMN_CLOUDS, this.clouds);
        cv.put(WeatherDBEntry.COLUMN_ICON_ID, this.icon_id);
        return cv;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public DBModel setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
        return this;
    }
}
