package com.kodatos.cumulonimbus.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import com.kodatos.cumulonimbus.apihelper.DBModel;
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
public class MiscUtils {

    /**
     * Converts given temperature for display purpose.
     * @param usefulTemp Temperature in Kelvin to convert
     * @return A Spanned object with temperature and degree symbol in superscript
     */
    @SuppressWarnings("deprecation")
    public static String makeTemperaturePretty(float usefulTemp, boolean metric){
        String usefulTempinString;
        if(usefulTemp==-1)
            usefulTempinString="";
        else
            usefulTempinString =  String.valueOf(Math.round(usefulTemp))+" ";
        String unit = metric ? "\u2103" : "\u2109";
        return usefulTempinString + unit;
    }

    public static void getAddressFromLatLong(double lat, double lon, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat,lon,1);
            String toastMessage = addresses.get(0).getLocality()+", "+addresses.get(0).getCountryName();
            Log.d("Location geocoded", toastMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DetailActivityDataModel getDetailModelfromDBModel(DBModel dbModel, int day, int imageId, int iconTint, boolean metric){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, day);
        SimpleDateFormat sf = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        String date = sf.format(calendar.getTime());
        String tempMain = String.valueOf((int) dbModel.getTemp());
        String unit = makeTemperaturePretty(-1, metric);
        String tempMin = String.valueOf(Math.round(dbModel.getTemp_min()));
        String tempMax = String.valueOf(Math.round(dbModel.getTemp_max()));
        String[] windData = dbModel.getWind().split("/");
        String windDescription = windClassifier(Double.parseDouble(windData[0]));
        float windDirection = Float.parseFloat(windData[1]);
        String windValue = windData[0] + (metric ? " m/s" : " mi/h");
        String humidity = String.valueOf(dbModel.getHumidity())+"%";
        String pressure = String.valueOf(dbModel.getPressure())+"mb";
        String UV = "UV Index: "+String.valueOf(dbModel.getUvIndex());
        String UVRisk = "Risk: "+UVClassifier(dbModel.getUvIndex());
        String rain = "Rain: "+String.valueOf(new DecimalFormat("#.##").format(dbModel.getRain_3h()))+" mm";
        String clouds = "Cloudiness: "+String.valueOf(dbModel.getClouds())+"%";
        return new DetailActivityDataModel(date, imageId, dbModel.getWeather_main(), dbModel.getWeather_desc(), tempMain, unit, tempMin, tempMax, windDescription, windDirection, iconTint, windValue, pressure, humidity, UV, UVRisk, rain, clouds);
    }

    public static String windClassifier(double speed){
        double speedInKPH = (18.0/5.0)*speed;
        if(speedInKPH<2)
            return "Calm";
        else if(isBetween(speedInKPH,2.0,6.0))
            return "Light Air";
        else if (isBetween(speedInKPH,7.0,11.0))
            return "Light Breeze";
        else if(isBetween(speedInKPH,12.0,19.0))
            return "Gentle Breeze";
        else if(isBetween(speedInKPH,20.0,30.0))
            return "Moderate Breeze";
        else if(isBetween(speedInKPH,31.0,39.0))
            return "Fresh Breeze";
        else if(isBetween(speedInKPH,40.0,50.0))
            return "Strong Breeze";
        else if(isBetween(speedInKPH,51.0,61.0))
            return "Moderate Gale";
        else if(isBetween(speedInKPH,62.0,74.0))
            return "Fresh Gale";
        else if(isBetween(speedInKPH,75.0,87.0))
            return "Strong Gale";
        else if(isBetween(speedInKPH,88.0,102.0))
            return "Whole Gale";
        else if(isBetween(speedInKPH,103.0,117.0))
            return "Storm";
        else if(speedInKPH>=119.0)
            return "Hurricane";
        return "N/A";
    }

    public static String UVClassifier(double index){
        if(isBetween(index,0.0,2.9))
            return "Low";
        else if(isBetween(index,3.0,5.9))
            return "Moderate";
        else if(isBetween(index,6.0,7.9))
            return "High";
        else if(isBetween(index,8.0,12.9))
            return "Very High";
        else if(index>=13.0)
            return "Extreme";
        return "";
    }

    public static boolean isBetween(double number, double lower, double upper){
        return number>=lower && number<=upper;
    }

}
