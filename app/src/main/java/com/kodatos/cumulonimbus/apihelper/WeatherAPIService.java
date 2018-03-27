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
    Call<CurrentWeatherModel> getCurrentWeatherByCoords(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/forecast")
    Call<ForecastWeatherModel> getForecastWeatherByString(@Query("q") String q, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/forecast")
    Call<ForecastWeatherModel> getForecastWeatherByCoords(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String appid, @Query("units") String units);

    @GET("data/2.5/uvi")
    Call<UVIndexModel> getCurrentUVIndex(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String appid);

    @GET("data/2.5/uvi/forecast")
    Call<List<UVIndexModel>> getForecastUVIndex(@Query("lat") String lat, @Query("lon") String lon, @Query("appid") String appid, @Query("cont") int cnt);
}
