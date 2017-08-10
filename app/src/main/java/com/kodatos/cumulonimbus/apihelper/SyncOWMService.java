package com.kodatos.cumulonimbus.apihelper;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.ForecastWeatherModel;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread for getting weather data from OpenWeatherMap.
 */
public class SyncOWMService extends IntentService implements OnSuccessListener<Location>{

    private Intent mIntent;
    private FusedLocationProviderClient mFusedClient;
    private WeatherAPIService weatherAPIService;
    private CurrentWeatherCallback mCurrentWeatherCallback;
    private ForecastWeatherCallback mForecastWeatherCallback;

    //TODO Insert own OpenWeatherMap API_KEY here or make user enter one
    private static String API_KEY;
    public static final String BASE_URL = "http://api.openweathermap.org/";

    //Action strings for use in caller function
    public static final String UPDATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_UPDATE_DB";
    public static final String CREATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_NEW_DB";

    //Tag for logging
    private final String LOG_TAG = "SyncOWMService ";

    public SyncOWMService() {
        super("SyncOWMService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        //TODO Remove Base64 decode
        API_KEY = new String(Base64.decode(getString(R.string.owm_api_key), Base64.DEFAULT));
        Log.w(LOG_TAG, API_KEY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherAPIService = retrofit.create(WeatherAPIService.class);
        mIntent = intent;
        mCurrentWeatherCallback = new CurrentWeatherCallback();
        mForecastWeatherCallback = new ForecastWeatherCallback();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(this.getString(R.string.pref_curr_location_key), false)) {
            mFusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedClient.getLastLocation().addOnSuccessListener(this);
            }
        }
        else{
            String custom_location = sp.getString(this.getString(R.string.pref_custom_location_key), this.getString(R.string.pref_custom_location_def));
            Log.d(LOG_TAG, custom_location);
            Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByString(custom_location, API_KEY);
            Log.d(LOG_TAG, currentWeatherModelCall.request().url().toString());
            Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByString(custom_location, API_KEY);
            currentWeatherModelCall.enqueue(mCurrentWeatherCallback);
            forecastWeatherModelCall.enqueue(mForecastWeatherCallback);
        }

        //TODO Add current location requests
    }

    /*
        onSuccess() apparently runs on the main UI thread and has been confirmed. Also, the workaround
        for calling this in a new thread is too intricate for me and it would be a mess to shift the code below to the main activity.
        Hence, a new thread is used for the Geocoder and call executions are also enqueued.
     */
    @Override
    public void onSuccess(Location location) {
        final double lat = location.getLatitude();
        final double lon = location.getLongitude();
        Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByCoords(lat,lon,API_KEY);
        Log.w(LOG_TAG, currentWeatherModelCall.request().url().toString());
        Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByCoords(lat,lon,API_KEY);
        currentWeatherModelCall.enqueue(mCurrentWeatherCallback);
        forecastWeatherModelCall.enqueue(mForecastWeatherCallback);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MiscUtils.getAddressFromLatLong(lat,lon,SyncOWMService.this);
            }
        }).start();
    }

    /*
        The only reason API request calls are executed asynchronously in the service is because the location based calls are running
        on the main UI thread due to onSuccessListener, even though string based calls are not.
    */
    private class CurrentWeatherCallback implements Callback<CurrentWeatherModel>{

        @Override
        public void onResponse(@NonNull Call<CurrentWeatherModel> call, @NonNull Response<CurrentWeatherModel> response) {
            if(response.isSuccessful()){
                CurrentWeatherModel currentWeatherModelResponse = response.body();
                ContentValues cv = currentWeatherModelResponse.getDBModel().getEquivalentCV();
                String where = "_ID=?";
                String[] selectionArgs = new String[]{"1"};
                if(UPDATE_ACTION.equals(mIntent.getAction())){
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                }
                else if(CREATE_ACTION.equals(mIntent.getAction())){
                    getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                }

            }
            else {
                JSONObject jsonError;
                try {
                    jsonError = new JSONObject(response.errorBody().string());
                    String errorCode = String.valueOf(jsonError.getInt("cod"));
                    String errorMessage = jsonError.getString("message");
                    String errorLog = errorCode+":"+errorMessage;
                    Log.w(LOG_TAG, errorLog);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<CurrentWeatherModel> call, @NonNull Throwable t) {
            t.printStackTrace();
            Log.w(LOG_TAG, "Couldn't access API. Check connection or call.");
        }
    }

    private class ForecastWeatherCallback implements Callback<ForecastWeatherModel>{

        @Override
        public void onResponse(@NonNull Call<ForecastWeatherModel> call, @NonNull Response<ForecastWeatherModel> response) {

            if (response.isSuccessful()) {
                ForecastWeatherModel forecastWeatherModelResponse = response.body();
                for(long i=1; i<=4; i++){
                    ContentValues cv = forecastWeatherModelResponse.getDBModel(i).getEquivalentCV();
                    String where = "_ID=?";
                    String[] selectionArgs = new String[]{String.valueOf(i+1)};
                    if(UPDATE_ACTION.equals(mIntent.getAction())){
                        getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                    }
                    else if(CREATE_ACTION.equals(mIntent.getAction())){
                        getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                    }
                }
            }
            else {
                JSONObject jsonError;
                try {
                    jsonError = new JSONObject(response.errorBody().string());
                    String errorCode = String.valueOf(jsonError.getInt("cod"));
                    String errorMessage = jsonError.getString("message");
                    String errorLog = errorCode+":"+errorMessage;
                    Log.w(LOG_TAG, errorLog);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<ForecastWeatherModel> call, @NonNull Throwable t) {
            t.printStackTrace();
            Log.w(LOG_TAG, "Couldn't access API. Check connection or call.");
        }
    }


}
