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

public class CurrentWeatherLayoutDataModel extends DetailActivityDataModel {

    public String visibility;
    public String sunrise;
    public String sunset;
    public String lastUpdated;
    public String locationAndIcon;

    public CurrentWeatherLayoutDataModel(String date, int weatherImageID, String weatherMain, String weatherDescription,
                                         String tempMain, String unit, String tempMin, String tempMax, String windDescription,
                                         float windDirection, int iconTint, String windValue, String pressureValue,
                                         String humidityValue, String uvIndexValue, String uvIndexDescription,
                                         String rainValue, String cloudinessValue, String visibility, String sunrise,
                                         String sunset, String lastUpdated, String locationAndIcon) {
        super(date, weatherImageID, weatherMain, weatherDescription, tempMain, unit, tempMin, tempMax, windDescription, windDirection, iconTint, windValue, pressureValue, humidityValue, uvIndexValue, uvIndexDescription, rainValue, cloudinessValue);
        this.visibility = visibility;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.lastUpdated = lastUpdated;
        this.locationAndIcon = locationAndIcon;
    }

}
