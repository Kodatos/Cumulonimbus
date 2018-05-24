/*
 * MIT License
 *
 * Copyright (c) 2017 N Abhishek (aka Kodatos)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.kodatos.cumulonimbus.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.apihelper.ServiceErrorContract;
import com.kodatos.cumulonimbus.uihelper.CurrentWeatherLayoutDataModel;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/*
    A utility class containing miscellaneous functions that may be used in other functions.
 */
@SuppressWarnings("WeakerAccess")
public class MiscUtils {

    public static int IMPOSSIBLE_TEMPERATURE = 2000;

    private MiscUtils(){}

    /**
     * Utility method to convert given temperature for display purpose.
     * @param usefulTempinString Temperature in Kelvin to convert
     * @return A Spanned object with temperature and degree symbol in superscript
     */

    public static String makeTemperaturePretty(String usefulTempinString, boolean metric){
        String unit = metric ? "\u2103" : "\u2109";
        return usefulTempinString + unit;
    }

    public static int getHeatIndex(double temperature, long rHumidity, boolean metric){
        if(metric)
            temperature = temperature * 1.8 + 32;           //If metric units, convert to fahrenheit
        double heatIndex = IMPOSSIBLE_TEMPERATURE;                             //Assign impossible heat index for invalid inputs
        if(temperature >= 80){
            heatIndex = -42.739 + (2.0409*temperature) + (10.1433*rHumidity) - (0.2247*temperature*rHumidity)
                    - (0.0068*temperature*temperature) - (0.0548*rHumidity*rHumidity) + (0.0012*temperature*temperature*rHumidity) + (0.0009*temperature*rHumidity*rHumidity)
                    - (0.00000199*temperature*temperature*rHumidity*rHumidity);
            if(rHumidity <= 13 && temperature <= 112){
                heatIndex -= (13 - rHumidity)/4 * Math.sqrt((17 - Math.abs(temperature - 95))/17);
            }
            else if(rHumidity >= 85 && temperature <=87){
                heatIndex += (rHumidity - 85)/10 * (87 - temperature)/5;
            }

            if(heatIndex <= temperature)
                heatIndex = IMPOSSIBLE_TEMPERATURE;                  //The equation may have failed if humidity was too low.
        }
        if(metric && heatIndex!= IMPOSSIBLE_TEMPERATURE)
            heatIndex = (heatIndex - 32)/1.8;               //If metric units, change back to celsius
        return (int) heatIndex;
    }

    public static int getWindChill(double temperature, double windSpeed, boolean metric){
        if(!metric)
            temperature = (temperature - 32)/1.8;
        windSpeed = metric ? 3.6*windSpeed : 1.6*windSpeed;   //Convert m/s or mph to kph
        double windChill = IMPOSSIBLE_TEMPERATURE;                               //Assign impossible wind chill for invalid inputs
        if(temperature <= 10 && windSpeed >= 4.8){
            windChill = 13.12 + (0.6215*temperature) - (11.37*Math.pow(windSpeed, 0.16)) + (0.3965*temperature*Math.pow(windSpeed, 0.16));
            if(windChill >= temperature)
                windChill = IMPOSSIBLE_TEMPERATURE;              //The equation may have failed if wind speed was too low
        }
        if(!metric && windChill!= IMPOSSIBLE_TEMPERATURE)                                     //If not metric, convert back to fahrenheit
            windChill = windChill*1.8 + 32;
        return (int) windChill;
    }

    //Generates a ready to use data binding model for the detail screen from database model
    public static DetailActivityDataModel getDetailModelFromDBModel(Context context, DBModel dbModel,
                                                                    int day, boolean metric) {
        String date = getPatternDate("EEE, d MMM", day);
        String tempMain = dbModel.getTemp();
        String unit = makeTemperaturePretty("", metric);
        String tempMin = String.valueOf(Math.round(dbModel.getTemp_min()));
        String tempMax = String.valueOf(Math.round(dbModel.getTemp_max()));
        String[] windData = dbModel.getWind().split("/");
        double windSpeed = Double.parseDouble(windData[0]);
        String windDescription = windClassifier(windSpeed, metric);
        float windDirection = Float.parseFloat(windData[1]);
        if(metric)
            windSpeed*=3.6;
        String windValue = String.valueOf((int)windSpeed) + (metric ? " km/h" : " mi/h");
        String humidity = String.valueOf(dbModel.getHumidity())+"%";
        String pressure = String.valueOf(dbModel.getPressure()) + " mb";
        String UV = "UV Index: "+String.valueOf(dbModel.getUvIndex());
        String UVRisk = "Risk: "+UVClassifier(dbModel.getUvIndex());
        String rain = "Rain: "+String.valueOf(new DecimalFormat("#.##").format(dbModel.getRain_3h()))+" mm";
        String clouds = "Cloudiness: "+String.valueOf(dbModel.getClouds())+"%";
        int imageId = getResourceIDForIconID(context, dbModel.getIcon_id());
        int iconTint = MiscUtils.getIconTint(context, dbModel.getIcon_id());
        return new DetailActivityDataModel(date, imageId, dbModel.getWeather_main(), dbModel.getWeather_desc(), tempMain, unit, tempMin,
                tempMax, windDescription, windDirection, iconTint, windValue, pressure, humidity, UV, UVRisk, rain, clouds);
    }

