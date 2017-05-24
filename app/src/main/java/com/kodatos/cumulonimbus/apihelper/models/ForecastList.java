
package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ForecastList {

    @SerializedName("dt")
    @Expose
    public long dt;
    @SerializedName("main")
    @Expose
    public Main main;
    @SerializedName("weather")
    @Expose
    public List<Weather> weather = null;
    @SerializedName("clouds")
    @Expose
    public Clouds clouds;
    @SerializedName("wind")
    @Expose
    public Wind wind;
    @SerializedName("sys")
    @Expose
    public Sys sys;
    @SerializedName("dt_txt")
    @Expose
    public String dtTxt;
    @SerializedName("rain")
    @Expose
    public Rain rain;
    @SerializedName("snow")
    @Expose
    public Snow snow;

}
