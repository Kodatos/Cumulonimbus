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

package com.kodatos.cumulonimbus.apihelper;


import android.os.Parcelable;

/*
    A model class that contains data for transactions with the database. This class holds all the columns in the database.
    The object will be populated with data from Cursors.
 */
public class DBModel implements Parcelable {

    private long id;
    private String weather_main;
    private String weather_desc;
    private String temp;
    private float temp_min;
    private float temp_max;
    private float pressure;
    private long humidity;
    private String wind;
    private long clouds;
    private String icon_id;
    private double uvIndex;
    private double rain_3h;

    public DBModel() {}

    public DBModel(long id, String weather_main, String weather_desc, String temp, float temp_min, float temp_max, float pressure, long humidity, String wind, long clouds, String icon_id, double uvIndex, double rain_3h) {
        this.id = id;
        this.weather_main = weather_main;
        //Capitalize first word of description
        String[] split = weather_desc.split(" ");
        StringBuilder sb = new StringBuilder();
        for(String s : split){
            sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1)).append(" ");
        }
        weather_desc = sb.toString();
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
        this.rain_3h = rain_3h;
    }

    public static final Creator<DBModel> CREATOR = new Creator<DBModel>() {
        @Override
        public DBModel createFromParcel(android.os.Parcel in) {
            return new DBModel(in);
        }

        @Override
        public DBModel[] newArray(int size) {
            return new DBModel[size];
        }
    };

    protected DBModel(android.os.Parcel in) {
        id = in.readLong();
        weather_main = in.readString();
        weather_desc = in.readString();
        temp = in.readString();
        temp_min = in.readFloat();
        temp_max = in.readFloat();
        pressure = in.readFloat();
        humidity = in.readLong();
        wind = in.readString();
        clouds = in.readLong();
        icon_id = in.readString();
        uvIndex = in.readDouble();
        rain_3h = in.readDouble();
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(weather_main);
        dest.writeString(weather_desc);
        dest.writeString(temp);
        dest.writeFloat(temp_min);
        dest.writeFloat(temp_max);
        dest.writeFloat(pressure);
        dest.writeLong(humidity);
        dest.writeString(wind);
        dest.writeLong(clouds);
        dest.writeString(icon_id);
        dest.writeDouble(uvIndex);
        dest.writeDouble(rain_3h);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
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


    public double getRain_3h() {
        return rain_3h;
    }

    public void setRain_3h(double rain_3h) {
        this.rain_3h = rain_3h;
    }

}