    // Generates a ready to use data binding model for main screen from database model and other variables
    public static CurrentWeatherLayoutDataModel getCurrentWeatherDataFromDBModel(Context context, DBModel dbModel, boolean metric, long visibility, String sunrise, String sunset, String lastUpdated, String locationAndIcon) {
        String date = getPatternDate("dd MMMM, YYYY", 0);
        String tempMain = makeTemperaturePretty(dbModel.getTemp(), metric);
        String tempMin = String.valueOf(Math.round(dbModel.getTemp_min()));
        String tempMax = String.valueOf(Math.round(dbModel.getTemp_max()));
        String[] windData = dbModel.getWind().split("/");
        double windSpeed = Double.parseDouble(windData[0]);
        String windDescription = windClassifier(windSpeed, metric);
        if(metric)
            windSpeed*=3.6;         //Convert to km/h for display
        float windDirection = Float.parseFloat(windData[1]);
        String windValue = String.valueOf((int)windSpeed) + (metric ? " km/h" : " mi/h");
        String humidity = String.valueOf(dbModel.getHumidity())+"%";
        String pressure = String.valueOf((int) dbModel.getPressure()) + " mb";
        String UVIndexValue = String.valueOf(dbModel.getUvIndex());
        String UVWithRisk = "UV Index: " + UVClassifier(dbModel.getUvIndex());
        String rain = dbModel.getRain_3h() == -1 ? "" : String.valueOf(new DecimalFormat("#.##").format(dbModel.getRain_3h())) + " mm";
        String clouds = String.valueOf(dbModel.getClouds())+"%";
        double calculatedVisibility = (double) (visibility / 1000) * (metric ? 1 : 0.621);
        String visibilityInKM = String.valueOf(new DecimalFormat("#.#").format(calculatedVisibility)) + (metric ? " km" : " mi");
        int imageId = getResourceIDForIconID(context, dbModel.getIcon_id());
        int iconTint = ColorUtils.setAlphaComponent(getBackgroundColorForIconID(context, dbModel.getIcon_id()), 175);
        return new CurrentWeatherLayoutDataModel(date, imageId, dbModel.getWeather_main(), dbModel.getWeather_desc(), tempMain,
                null, tempMin, tempMax, windDescription, windDirection, iconTint, windValue, pressure, humidity,
                UVIndexValue, UVWithRisk, rain, clouds, visibilityInKM, sunrise, sunset, lastUpdated, locationAndIcon);
    }

