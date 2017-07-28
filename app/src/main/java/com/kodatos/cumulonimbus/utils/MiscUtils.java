package com.kodatos.cumulonimbus.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/*
    A utility class containing miscellaneous functions that may be used in other functions.
 */
public class MiscUtils {

    /**
     * Converts given temperature in Kelvin to Celsius or Fahrenheit.
     * @param temp Temperature in Kelvin to convert
     * @param metric Boolean representing choice of metric or imperial umits
     * @return A Spanned object with converted temperature and degree symbol in superscript
     */
    @SuppressWarnings("deprecation")
    public static Spanned tempFromHtml(float temp, boolean metric){
        int tempInC = (int) (temp - 273.15);
        int tempInF = (int) ((tempInC*1.8)+32);
        int usefulTemp = metric? tempInC : tempInF;
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(String.valueOf(usefulTemp)+"<sup>o</sup>",Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(String.valueOf(usefulTemp)+"<sup>o</sup>");
        }
        return result;
    }

    public static void displayAddressFromLatLong(double lat, double lon, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat,lon,1);
            String toastMessage = addresses.get(0).getLocality()+", "+addresses.get(0).getCountryName();
            Toast.makeText(context,toastMessage,Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
