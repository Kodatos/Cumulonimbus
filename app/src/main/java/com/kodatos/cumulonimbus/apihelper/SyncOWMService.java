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

package com.kodatos.cumulonimbus.apihelper;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.RemoteException;
import android.preference.PreferenceManager;
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
import com.kodatos.cumulonimbus.utils.ServiceLocator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import retrofit2.Call;
import retrofit2.Response;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread for getting weather data from OpenWeatherMap.
 * <p>
 * Two notable observations are used to properly set and evaluate weather data while properly demarcating current and forecast :
 * 1. For any given time in UTC, the number of 3 hour forecasts for the same day available can be given by 7 - (time/3).
 * Also, since the first row is occupied by current data, one more row has to be skipped to get the next day forecast
 * 2. Every 8th record from the beginning of forecast data corresponds to data for the current time but for the next coming days.
 * This is suitable for getting a row from the whole database which is guaranteed to belong to the desired day.
 */
public class SyncOWMService extends IntentService {

    //Action strings for use in caller function
    public static final String UPDATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_UPDATE_DB";
    public static final String CREATE_ACTION = "com.kodatos.cumulonimbus.apihelper.SyncOWMService.ACTION_NEW_DB";
    //TODO Insert own OpenWeatherMap OWM_API_KEY here or make user enter one
    private static String OWM_API_KEY;
    //Tag for logging
    private final String LOG_TAG = getClass().getName();
    private Intent mIntent;
    private WeatherAPIService weatherAPIService;

    private boolean errorBroadcasted = false;

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences weatherSharedPreferences;

    private String backupAddressFromResponse;

    public SyncOWMService() {
        super("SyncOWMService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        OWM_API_KEY = getString(R.string.owm_api_key);
        Log.d(LOG_TAG, OWM_API_KEY);
        weatherAPIService = ServiceLocator.getBaseService();
        mIntent = intent;

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherSharedPreferences = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

        DecimalFormat df = new DecimalFormat("#0.####");
        String lat, lon;
        String units = defaultSharedPreferences.getBoolean(getString(R.string.pref_metrics_key), true) ? "metric" : "imperial";
        if (defaultSharedPreferences.getBoolean(this.getString(R.string.pref_curr_location_key), true)) {
            //User expects current location data. Hence, last known co-ordinates are retrieved and the respective url called.
            FusedLocationProviderClient mFusedClient = LocationServices.getFusedLocationProviderClient(this);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                broadcastError(ServiceErrorContract.ERROR_NO_PERMISSION, null);
                return;
            } else {
                try {
                    Location location = Tasks.await(mFusedClient.getLastLocation());
                    if (location != null) {
                        lat = df.format(location.getLatitude());
                        lon = df.format(location.getLongitude());
                    } else {
                        broadcastError(ServiceErrorContract.ERROR_LOCATION, ServiceErrorContract.ERROR_DETAILS_NULL + "/" + mIntent.getAction());
                        return;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_GENERIC);
                    return;
                }
            }
        } else {
            String chosenCoordinates = defaultSharedPreferences.getString(KeyConstants.CHOSEN_COORDINATES, "NIL");
            if ("NIL".equals(chosenCoordinates)) {
                broadcastError(ServiceErrorContract.ERROR_NO_LOCATION_CHOSEN, ServiceErrorContract.ERROR_DETAILS_NULL);
                return;
            }
            lat = chosenCoordinates.split("/")[0];
            lon = chosenCoordinates.split("/")[1];
        }
        final Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByCoords(lat, lon, OWM_API_KEY, units);
        Log.i(LOG_TAG, currentWeatherModelCall.request().url().toString());
        final Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByCoords(lat, lon, OWM_API_KEY, units);

        //Execute responses
        try {
            handleCurrentWeatherResponse(currentWeatherModelCall, lat, lon);
            handleForecastWeatherResponse(forecastWeatherModelCall);
            getUVIndex(lat, lon);
        } catch (IOException | JSONException e) {
            broadcastError(ServiceErrorContract.ERROR_GENERIC, ServiceErrorContract.ERROR_DETAILS_IO);
            e.printStackTrace();
            return;
        }
        defaultSharedPreferences.edit().putLong(KeyConstants.LAST_UPDATE_DATE_KEY, System.currentTimeMillis()).apply();
    }


