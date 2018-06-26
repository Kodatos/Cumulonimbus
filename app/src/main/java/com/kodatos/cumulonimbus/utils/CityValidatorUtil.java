package com.kodatos.cumulonimbus.utils;

import android.util.Log;

import com.kodatos.cumulonimbus.apihelper.WeatherAPIService;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class CityValidatorUtil {

    public static int INVALID = 32;
    public static int VALID = 64;

    public static String[] checkIfStringValid(String city, String api_key) {
        //Check internet
        String LOG_TAG = "city-validator";
        Log.d(LOG_TAG, "validating");
        WeatherAPIService checkerService = ServiceLocator.getBaseService();
        Call<CurrentWeatherModel> currentWeatherModelCall = checkerService.getCurrentWeatherByString(city, api_key, "metric");
        try {
            Response<CurrentWeatherModel> response = currentWeatherModelCall.execute();
            if (response.isSuccessful()) {
                CurrentWeatherModel model = Objects.requireNonNull(response.body());
                return new String[]{model.name + ", " + model.sysCurrent.country, String.valueOf(model.coord.lat), String.valueOf(model.coord.lon)};
            } else {
                JSONObject jsonError = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                String errorCode = String.valueOf(jsonError.getInt("cod"));
                if ("404".equals(errorCode) || "400".equals(errorCode)) {
                    Log.d(LOG_TAG, "invalid_city");
                }
                return null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
