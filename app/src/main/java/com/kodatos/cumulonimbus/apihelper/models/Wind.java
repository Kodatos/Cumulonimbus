
package com.kodatos.cumulonimbus.apihelper.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Wind {

    @SerializedName("speed")
    @Expose
    private float speed;
    @SerializedName("deg")
    @Expose
    private float deg;

    // A helper method to get a String for database usage
    public String getUsefulWind(){
        return String.valueOf(speed)+"/"+String.valueOf(deg);
    }

}
