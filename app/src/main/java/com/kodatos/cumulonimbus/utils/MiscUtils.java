package com.kodatos.cumulonimbus.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
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
    public static Spanned makeTemperaturePretty(float usefulTemp){
        int usefulTempinInt = (int) usefulTemp;
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(String.valueOf(usefulTempinInt)+"<sup>o</sup>",Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(String.valueOf(usefulTempinInt)+"<sup>o</sup>");
        }
        return result;
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
}
