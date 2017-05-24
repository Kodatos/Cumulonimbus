
package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coord {

    @SerializedName("lat")
    @Expose
    public double lat;
    @SerializedName("lon")
    @Expose
    public double lon;

}