    // Handles acquiring UV index data for both current and forecast weather
    private void getUVIndex(String lat, String lon) throws IOException, JSONException {
        Call<UVIndexModel> uvIndexModelCall = weatherAPIService.getCurrentUVIndex(lat, lon, OWM_API_KEY);
        Call<List<UVIndexModel>> uvIndexModelsCall = weatherAPIService.getForecastUVIndex(lat, lon, OWM_API_KEY, 4);
        ArrayList<ContentProviderOperation> uvOps = new ArrayList<>();
        try {
            Response<UVIndexModel> currentUVIndexModelResponse = uvIndexModelCall.execute();
            Response<List<UVIndexModel>> forecastUVIndexModelsResponse = uvIndexModelsCall.execute();

            if (currentUVIndexModelResponse.isSuccessful()) {
                UVIndexModel uvIndexModel = Objects.requireNonNull(currentUVIndexModelResponse.body());
                ContentValues cv = new ContentValues();
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, uvIndexModel.value);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{"1"};
                //getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                uvOps.add(ContentProviderOperation.newUpdate(WeatherDBContract.WeatherDBEntry.CONTENT_URI.buildUpon().appendPath("uvupdate").build())
                        .withValues(cv)
                        .withSelection(where, selectionArgs).build());
            } else {
                broadCastJSONError(Objects.requireNonNull(currentUVIndexModelResponse.errorBody()).string());
            }

