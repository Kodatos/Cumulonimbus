package com.kodatos.cumulonimbus.uihelper;

import android.text.Spanned;

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
        this.calculatedDay = calculatedDay;
    }
}
