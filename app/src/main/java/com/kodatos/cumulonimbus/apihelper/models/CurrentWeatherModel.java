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

import java.util.List;

public class CurrentWeatherModel {
    @SerializedName("coord")
    @Expose
    public Coord coord;
    @SerializedName("weather")
    @Expose
    public List<Weather> weather = null;
    @SerializedName("base")
    @Expose
    public String base;
    @SerializedName("main")
    @Expose
    public MainCurrent mainCurrent;
    @SerializedName("visibility")
    @Expose
    public long visibility;
    @SerializedName("wind")
    @Expose
    public Wind wind;
    @SerializedName("clouds")
    @Expose
    public Clouds clouds;
    @SerializedName("dt")
    @Expose
    public long dt;
    @SerializedName("sys")
    @Expose
    public SysCurrent sysCurrent;
    @SerializedName("id")
    @Expose
    public long id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("cod")
    @Expose
    public long cod;

    public CurrentWeatherModel() {
    }

    /**
     *
     * @return A ContentValues object representing model data for database transactions
     */
    public ContentValues getEquivalentCV(){
        ContentValues cv = new ContentValues();
        Weather w = weather.get(0);
        cv.put(WeatherDBContract.WeatherDBEntry._ID, 1);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN,w.main);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC,w.description);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP,String.valueOf(Math.round(mainCurrent.temp)));
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN,mainCurrent.tempMin);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX,mainCurrent.tempMax);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE,mainCurrent.pressure);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY,mainCurrent.humidity);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_WIND,wind.getUsefulWind());
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS, clouds.all);
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID, w.icon);
        // OpenWeatherMap doesn't provide last 3 hr rain data for current weather most of the times. It is updated with available
        // forecast rain data
        cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H, -1);
        return cv;
    }
}
