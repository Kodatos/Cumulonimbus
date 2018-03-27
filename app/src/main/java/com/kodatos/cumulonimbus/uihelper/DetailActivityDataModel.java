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

package com.kodatos.cumulonimbus.uihelper;

public class DetailActivityDataModel {
    public String date;
    public int weatherImageID;
    public String weatherMain;
    public String weatherDescription;
    public String tempMain;
    public String unit;
    public String tempMin;
    public String tempMax;
    public String windDescription;
    public float windDirection;
    public int iconTint;
    public String windValue;
    public String pressureValue;
    public String humidityValue;
    public String uvIndexValue;
    public String uvIndexDescription;
    public String rainValue;
    public String cloudinessValue;

    public DetailActivityDataModel(String date, int weatherImageID, String weatherMain, String weatherDescription, String tempMain, String unit, String tempMin, String tempMax, String windDescription, float windDirection, int iconTint, String windValue, String pressureValue, String humidityValue, String uvIndexValue, String uvIndexDescription, String rainValue, String cloudinessValue) {
        this.date = date;
        this.weatherImageID = weatherImageID;
        this.weatherMain = weatherMain;
        this.weatherDescription = weatherDescription;
        this.tempMain = tempMain;
        this.unit = unit;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.windDescription = windDescription;
        this.windDirection = windDirection;
        this.iconTint = iconTint;
        this.windValue = windValue;
        this.pressureValue = pressureValue;
        this.humidityValue = humidityValue;
        this.uvIndexValue = uvIndexValue;
        this.uvIndexDescription = uvIndexDescription;
        this.rainValue = rainValue;
        this.cloudinessValue = cloudinessValue;
    }
}
