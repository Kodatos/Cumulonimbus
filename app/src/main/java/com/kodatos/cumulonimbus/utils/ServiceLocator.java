package com.kodatos.cumulonimbus.utils;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.UVIndexActivity;
import com.kodatos.cumulonimbus.apihelper.ServiceErrorContract;
import com.kodatos.cumulonimbus.apihelper.SyncOWMService;
import com.kodatos.cumulonimbus.apihelper.WeatherAPIService;
import com.kodatos.cumulonimbus.uihelper.InfoDialogFragment;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceLocator {

    public static WeatherAPIService getBaseService(){
        final String WEATHER_BASE_URL = "https://api.openweathermap.org/";
        Retrofit weatherRetrofit = new Retrofit.Builder()
                .baseUrl(WEATHER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return weatherRetrofit.create(WeatherAPIService.class);
    }

    public static InfoDialogFragment createFeelsLikeDialog(Activity activity, String feelsLikeDescription){
        String title = activity.getString(R.string.apparent_temperature_title);
        int drawableID = R.drawable.ic_thermometer;
        int backgroundColor = ContextCompat.getColor(activity, R.color.apparent_temperature_color);
        return createInfoDialog(title, feelsLikeDescription, drawableID, backgroundColor, null, null);
    }

    public static InfoDialogFragment createRainInfoDialog(Context context){
        String title = context.getString(R.string.rain_volume_info_title);
        String description = context.getString(R.string.rain_volume_info_desc);
        int drawableID = R.drawable.ic_umbrella;
        int backgroundColor = ContextCompat.getColor(context, R.color.rain_volume_info_color);
        return createInfoDialog(title, description, drawableID, backgroundColor, null, null);
    }

    public static InfoDialogFragment createWindInfoDialog(Activity activity){
        String title = activity.getString(R.string.wind_info_title);
        String description = activity.getString(R.string.wind_info_desc);
        int drawableID = R.drawable.ic_wind_direction_cut;
        int backgroundColor = ContextCompat.getColor(activity, R.color.wind_info_color);
        String positiveText = activity.getString(R.string.uv_info_positive);
        View.OnClickListener positiveAction = v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.wind_info_wiki_url)));
            if(browserIntent.resolveActivity(activity.getPackageManager()) != null){
                activity.startActivity(browserIntent);
            }
        };
        return createInfoDialog(title, description, drawableID, backgroundColor, positiveText, positiveAction);
    }

    public static InfoDialogFragment createUVInfoDialog(Activity activity){
        String title = activity.getString(R.string.uv_info_title);
        String description = activity.getString(R.string.uv_info_desc);
        int drawableID = R.drawable.ic_uv_index;
        int backgroundColor = ContextCompat.getColor(activity, R.color.uv_info_color);
        String positiveText = activity.getString(R.string.uv_info_positive);
        View.OnClickListener positiveAction  = v -> {
            //Open UV info activity
            Intent intent = new Intent(activity, UVIndexActivity.class);
            activity.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(activity).toBundle());
        };
        return createInfoDialog(title, description, drawableID, backgroundColor, positiveText, positiveAction);
    }

    public static InfoDialogFragment createLocationInfoDialog(Activity activity, boolean isRefreshing){
        String title = activity.getString(R.string.location_info_title);
        String description = activity.getString(R.string.location_info_desc);
        int drawableID = R.drawable.ic_location_current;
        int backgroundColor = ContextCompat.getColor(activity, R.color.location_info_color);
        String positiveText = activity.getString(R.string.location_info_positive);
        View.OnClickListener positiveAction = v -> {
            if(!isRefreshing){
                          /*
                            Generate a location error broadcast typically used by the SyncOWMService, as the actions are similar here.
                            Also, since this is user invoked, it is safe to assume sync is an update.
                           */
                LocalBroadcastManager.getInstance(activity)
                        .sendBroadcast(getServiceErrorBroadcastIntent(ServiceErrorContract.ERROR_LOCATION, ServiceErrorContract.ERROR_DETAILS_NULL + "/" + SyncOWMService.UPDATE_ACTION));
            }
        };
        return createInfoDialog(title, description, drawableID, backgroundColor, positiveText, positiveAction);
    }

    private static InfoDialogFragment createInfoDialog(String title, String description, int drawableID, int backgroundColor, String positiveText, View.OnClickListener positiveAction){
        InfoDialogFragment fragment = InfoDialogFragment.newInstance(title, description, drawableID, backgroundColor);
        if(positiveText != null){
            fragment.setPositiveAction(positiveText, positiveAction);
        }
        return fragment;
    }

    //Common method to get an intent for error broadcast between service and activity. Details are provided by caller
    public static Intent getServiceErrorBroadcastIntent(String type, String details){
        Intent errorIntent = new Intent(ServiceErrorContract.BROADCAST_INTENT_FILTER);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_TYPE, type);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_DETAILS, details);
        return errorIntent;
    }
}