            if (forecastUVIndexModelsResponse.isSuccessful()) {
                List<UVIndexModel> uvIndexModels = Objects.requireNonNull(forecastUVIndexModelsResponse.body());
                int count = Math.min(4, uvIndexModels.size());
                for (int i = 1; i <= count; i++) {
                    /*
                     * The UV Index API response only has one record per day, in contrast to 8 records per day for weather data.
                     * Hence, it is written at only one row from the 8 rows of that particular day at a position which can be determined
                     * easily at data binding
                     */
                    ContentValues cv = new ContentValues();
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, uvIndexModels.get(i - 1).value);
                    String where = "_ID=?";
                    String[] selectionArgs = new String[]{String.valueOf((i * 8) + 1)};
                    //getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                    uvOps.add(ContentProviderOperation.newUpdate(WeatherDBContract.WeatherDBEntry.CONTENT_URI.buildUpon().appendPath("uvupdate").build())
                            .withValues(cv)
                            .withSelection(where, selectionArgs).build());
                }
                getContentResolver().applyBatch(WeatherDBContract.AUTHORITY, uvOps);
            } else {
                broadCastJSONError(Objects.requireNonNull(forecastUVIndexModelsResponse.errorBody()).string());
                stopSelf();
            }
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    //Handles acquiring current weather data
    private void handleCurrentWeatherResponse(Call<CurrentWeatherModel> call, String... coordinates) throws IOException, JSONException {
        Response<CurrentWeatherModel> response = call.execute();
        if (response.isSuccessful()) {
            CurrentWeatherModel currentWeatherModelResponse = response.body();
            ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
            ContentValues cv = Objects.requireNonNull(currentWeatherModelResponse).getEquivalentCV();

            backupAddressFromResponse = currentWeatherModelResponse.name;

            // Current weather data is stored as 1st ID
            String where = "_ID=?";
            String[] selectionArgs = new String[]{"1"};
            if (UPDATE_ACTION.equals(mIntent.getAction())) {
                //getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                contentProviderOperations.add(ContentProviderOperation.newUpdate(WeatherDBContract.WeatherDBEntry.CONTENT_URI)
                        .withValues(cv)
                        .withSelection(where, selectionArgs).build());
                try {
                    getContentResolver().applyBatch(WeatherDBContract.AUTHORITY, contentProviderOperations);
                } catch (RemoteException | OperationApplicationException e) {
                    e.printStackTrace();
                }
            } else if (CREATE_ACTION.equals(mIntent.getAction())) {
                //Since UV Index data is acquired after current weather, it is initially set as 0
                cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
                //getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI, cv);
            }

            //Current weather data that cannot be written in database is saved in shared preferences.
            String locationAndIcon = "Unknown. Try again";
            //Generate location string to display in UI. The boolean segment indicates current or custom location.
            if (defaultSharedPreferences.getBoolean(this.getString(R.string.pref_curr_location_key), true)) {
                String address = getCachedOrReverseGeocodedAddress(Double.parseDouble(coordinates[0]), Double.parseDouble(coordinates[1]));
                if (address == null) {
                    broadcastError(ServiceErrorContract.ERROR_REVERSE_GEOCODER, "null");
                    Log.e(LOG_TAG, "location null");
                    stopSelf();
                } else {
                    locationAndIcon = address + "/true";
                }
            } else {
                locationAndIcon = defaultSharedPreferences.getString(KeyConstants.CHOSEN_CUSTOM_LOCATION, "") + "/false";
            }
            weatherSharedPreferences.edit().putLong(KeyConstants.CURRENT_WEATHER_SUNRISE_KEY, currentWeatherModelResponse.sysCurrent.sunrise).
                    putLong(KeyConstants.CURRENT_WEATHER_SUNSET_KEY, currentWeatherModelResponse.sysCurrent.sunset).
                    putString(KeyConstants.CURRENT_WEATHER_ICON_ID_KEY, currentWeatherModelResponse.weather.get(0).icon).
                    putLong(KeyConstants.CURRENT_WEATHER_VISIBILITY_KEY, currentWeatherModelResponse.visibility).
                    putString(KeyConstants.LOCATION_NAME_KEY, locationAndIcon).apply();
        } else {
            broadCastJSONError(Objects.requireNonNull(response.errorBody()).string());
            stopSelf();
        }
    }

    //Handles acquiring forecast weather data for next 4 days
    private void handleForecastWeatherResponse(Call<ForecastWeatherModel> call) throws IOException, JSONException {
        Response<ForecastWeatherModel> response = call.execute();
        if (response.isSuccessful()) {
            ForecastWeatherModel forecastWeatherModelResponse = Objects.requireNonNull(response.body());
            ArrayList<ContentProviderOperation> contentProviderOperations = new ArrayList<>();
            ArrayList<ContentValues> bulkContentValues = new ArrayList<>();
            //Store all records available
            for (int i = 0; i <= forecastWeatherModelResponse.cnt; i++) {
                ContentValues cv = forecastWeatherModelResponse.getEquivalentCV(i);
                String where = "_ID=?";
                String[] selectionArgs = new String[]{String.valueOf(i + 1)};
                if (UPDATE_ACTION.equals(mIntent.getAction())) {
                    //If update, add as a batch operation
                    //getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv,where,selectionArgs);
                    contentProviderOperations.add(ContentProviderOperation.newUpdate(WeatherDBContract.WeatherDBEntry.CONTENT_URI)
                            .withValues(cv)
                            .withSelection(where, selectionArgs).build());
                } else if (i == 0) {
                    /*
                    Current weather data rarely gives rain volume for the day. Hence, it is calculated here. Not included in
                    bulk insert and updated separately.
                    */
                    getContentResolver().update(WeatherDBContract.WeatherDBEntry.CONTENT_URI, cv, where, selectionArgs);
                } else if (CREATE_ACTION.equals(mIntent.getAction())) {
                    //If insert, add as a part of the bulk insert
                    cv.put(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX, 0.0);
                    //getContentResolver().insert(WeatherDBContract.WeatherDBEntry.CONTENT_URI,cv);
                    bulkContentValues.add(cv);
                }
            }
            try {
                if (UPDATE_ACTION.equals(mIntent.getAction()))
                    getContentResolver().applyBatch(WeatherDBContract.AUTHORITY, contentProviderOperations);
                else if (CREATE_ACTION.equals(mIntent.getAction())) {
                    getContentResolver().bulkInsert(WeatherDBContract.WeatherDBEntry.CONTENT_URI, bulkContentValues.toArray(new ContentValues[0]));
                }
            } catch (RemoteException | OperationApplicationException e) {
                e.printStackTrace();
            }
            Log.d(LOG_TAG, "forecast done");
        } else {
            broadCastJSONError(Objects.requireNonNull(response.errorBody()).string());
            stopSelf();
        }
    }

    /**
     * Utility function that converts provided co-ordinates to human readable address. First it checks whether the same co-ordinates have
     * been reverse geocoded previously. If yes, it provides the previous address results. Else, it does a fresh reverse-geocoding of
     * the co-ordinates and saves the results.
     *
     * @param lat Latitude part of co-ordinates
     * @param lon Longitude part of co-ordinates
     * @return String representing a human readable address. Null if reverse geocoding failed
     */
    private String getCachedOrReverseGeocodedAddress(double lat, double lon) {
        String addressKey = KeyConstants.CACHED_ADDRESS_KEY;
        String coordsKey = KeyConstants.CACHED_COORDS_KEY;
        String coordsAsString = lat + "/" + lon;
        if (weatherSharedPreferences.contains(coordsKey) && weatherSharedPreferences.contains(addressKey)) {
            if (weatherSharedPreferences.getString(coordsKey, "").equals(coordsAsString)) {
                Log.d(LOG_TAG, "Using cached address");
                return weatherSharedPreferences.getString(addressKey, "");
            }
        }
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String address;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses == null)  //No matches were found
                return null;
            address = (addresses.get(0).getSubLocality() != null ? addresses.get(0).getSubLocality() : "") + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
        } catch (IOException e) {
            if (!Geocoder.isPresent())
                Log.d(LOG_TAG, "No geocoder present!");
            Log.d(LOG_TAG, "Geocoder failed. Using weather response");
            address = backupAddressFromResponse;
        }
        weatherSharedPreferences.edit()
                .putString(coordsKey, coordsAsString)
                .putString(addressKey, address)
                .apply();
        return address;
    }

    private void broadCastJSONError(String jsonString) throws JSONException {
        JSONObject jsonError;
        jsonError = new JSONObject(jsonString);
        String errorCode = String.valueOf(jsonError.getInt("cod"));
        String errorMessage = jsonError.getString("message");
        String errorLog = errorCode + ":" + errorMessage;
        broadcastError(ServiceErrorContract.ERROR_RESPONSE, errorLog);
        Log.w(LOG_TAG, errorLog);
    }

    private void broadcastError(String type, String details) {
        if (!errorBroadcasted) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(ServiceLocator.getServiceErrorBroadcastIntent(type, details));
            errorBroadcasted = true;
        }
    }
}
