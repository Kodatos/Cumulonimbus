package com.kodatos.cumulonimbus.apihelper;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.ForecastWeatherModel;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;

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
public class syncOWMService extends IntentService {

    private Intent mIntent;

    //TODO Insert own OpenWeatherMap API_KEY here
    public static final String API_KEY = "xxxxxxxx";
    public static final String BASE_URL = "api.openweathermap.org";

    public static final String UPDATE_ACTION = "com.kodatos.cumulonimbus.apihelper.syncOWMService.ACTION_UPDATE_DB";
    public static final String CREATE_ACTION = "com.kodatos.cumulonimbus.apihelper.syncOWMService.ACTION_NEW_DB";

    public syncOWMService() {
        super("syncOWMService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherAPIService weatherAPIService = retrofit.create(WeatherAPIService.class);
        mIntent=intent;
        CurrentWeatherCallback mCurrentWeatherCallback = new CurrentWeatherCallback();
        ForecastWeatherCallback mForecastWeatherCallback = new ForecastWeatherCallback();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String custom_location = sp.getString(getString(R.string.pref_custom_location_key), null);

        Call<CurrentWeatherModel> currentWeatherModelCall = weatherAPIService.getCurrentWeatherByString(custom_location,API_KEY);
        currentWeatherModelCall.enqueue(mCurrentWeatherCallback);
        Call<ForecastWeatherModel> forecastWeatherModelCall = weatherAPIService.getForecastWeatherByString(custom_location,API_KEY);
        forecastWeatherModelCall.enqueue(mForecastWeatherCallback);

        //TODO Add current location requests
    }

    private class CurrentWeatherCallback implements Callback<CurrentWeatherModel>{

        @Override
        public void onResponse(@NonNull Call<CurrentWeatherModel> call, @NonNull Response<CurrentWeatherModel> response) {
            if(response.isSuccessful()){
                CurrentWeatherModel currentWeatherModelResponse = response.body();
                ContentValues cv = currentWeatherModelResponse.getDBModel().getEquivalentCV();
                String where = "ID=?";
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
                    Log.w(this.getClass().getName(), errorLog);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<CurrentWeatherModel> call, @NonNull Throwable t) {
            t.printStackTrace();
            Log.w(this.getClass().getName(), "Couldn't access API. Check connection or call.");
        }
    }

    private class ForecastWeatherCallback implements Callback<ForecastWeatherModel>{

        @Override
        public void onResponse(@NonNull Call<ForecastWeatherModel> call, @NonNull Response<ForecastWeatherModel> response) {

            if (response.isSuccessful()) {
                ForecastWeatherModel forecastWeatherModelResponse = response.body();
                for(long i=1; i<=4; i++){
                    ContentValues cv = forecastWeatherModelResponse.getDBModel(i).getEquivalentCV();
                    String where = "ID=?";
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
                    Log.w(this.getClass().getName(), errorLog);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(@NonNull Call<ForecastWeatherModel> call, @NonNull Throwable t) {
            t.printStackTrace();
            Log.w(this.getClass().getName(), "Couldn't access API. Check connection or call.");
        }
    }
}
