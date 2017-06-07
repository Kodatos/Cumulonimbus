package com.kodatos.cumulonimbus.apihelper.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kodatos.cumulonimbus.apihelper.DBModel;

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

    // A helper method to get required data for database operations
    public DBModel getDBModel(){
        // First row in database
        Weather w = weather.get(0);
        // Weather contains main and desc while MainCurrent contains the rest except wind
        return new DBModel(1,w.main,w.description,mainCurrent.temp,mainCurrent.tempMin,mainCurrent.tempMax,mainCurrent.pressure,mainCurrent.humidity,wind.getUsefulWind());
    }
}
