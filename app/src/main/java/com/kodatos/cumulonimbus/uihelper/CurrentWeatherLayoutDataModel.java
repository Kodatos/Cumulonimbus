package com.kodatos.cumulonimbus.uihelper;

public class CurrentWeatherLayoutDataModel extends DetailActivityDataModel {

    public String visibility;
    public String sunrise;
    public String sunset;
    public String lastUpdated;

    public CurrentWeatherLayoutDataModel(String date, int weatherImageID, String weatherMain, String weatherDescription, String tempMain, String unit, String tempMin, String tempMax, String windDescription, float windDirection, int iconTint, String windValue, String pressureValue, String humidityValue, String uvIndexValue, String uvIndexDescription, String rainValue, String cloudinessValue, String visibility, String sunrise, String sunset, String lastUpdated) {
        super(date, weatherImageID, weatherMain, weatherDescription, tempMain, unit, tempMin, tempMax, windDescription, windDirection, iconTint, windValue, pressureValue, humidityValue, uvIndexValue, uvIndexDescription, rainValue, cloudinessValue);
        this.visibility = visibility;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.lastUpdated = lastUpdated;
    }

}