    public static String getPatternDate(String pattern, int dayOffset){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, dayOffset);
        SimpleDateFormat sf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sf.format(calendar.getTime());
    }

    //Common method to get an intent for error broadcast between service and activity. Details are provided by caller
    public static Intent getServiceErrorBroadcastIntent(String type, String details){
        Intent errorIntent = new Intent(ServiceErrorContract.BROADCAST_INTENT_FILTER);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_TYPE, type);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_DETAILS, details);
        return errorIntent;
    }

    /**
     *
     * @param speed Speed to be classified
     * @param metric True if speed is in m/s. False for mi/h
     * @return Category defining wind speed
     */
    public static String windClassifier(double speed, boolean metric){
        double speedInKPH = metric ? 3.6*speed : 1.60*speed;
        if(speedInKPH<1)
            return "Calm ";
        else if(speedInKPH<6)
            return "Light Air ";
        else if (speedInKPH<12)
            return "Light Breeze ";
        else if(speedInKPH<20)
            return "Gentle Breeze ";
        else if(speedInKPH<29)
            return "Moderate Breeze ";
        else if(speedInKPH<39)
            return "Fresh Breeze ";
        else if(speedInKPH<50)
            return "Strong Breeze ";
        else if(speedInKPH<62)
            return "Moderate Gale ";
        else if(speedInKPH<75)
            return "Fresh Gale ";
        else if(speedInKPH<89)
            return "Strong Gale ";
        else if(speedInKPH<103)
            return "Whole Gale ";
        else if(speedInKPH<118)
            return "Storm ";
        else if(speedInKPH>=118)
            return "Hurricane ";
        return "N/A ";
    }

    public static String UVClassifier(double index){
        if(index<3)
            return "Low";
        else if(index<6)
            return "Moderate";
        else if(index<8)
            return "High";
        else if(index<11)
            return "Very High";
        else if(index>=11)
            return "Extreme";
        return "";
    }

    /**
     * Utility method to generate appropriate last updated message based on time passed since last update
     * @param currentMillis Time at which message is required, in milliseconds
     * @param lastUpdatedMillis Time of last update, in milliseconds
     * @return The required String message
     */
    public static String getLastUpdatedStringFromMillis(long currentMillis, long lastUpdatedMillis){
        long second = 1000;
        long minute = second*60;
        long hour = minute*60;
        long day = hour*24;
        long difference = currentMillis - lastUpdatedMillis;
        String updated = "Updated ";
        String time;
        if(difference<=10*second)
            time = "moments ago";
        else if(difference<=minute)
            time = String.valueOf(difference / second) + " seconds ago";
        else if(difference<=hour)
            time = String.valueOf(difference / minute) + " minutes ago";
        else if(difference<=day)
            time = String.valueOf(difference / hour) + " hours ago";
        else if(difference<=10*day)
            time = String.valueOf(difference / day) + " day(s) ago";
        else
            time = "long ago";
        return updated + time;
    }

    public static int getResourceIDForIconID(Context context, String iconID) {
        if ("03n".equals(iconID))
            iconID = "03d";
        else if ("04n".equals(iconID))
            iconID = "04d";
        else if ("09n".equals(iconID))
            iconID = "09d";
        else if ("11n".equals(iconID))
            iconID = "11d";
        else if ("13n".equals(iconID))
            iconID = "13d";
        else if ("50n".equals(iconID))
            iconID = "50d";
        return context.getResources().getIdentifier("ic_" + iconID, "drawable", context.getPackageName());
    }

    public static int getIconTint(Context context, String iconID) {
        int colorRID = R.color.colorAccent;
        if ("01d".equals(iconID))
            colorRID = R.color._01d_icon_tint;
        else if ("01n".equals(iconID))
            colorRID = R.color._01n_icon_tint;
        else if (iconID.contains("02"))
            colorRID = R.color._02d_icon_tint;
        else if (iconID.contains("03"))
            colorRID = R.color._03d_icon_tint;
        else if (iconID.contains("04"))
            colorRID = R.color._04d_icon_tint;
        else if (iconID.contains("09") || iconID.contains("10"))
            colorRID = R.color._09d_icon_tint;
        else if (iconID.contains("11"))
            colorRID = R.color._11d_icon_tint;
        else if (iconID.contains("13"))
            colorRID = R.color._13d_icon_tint;
        else if (iconID.contains("50"))
            colorRID = R.color._50d_icon_tint;
        return ContextCompat.getColor(context, colorRID);
    }

    public static int getBackgroundColorForIconID(Context context, String iconID) {
        int colorRID = R.color.colorPrimary;
        if ("01d".equals(iconID))
            colorRID = R.color._01d_background;
        else if ("01n".equals(iconID))
            colorRID = R.color._01n_background;
        else if (iconID.contains("02"))
            colorRID = R.color._02d_background;
        else if (iconID.contains("03"))
            colorRID = R.color._03d_background;
        else if (iconID.contains("04"))
            colorRID = R.color._04d_background;
        else if (iconID.contains("09") || iconID.contains("10"))
            colorRID = R.color._09d_background;
        else if (iconID.contains("11"))
            colorRID = R.color._11d_background;
        else if (iconID.contains("13"))
            colorRID = R.color._13d_background;
        else if (iconID.contains("50"))
            colorRID = R.color._50d_background;
        return ContextCompat.getColor(context, colorRID);
    }
}
