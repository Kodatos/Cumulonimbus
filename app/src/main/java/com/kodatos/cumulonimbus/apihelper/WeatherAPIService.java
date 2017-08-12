package com.kodatos.cumulonimbus.apihelper;

import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.ForecastWeatherModel;
import com.kodatos.cumulonimbus.apihelper.models.UVIndexModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
    An interface with functions for requesting current and forecast weather data for current co-ordinates or a user-entered location
 */

public interface WeatherAPIService {

    @GET("data/2.5/weather")
    Call<CurrentWeatherModel> getCurrentWeatherByString(@Query("q") String q, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/weather")
    Call<CurrentWeatherModel> getCurrentWeatherByCoords(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/forecast")
    Call<ForecastWeatherModel> getForecastWeatherByString(@Query("q") String q, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/forecast")
    Call<ForecastWeatherModel> getForecastWeatherByCoords(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/uvi")
    Call<UVIndexModel> getCurrentUVIndex(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid);

    @GET("data/2.5/uvi/forecast")
    Call<List<UVIndexModel>> getForecastUVIndex(@Query("lat") double lat, @Query("lon") double lon, @Query("appid") String appid, @Query("cont") int cnt);
}
