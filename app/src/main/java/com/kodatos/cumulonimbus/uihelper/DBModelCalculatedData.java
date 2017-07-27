package com.kodatos.cumulonimbus.uihelper;

import android.text.Spanned;

/*
    A model class that contains calculated data from code for binding in the layout so that the original DBModel object is not touched
 */
public class DBModelCalculatedData {

    private int drawable_resource_id;
    private Spanned calculatedTemp;
    private String calculatedDay;

    public int getDrawable_resource_id() {
        return drawable_resource_id;
    }

    public String getCalculatedDay() {
        return calculatedDay;
    }

    public Spanned getCalculatedTemp() {
        return calculatedTemp;
    }

    public DBModelCalculatedData(int drawable_resource_id, Spanned calculatedTemp, String calculatedDay) {
        this.drawable_resource_id = drawable_resource_id;
        this.calculatedTemp = calculatedTemp;
        this.calculatedDay = calculatedDay.toUpperCase();
    }
}
