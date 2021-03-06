
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

package com.kodatos.cumulonimbus.apihelper.models;

import android.content.ContentValues;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;

import java.text.SimpleDateFormat;
import java.util.Date;
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
     * Provides a contentValue to be used in database. A position of value 0 is assigned to current weather, and
     * the corresponding contentValues object includes only the rain volume to be updated. However, current weather is
     * not included in the forecast list, hence 1 is subtracted from the position for the rest.
     * @param position The position for which weather is accessed
     * @return A ContentValues object representing model data for database transactions
     */
    public ContentValues getEquivalentCV(int position) {
        ContentValues cv = new ContentValues();
        float netRain3h=0;

        /*
            This block provides the rain volume information for current weather, as the
            current weather API response almost never includes the rain parameter
         */
        if (position == 0) {
            int i = 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String required = sdf.format(new Date());
            while(forecastList.get(i).dtTxt.contains(required)){
                if (forecastList.get(i).rain != null)
                    netRain3h += forecastList.get(i).rain._3h;
                i++;
            }
            cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H, netRain3h);
            return cv;
        }


        ForecastList fl = forecastList.get(position - 1);
        Weather w = fl.weather.get(0);
        Main m = fl.main;

        if (fl.rain != null)
            netRain3h = fl.rain._3h;

        cv.put(WeatherDBContract.WeatherDBEntry._ID, (long) (position + 1));       //ID starts from 1 including current weather
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN,w.main);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC,w.description);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP, String.valueOf(Math.round(m.temp)));
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
