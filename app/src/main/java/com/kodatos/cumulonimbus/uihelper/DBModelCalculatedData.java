package com.kodatos.cumulonimbus.uihelper;

import android.text.Spanned;

/*
    A model class that contains calculated data from code for binding in the layout so that the original DBModel object is not touched
 */
public class DBModelCalculatedData {

    private int drawable_resource_id;
    private String calculatedTemp;
    private String calculatedDay;
    private String weatherMain;
    private String weatherDesc;

    public int getDrawable_resource_id() {
        return drawable_resource_id;
    }

    public String getCalculatedDay() {
        return calculatedDay;
    }

    public String getCalculatedTemp() {
        return calculatedTemp;
    }


    public String getWeatherMain() {
        return weatherMain;
    }

    public String getWeatherDesc() {
        return weatherDesc;
    }

    public DBModelCalculatedData(int drawable_resource_id, String calculatedTemp, String calculatedDay,String weatherMain, String weatherDesc) {
        this.drawable_resource_id = drawable_resource_id;
        this.calculatedTemp = calculatedTemp;
        this.calculatedDay = calculatedDay.toUpperCase();
        this.weatherMain = weatherMain;
        this.weatherDesc = weatherDesc;
    }
}
