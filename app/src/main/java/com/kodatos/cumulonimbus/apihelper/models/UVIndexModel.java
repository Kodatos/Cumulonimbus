package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UVIndexModel {

    @SerializedName("lat")
    @Expose
    public double lat;
    @SerializedName("lon")
    @Expose
    public double lon;
    @SerializedName("date_iso")
    @Expose
    public String dateIso;
    @SerializedName("date")
    @Expose
    public int date;
    @SerializedName("value")
    @Expose
    public double value;

}
