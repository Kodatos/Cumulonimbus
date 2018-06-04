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

import java.util.Objects;

/*
    A model class that contains calculated data from code for binding in the layout so that the original DBModel object is not touched
 */
public class DBModelCalculatedData {

    private int drawable_resource_id;
    private String calculatedTemp;
    private String calculatedDay;
    private String weatherMain;
    private String weatherDesc;

    public int getDrawable_resource_id() {
        return drawable_resource_id;
    }

    public String getCalculatedDay() {
        return calculatedDay;
    }

    public String getCalculatedTemp() {
        return calculatedTemp;
    }


    public String getWeatherMain() {
        return weatherMain;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    public DBModelCalculatedData(int drawable_resource_id, String calculatedTemp, String calculatedDay, String weatherMain, String weatherDesc) {
        this.drawable_resource_id = drawable_resource_id;
        this.calculatedTemp = calculatedTemp;
        this.calculatedDay = calculatedDay.toUpperCase();
        this.weatherMain = weatherMain;
        this.weatherDesc = weatherDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBModelCalculatedData that = (DBModelCalculatedData) o;
        return drawable_resource_id == that.drawable_resource_id &&
                Objects.equals(calculatedTemp, that.calculatedTemp) &&
                Objects.equals(calculatedDay, that.calculatedDay) &&
                Objects.equals(weatherMain, that.weatherMain) &&
                Objects.equals(weatherDesc, that.weatherDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(drawable_resource_id, calculatedTemp, calculatedDay, weatherMain, weatherDesc);
    }
}
