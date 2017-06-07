
package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Main {

    @SerializedName("temp")
    @Expose
    public float temp;
    @SerializedName("temp_min")
    @Expose
    public float tempMin;
    @SerializedName("temp_max")
    @Expose
    public float tempMax;
    @SerializedName("pressure")
    @Expose
    public float pressure;
    @SerializedName("sea_level")
    @Expose
    public float seaLevel;
    @SerializedName("grnd_level")
    @Expose
    public float grndLevel;
    @SerializedName("humidity")
    @Expose
    public long humidity;
    @SerializedName("temp_kf")
    @Expose
    public float tempKf;

}
