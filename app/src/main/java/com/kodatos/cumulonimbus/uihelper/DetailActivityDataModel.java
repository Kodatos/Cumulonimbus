package com.kodatos.cumulonimbus.uihelper;

import android.text.Spanned;

public class DetailActivityDataModel {
    public String date;
    public int weatherImageID;
    public String weatherMain;
    public String weatherDescription;
    public String tempMain;
    public String unit;
    public String tempMin;
    public String tempMax;
    public String windDescription;
    public float windDirection;
    public int iconTint;
    public String windValue;
    public String pressureValue;
    public String humidityValue;
    public String uvIndexValue;
    public String uvIndexDescription;
    public String rainValue;
    public String cloudinessValue;

    public DetailActivityDataModel(String date, int weatherImageID, String weatherMain, String weatherDescription, String tempMain, String unit, String tempMin, String tempMax, String windDescription, float windDirection, int iconTint, String windValue, String pressureValue, String humidityValue, String uvIndexValue, String uvIndexDescription, String rainValue, String cloudinessValue) {
        this.date = date;
        this.weatherImageID = weatherImageID;
        this.weatherMain = weatherMain;
        this.weatherDescription = weatherDescription;
        this.tempMain = tempMain;
        this.unit = unit;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.windDescription = windDescription;
        this.windDirection = windDirection;
        this.iconTint = iconTint;
        this.windValue = windValue;
        this.pressureValue = pressureValue;
        this.humidityValue = humidityValue;
        this.uvIndexValue = uvIndexValue;
        this.uvIndexDescription = uvIndexDescription;
        this.rainValue = rainValue;
        this.cloudinessValue = cloudinessValue;
    }
}
