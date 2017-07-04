
package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kodatos.cumulonimbus.apihelper.DBModel;

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

    // A helper method to get required data for database operations
    public DBModel getDBModel(long day){
        ForecastList fl = forecastList.get((int) (day*8));
        Weather w = fl.weather.get(0);
        Main m = fl.main;
        return new DBModel(day+1,w.main,w.description,m.temp,m.tempMin,
                m.tempMax,m.pressure,m.humidity,fl.wind.getUsefulWind(),fl.clouds.all);

    }
}
