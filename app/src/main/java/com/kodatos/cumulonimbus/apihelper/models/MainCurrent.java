package com.kodatos.cumulonimbus.apihelper.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainCurrent {
    @SerializedName("temp")
    @Expose
    public float temp;
    @SerializedName("pressure")
    @Expose
    public float pressure;
    @SerializedName("humidity")
    @Expose
    public long humidity;
    @SerializedName("temp_min")
    @Expose
    public float tempMin;
    @SerializedName("temp_max")
    @Expose
    public float tempMax;
}
