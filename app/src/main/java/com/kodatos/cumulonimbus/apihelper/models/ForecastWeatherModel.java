
package com.kodatos.cumulonimbus.apihelper.models;

import android.content.ContentValues;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
    public ContentValues getEquivalentCV(int day){
        ContentValues cv = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (day));
        String required = sdf.format(calendar.getTime());
        float netRain3h=0;
        StringBuilder temperatureList = new StringBuilder();
        StringBuilder iconList = new StringBuilder();
        int i=0;
        if(day==0){
            while(forecastList.get(i).dtTxt.contains(required)){
                if (forecastList.get(i).rain != null)
                    netRain3h += forecastList.get(i).rain._3h;
                i++;
            }
            cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H, netRain3h);
            return cv;
        }
        ForecastList fl = forecastList.get((day*8)-1);
        Weather w = fl.weather.get(0);
        Main m = fl.main;
        for(ForecastList flist : forecastList){
            if(flist.dtTxt.contains(required)){
                if (flist.rain != null)
                    netRain3h += flist.rain._3h;
                temperatureList.append(Math.round(flist.main.temp)).append("/");
                iconList.append(flist.weather.get(0).icon).append("/");
                if(i==7)
                    break;
                i++;
            }
        }
        cv.put(WeatherDBContract.WeatherDBEntry._ID, (long)(day+1));
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN,w.main);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC,w.description);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP,temperatureList.toString());
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN,m.tempMin);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX,m.tempMax);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE,m.pressure);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY,m.humidity);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WIND,fl.wind.getUsefulWind());
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS, fl.clouds.all);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID, iconList.toString());
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H, netRain3h);
        return cv;
    }
}
