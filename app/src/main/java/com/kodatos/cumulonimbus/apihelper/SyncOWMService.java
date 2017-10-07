package com.kodatos.cumulonimbus.apihelper;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.ForecastWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.UVIndexModel;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread for getting weather data from OpenWeatherMap.
 */
public class SyncOWMService extends IntentService implements OnSuccessListener<Location>{

    private Intent mIntent;
    private WeatherAPIService weatherAPIService;
    private String units;

    //TODO Insert own OpenWeatherMap API_KEY here or make user enter one
    private static String API_KEY;
    public static final String BASE_URL = "http://api.openweathermap.org/";

    //Action strings for use in caller function
    public static final String UPDATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_UPDATE_DB";
    public static final String CREATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_NEW_DB";

    //Tag for logging
    private final String LOG_TAG = getClass().getName();

    public SyncOWMService() {
        super("SyncOWMService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        //TODO Remove Base64 decode
        API_KEY = getString(R.string.owm_api_key);
        Log.w(LOG_TAG, API_KEY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherAPIService = retrofit.create(WeatherAPIService.class);
        mIntent = intent;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        units = sp.getBoolean(getString(R.string.pref_metrics_key), true) ? "metric" : "imperial";
        if (sp.getBoolean(this.getString(R.string.pref_curr_location_key), false)) {
            FusedLocationProviderClient mFusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedClient.getLastLocation().addOnSuccessListener(this);
            }
        }
        else{
            String custom_location = sp.getString(this.getString(R.string.pref_custom_location_key), this.getString(R.string.pref_custom_location_def));
            Log.d(LOG_TAG, custom_location);
            Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByString(custom_location, API_KEY, units);
            Log.d(LOG_TAG, currentWeatherModelCall.request().url().toString());
            Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByString(custom_location, API_KEY, units);
            //currentWeatherModelCall.enqueue(mCurrentWeatherCallback);
            //forecastWeatherModelCall.enqueue(mForecastWeatherCallback);
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                handleCurrentWeatherResponse(currentWeatherModelCall);
                handleForecastWeatherResponse(forecastWeatherModelCall);
                List<Address> addresses = geocoder.getFromLocationName(custom_location, 1);
                getUVIndex(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sp.edit().putLong(getString(R.string.last_update_date_key), System.currentTimeMillis()).apply();
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
        final Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByCoords(lat,lon,API_KEY, units);
        Log.d(LOG_TAG, currentWeatherModelCall.request().url().toString());
        final Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByCoords(lat,lon,API_KEY, units);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleCurrentWeatherResponse(currentWeatherModelCall);
                    handleForecastWeatherResponse(forecastWeatherModelCall);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MiscUtils.getAddressFromLatLong(lat,lon,SyncOWMService.this);
                getUVIndex(lat, lon);
            }
        }).start();
    }

    private void getUVIndex(double lat, double lon){
        Call<UVIndexModel> uvIndexModelCall = weatherAPIService.getCurrentUVIndex(lat, lon, API_KEY);
        Call<List<UVIndexModel>> uvIndexModelsCall = weatherAPIService.getForecastUVIndex(lat,lon,API_KEY,4);
        //uvIndexModelCall.enqueue(new CurrentUVIndexCallback());
        //uvIndexModelsCall.enqueue(new ForecastUVIndexCallback());
        try {
            Response<UVIndexModel> uvIndexModelResponse = uvIndexModelCall.execute();
            Response<List<UVIndexModel>> uvIndexModelsResponse = uvIndexModelsCall.execute();
            if(uvIndexModelResponse.isSuccessful()){
                UVIndexModel uvIndexModel = uvIndexModelResponse.body();
                ContentValues cv = new ContentValues();
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, uvIndexModel.value);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{"1"};
                getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
            }
            else {
                JSONObject jsonError;
                jsonError = new JSONObject(uvIndexModelResponse.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+errorMessage;
                Log.w(LOG_TAG, errorLog);
            }
            if(uvIndexModelsResponse.isSuccessful()){
                List<UVIndexModel> uvIndexModels = uvIndexModelsResponse.body();
                for(int i=1; i<=4; i++){
                    ContentValues cv = new ContentValues();
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX,uvIndexModels.get(i-1).value);
                    String where = "_ID=?";
                    String[] selectionArgs = new String[]{String.valueOf(i+1)};
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                }
            }
            else {
                JSONObject jsonError;
                jsonError = new JSONObject(uvIndexModelsResponse.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+errorMessage;
                Log.w(LOG_TAG, errorLog);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCurrentWeatherResponse(Call<CurrentWeatherModel> call) throws IOException {
        Response<CurrentWeatherModel> response = call.execute();
        if(response.isSuccessful()){
            CurrentWeatherModel currentWeatherModelResponse = response.body();
            ContentValues cv = currentWeatherModelResponse.getEquivalentCV();
            SharedPreferences weatherSP = getSharedPreferences("weather_display_pref", MODE_PRIVATE);
            weatherSP.edit().putLong(getString(R.string.current_weather_sunrise_key), currentWeatherModelResponse.sysCurrent.sunrise).
                    putLong(getString(R.string.current_weather_sunset_key), currentWeatherModelResponse.sysCurrent.sunset).
                    putString(getString(R.string.current_weather_icon_id_key), currentWeatherModelResponse.weather.get(0).icon).
                    putLong(getString(R.string.current_weather_visibility), currentWeatherModelResponse.visibility).apply();
            String where = "_ID=?";
            String[] selectionArgs = new String[]{"1"};
            if(UPDATE_ACTION.equals(mIntent.getAction())){
                getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
            }
            else if(CREATE_ACTION.equals(mIntent.getAction())){
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
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

    private void handleForecastWeatherResponse(Call<ForecastWeatherModel> call) throws IOException {
        Response<ForecastWeatherModel> response = call.execute();
        if (response.isSuccessful()) {
            ForecastWeatherModel forecastWeatherModelResponse = response.body();
            for(int i=0; i<=4; i++){
                ContentValues cv = forecastWeatherModelResponse.getEquivalentCV(i);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{String.valueOf(i+1)};
                if(UPDATE_ACTION.equals(mIntent.getAction()) || i==0){
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                }
                else if(CREATE_ACTION.equals(mIntent.getAction())){
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
                    getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                }
            }
            Log.d(LOG_TAG, "forecast done");
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
}
