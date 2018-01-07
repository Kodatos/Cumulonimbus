package com.kodatos.cumulonimbus.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.uihelper.CurrentWeatherLayoutDataModel;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/*
    A utility class containing miscellaneous functions that may be used in other functions.
 */
@SuppressWarnings("WeakerAccess")
public class MiscUtils {

    /**
     * Utility method to convert given temperature for display purpose.
     * @param usefulTempinString Temperature in Kelvin to convert
     * @return A Spanned object with temperature and degree symbol in superscript
     */

    public static String makeTemperaturePretty(String usefulTempinString, boolean metric){
        String unit = metric ? "\u2103" : "\u2109";
        return usefulTempinString + unit;
    }

    /**
     * Utility method to get user friendly address from co-ordinates
     *
     * @param lat     Latitude of location
     * @param lon     Longitude of location
     * @param context Requesting context
     * @return String containing locality and country
     */
    public static String getAddressFromLatLong(double lat, double lon, Context context) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
        return addresses.get(0).getSubLocality() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
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
        String windDescription = windClassifier(Double.parseDouble(windData[0]));
        float windDirection = Float.parseFloat(windData[1]);
        String windValue = windData[0] + (metric ? " m/s" : " mi/h");
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
        String windDescription = windClassifier(Double.parseDouble(windData[0]));
        float windDirection = Float.parseFloat(windData[1]);
        String windValue = windData[0] + (metric ? " m/s" : " mi/h");
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

    public static String windClassifier(double speed){
        double speedInKPH = (18.0/5.0)*speed;
        if(speedInKPH<2)
            return "Calm";
        else if(isBetween(speedInKPH,2.0,6.99))
            return "Light Air";
        else if (isBetween(speedInKPH,7.0,11.99))
            return "Light Breeze";
        else if(isBetween(speedInKPH,12.0,19.99))
            return "Gentle Breeze";
        else if(isBetween(speedInKPH,20.0,30.99))
            return "Moderate Breeze";
        else if(isBetween(speedInKPH,31.0,39.99))
            return "Fresh Breeze";
        else if(isBetween(speedInKPH,40.0,50.99))
            return "Strong Breeze";
        else if(isBetween(speedInKPH,51.0,61.99))
            return "Moderate Gale";
        else if(isBetween(speedInKPH,62.0,74.99))
            return "Fresh Gale";
        else if(isBetween(speedInKPH,75.0,87.99))
            return "Strong Gale";
        else if(isBetween(speedInKPH,88.0,102.99))
            return "Whole Gale";
        else if(isBetween(speedInKPH,103.0,117.99))
            return "Storm";
        else if(speedInKPH>=118.0)
            return "Hurricane";
        return "N/A";
    }

    public static String UVClassifier(double index){
        if(isBetween(index,0.0,2.99))
            return "Low";
        else if(isBetween(index,3.0,5.99))
            return "Moderate";
        else if(isBetween(index,6.0,7.99))
            return "High";
        else if(isBetween(index,8.0,12.99))
            return "Very High";
        else if(index>=13.0)
            return "Extreme";
        return "";
    }

    public static boolean isBetween(double number, double lower, double upper){
        return number>=lower && number<=upper;
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
