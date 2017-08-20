package com.kodatos.cumulonimbus.apihelper;


import android.content.ContentValues;
import android.text.Spanned;

import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import static com.kodatos.cumulonimbus.datahelper.WeatherDBContract.*;

/*
    A model class that contains data for transactions with the database. This class holds all the columns in the database.
    The object will be populated with data from Cursors.
 */
@Parcel(Parcel.Serialization.BEAN)
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

    public DBModel() {}

    @ParcelConstructor
    public DBModel(long id, String weather_main, String weather_desc, float temp, float temp_min, float temp_max, float pressure, long humidity, String wind, long clouds, String icon_id, double uvIndex) {
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
        this.uvIndex = uvIndex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWeather_main() {
        return weather_main;
    }

    public void setWeather_main(String weather_main) {
        this.weather_main = weather_main;
    }

    public String getWeather_desc() {
        return weather_desc;
    }

    public void setWeather_desc(String weather_desc) {
        this.weather_desc = weather_desc;
    }

    public float getTemp() {
        return temp;
    }

    public void setTemp(float temp) {
        this.temp = temp;
    }

    public float getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(float temp_min) {
        this.temp_min = temp_min;
    }

    public float getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(float temp_max) {
        this.temp_max = temp_max;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public long getHumidity() {
        return humidity;
    }

    public void setHumidity(long humidity) {
        this.humidity = humidity;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public long getClouds() {
        return clouds;
    }

    public void setClouds(long clouds) {
        this.clouds = clouds;
    }

    public String getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(String icon_id) {
        this.icon_id = icon_id;
    }

    public double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

}
