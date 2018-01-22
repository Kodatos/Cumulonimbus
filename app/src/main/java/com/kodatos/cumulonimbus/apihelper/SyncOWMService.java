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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.ForecastWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.UVIndexModel;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread for getting weather data from OpenWeatherMap.
 *
 * Two notable observations are used to properly set and evaluate weather data while properly demarcating current and forecast :
 *  1. For any given time in UTC, the number of 3 hour forecasts for the same day available can be given by 7 - (time/3).
 *      Also, since the first row is occupied by current data, one more row has to be skipped to get the next day forecast
 *  2. Every 8th record from the beginning of forecast data corresponds to data for the current time but for the next coming days.
 *     This is suitable for getting a row from the whole database which is guaranteed to belong to the desired day.
 */
public class SyncOWMService extends IntentService {

    public static final String BASE_URL = "http://api.openweathermap.org/";
    //Action strings for use in caller function
    public static final String UPDATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_UPDATE_DB";
    public static final String CREATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_NEW_DB";
    //TODO Insert own OpenWeatherMap API_KEY here or make user enter one
    private static String API_KEY;
    //Tag for logging
    private final String LOG_TAG = getClass().getName();
    private Intent mIntent;
    private WeatherAPIService weatherAPIService;
    private String units;

    public SyncOWMService() {
        super("SyncOWMService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        API_KEY = getString(R.string.owm_api_key);
        Log.d(LOG_TAG, API_KEY);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherAPIService = retrofit.create(WeatherAPIService.class);
        mIntent = intent;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        units = sp.getBoolean(getString(R.string.pref_metrics_key), true) ? "metric" : "imperial";
        if (sp.getBoolean(this.getString(R.string.pref_curr_location_key), true)) {

            //User expects current location data. Hence, last known co-ordinates are retrieved and the respective url called.
            FusedLocationProviderClient mFusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    Location location = Tasks.await(mFusedClient.getLastLocation());
                    if (location != null) {
                        final double lat = location.getLatitude();
                        final double lon = location.getLongitude();
                        final Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByCoords(lat, lon, API_KEY, units);
                        Log.i(LOG_TAG, currentWeatherModelCall.request().url().toString());
                        final Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByCoords(lat, lon, API_KEY, units);

                        //Execute responses
                        handleCurrentWeatherResponse(currentWeatherModelCall, lat, lon);
                        handleForecastWeatherResponse(forecastWeatherModelCall);
                        getUVIndex(lat, lon);
                    } else {
                        broadcastError(ServiceErrorContract.ERROR_LOCATION, ServiceErrorContract.ERROR_DETAILS_NULL + "/" + mIntent.getAction());
                        stopSelf();
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_GENERIC);
                } catch (IOException e) {
                    broadcastError(ServiceErrorContract.ERROR_LOCATION, ServiceErrorContract.ERROR_DETAILS_IO);
                    e.printStackTrace();
                    return;
                }
            }
        } else {
            // User expects data for a particular custom location. The location string is directly used for url call.
            String custom_location = sp.getString(this.getString(R.string.pref_custom_location_key), this.getString(R.string.pref_custom_location_def));
            Log.i(LOG_TAG, custom_location);
            Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByString(custom_location, API_KEY, units);
            Log.i(LOG_TAG, currentWeatherModelCall.request().url().toString());
            Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByString(custom_location, API_KEY, units);

            //Execute responses
            try {
                //The UV index API only accepts co-ordinates as parameters. Hence the custom location name has to be geocoded
                String coords = getCachedOrGeocodedCoords(custom_location);
                if (coords != null) {
                    double latitude = Double.parseDouble(coords.split("/")[0]);
                    double longitude = Double.parseDouble(coords.split("/")[1]);
                    handleCurrentWeatherResponse(currentWeatherModelCall);
                    handleForecastWeatherResponse(forecastWeatherModelCall);
                    getUVIndex(latitude, longitude);
                } else {
                    broadcastError(ServiceErrorContract.ERROR_GEOCODER, ServiceErrorContract.ERROR_DETAILS_NULL);
                    stopSelf();
                }
            } catch (IOException e) {
                broadcastError(ServiceErrorContract.ERROR_GEOCODER, ServiceErrorContract.ERROR_DETAILS_IO);
                e.printStackTrace();
                return;
            }
        }
        sp.edit().putLong(KeyConstants.LAST_UPDATE_DATE_KEY, System.currentTimeMillis()).apply();
    }

    /**
     * Utility function that converts provided location to co-ordinates. First it checks whether the same location has
     * been geocoded previously. If yes, it provides the previous co-ordinate results. Else, it does a fresh geocoding of
     * the location name and saves the results.
     *
     * @param custom_location The user provided location to convert to co-ordinates.
     * @return A string with co-ordinate data. Null if geocoding failed.
     * @throws IOException If geocoding encountered network errors as specified in official Android documentation.
     */
    private String getCachedOrGeocodedCoords(String custom_location) throws IOException {
        String customLocationKey = KeyConstants.CACHED_CUSTOM_LOCATION_KEY;
        String customCoordsKey = KeyConstants.CACHED_CUSTOM_LOCATION_COORDS_KEY;
        SharedPreferences weatherSP = getSharedPreferences("weather_display_pref", MODE_PRIVATE);
        if (weatherSP.contains(customCoordsKey) && weatherSP.contains(customLocationKey)) {
            if (weatherSP.getString(customLocationKey, "").equals(custom_location))
                return weatherSP.getString(customCoordsKey, "");
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocationName(custom_location, 1);
        if (addresses == null)  //Location name doesn't exist
            return null;
        String coordsAsString = String.valueOf(addresses.get(0).getLatitude()) + "/" + String.valueOf(addresses.get(0).getLongitude());
        weatherSP.edit()
                .putString(customCoordsKey, coordsAsString)
                .putString(customLocationKey, custom_location)
                .apply();
        return coordsAsString;
    }

    /**
     * Utility function that converts provided co-ordinates to human readable address. First it checks whether the same co-ordinates have
     * been reverse geocoded previously. If yes, it provides the previous address results. Else, it does a fresh reverse-geocoding of
     * the co-ordinates and saves the results.
     *
     * @param lat Latitude part of co-ordinates
     * @param lon Longitude part of co-ordinates
     * @return String representing a human readable address. Null if reverse geocoding failed
     * @throws IOException If geocoding encountered network errors as specified in official Android documentation.
     */
    private String getCachedOrReverseGeocodedAddress(double lat, double lon) throws IOException {
        String addressKey = KeyConstants.CACHED_ADDRESS_KEY;
        String coordsKey = KeyConstants.CACHED_COORDS_KEY;
        String coordsAsString = String.valueOf(lat) + "/" + String.valueOf(lon);
        SharedPreferences weatherSP = getSharedPreferences("weather_display_pref", MODE_PRIVATE);
        if (weatherSP.contains(coordsKey) && weatherSP.contains(addressKey)) {
            if (weatherSP.getString(coordsKey, "").equals(coordsAsString))
                return weatherSP.getString(addressKey, "");
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
        if (addresses == null)  //No matches were found
            return null;
        String address = addresses.get(0).getSubLocality() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
        weatherSP.edit()
                .putString(coordsKey, coordsAsString)
                .putString(addressKey, address)
                .apply();
        return address;
    }


    // Handles acquiring UV index data for both current and forecast weather
    private void getUVIndex(double lat, double lon){
        Call<UVIndexModel> uvIndexModelCall = weatherAPIService.getCurrentUVIndex(lat, lon, API_KEY);
        Call<List<UVIndexModel>> uvIndexModelsCall = weatherAPIService.getForecastUVIndex(lat,lon,API_KEY,4);
        try {
            Response<UVIndexModel> currentUVIndexModelResponse = uvIndexModelCall.execute();
            Response<List<UVIndexModel>> forecastUVIndexModelsResponse = uvIndexModelsCall.execute();

            if (currentUVIndexModelResponse.isSuccessful()) {
                UVIndexModel uvIndexModel = currentUVIndexModelResponse.body();
                ContentValues cv = new ContentValues();
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, uvIndexModel.value);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{"1"};
                getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
            } else {
                JSONObject jsonError;
                jsonError = new JSONObject(currentUVIndexModelResponse.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+ errorMessage;
                broadcastError(ServiceErrorContract.ERROR_RESPONSE, errorLog);
                Log.w(LOG_TAG, errorLog);
            }

            if (forecastUVIndexModelsResponse.isSuccessful()) {
                List<UVIndexModel> uvIndexModels = forecastUVIndexModelsResponse.body();
                for(int i = 1; i<=4; i++){
                    /*
                     * The UV Index API response only has one record per day, in contrast to 8 records per day for weather data.
                     * Hence, it is written at only one row from the 8 rows of that particular day at a position which can be determined
                     * easily at data binding
                     */
                    ContentValues cv = new ContentValues();
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX,uvIndexModels.get(i-1).value);
                    String where = "_ID=?";
                    String[] selectionArgs = new String[]{String.valueOf((i * 8) + 1)};
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                }
            } else {
                JSONObject jsonError;
                jsonError = new JSONObject(forecastUVIndexModelsResponse.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+ errorMessage;
                broadcastError(ServiceErrorContract.ERROR_RESPONSE, errorLog);
                Log.w(LOG_TAG, errorLog);
                stopSelf();
            }

        } catch (JSONException | IOException e) {
            broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_GENERIC);
            e.printStackTrace();
        }
    }

    //Handles acquiring current weather data
    private void handleCurrentWeatherResponse(Call<CurrentWeatherModel> call, double... coordinates) throws IOException {
        Response<CurrentWeatherModel> response = call.execute();
        if(response.isSuccessful()){
            CurrentWeatherModel currentWeatherModelResponse = response.body();
            ContentValues cv = currentWeatherModelResponse.getEquivalentCV();
            SharedPreferences weatherSP = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

            //Current weather data that cannot be written in database is saved in shared preferences.
            String locationAndIcon = "Unknown. Try again";
            //Generate location string to display in UI. The boolean segment indicates current or custom location.
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(this.getString(R.string.pref_curr_location_key), true)) {
                try {
                    String address = getCachedOrReverseGeocodedAddress(coordinates[0], coordinates[1]);
                    if (address == null) {
                        broadcastError(ServiceErrorContract.ERROR_REVERSE_GEOCODER, "null");
                        Log.e(LOG_TAG, "location null");
                        stopSelf();
                    } else {
                        locationAndIcon = address + "/true";
                    }
                } catch (IOException e) {
                    broadcastError(ServiceErrorContract.ERROR_REVERSE_GEOCODER, "io");
                    e.printStackTrace();
                    stopSelf();
                }
            } else {
                locationAndIcon = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_custom_location_key), "") + "/false";
            }
            weatherSP.edit().putLong(KeyConstants.CURRENT_WEATHER_SUNRISE_KEY, currentWeatherModelResponse.sysCurrent.sunrise).
                    putLong(KeyConstants.CURRENT_WEATHER_SUNSET_KEY, currentWeatherModelResponse.sysCurrent.sunset).
                    putString(KeyConstants.CURRENT_WEATHER_ICON_ID_KEY, currentWeatherModelResponse.weather.get(0).icon).
                    putLong(KeyConstants.CURRENT_WEATHER_VISIBILITY_KEY, currentWeatherModelResponse.visibility).
                    putString(KeyConstants.LOCATION_NAME_KEY, locationAndIcon).apply();

            // Current weather data is stored as 1st ID
            String where = "_ID=?";
            String[] selectionArgs = new String[]{"1"};
            if(UPDATE_ACTION.equals(mIntent.getAction())){
                getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
            } else if(CREATE_ACTION.equals(mIntent.getAction())){
                //Since UV Index data is acquired after current weather, it is initially set as 0
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
                getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
            }
        } else {
            JSONObject jsonError;
            try {
                jsonError = new JSONObject(response.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+ errorMessage;
                broadcastError(ServiceErrorContract.ERROR_RESPONSE, errorLog);
                Log.w(LOG_TAG, errorLog);
                stopSelf();
            } catch (JSONException | IOException e) {
                broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_GENERIC);
                e.printStackTrace();
            }
        }
    }

    //Handles acquiring forecast weather data for next 4 days
    private void handleForecastWeatherResponse(Call<ForecastWeatherModel> call) throws IOException {
        Response<ForecastWeatherModel> response = call.execute();
        if (response.isSuccessful()) {
            ForecastWeatherModel forecastWeatherModelResponse = response.body();

            //Store all records available
            for (int i = 0; i <= forecastWeatherModelResponse.cnt; i++) {
                ContentValues cv = forecastWeatherModelResponse.getEquivalentCV(i);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{String.valueOf(i+1)};
                if(UPDATE_ACTION.equals(mIntent.getAction()) || i==0){
                    //Current weather data rarely gives rain volume for the day. Hence, it is calculated here.
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                } else if(CREATE_ACTION.equals(mIntent.getAction())){
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
                    getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                }
            }
            Log.d(LOG_TAG, "forecast done");
        } else {
            JSONObject jsonError;
            try {
                jsonError = new JSONObject(response.errorBody().string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                String errorMessage = jsonError.getString("message");
                String errorLog = errorCode+":"+ errorMessage;
                broadcastError(ServiceErrorContract.ERROR_RESPONSE, errorLog);
                Log.w(LOG_TAG, errorLog);
                stopSelf();
            } catch (JSONException | IOException e) {
                broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_GENERIC);
                e.printStackTrace();
            }
        }
    }

    private void broadcastError(String type, String details) {
        Intent errorIntent = new Intent(ServiceErrorContract.BROADCAST_INTENT_FILTER);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_TYPE, type);
        errorIntent.putExtra(ServiceErrorContract.SERVICE_ERROR_DETAILS, details);
        LocalBroadcastManager.getInstance(this).sendBroadcast(errorIntent);
    }
}
