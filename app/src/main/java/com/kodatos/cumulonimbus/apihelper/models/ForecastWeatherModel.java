
package com.kodatos.cumulonimbus.apihelper.models;

import android.content.ContentValues;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;

public class ForecastWeatherModel {

    @SerializedName("cod")
    @Expose
    public String cod;
    @SerializedName("message")
    @Expose
    public float message;
    @SerializedName("cnt")
    @Expose
    public long cnt;
    @SerializedName("list")
    @Expose
    public java.util.List<ForecastList> forecastList = null;
    @SerializedName("city")
    @Expose
    public City city;

    public ForecastWeatherModel() {
    }

    /**
     * @param day The day for which weather is accessed
     * @return A ContentValues object representing model data for database transactions
     */
    public ContentValues getEquivalentCV(long day){
        ContentValues cv = new ContentValues();
        ForecastList fl = forecastList.get((int) day);
        Weather w = fl.weather.get(0);
        Main m = fl.main;
        float netRain3h=0,i=0;
        for(ForecastList flist : forecastList){
            if(i<8){
                i++;
            }
            else {
                if (flist.rain != null)
                    netRain3h += flist.rain._3h;
            }
        }
        cv.put(WeatherDBContract.WeatherDBEntry._ID, (day/8)+1);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN,w.main);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC,w.description);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP,m.temp);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN,m.tempMin);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX,m.tempMax);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE,m.pressure);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY,m.humidity);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WIND,fl.wind.getUsefulWind());
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS, fl.clouds.all);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID, w.icon);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H, netRain3h);
        return cv;
    }
}
