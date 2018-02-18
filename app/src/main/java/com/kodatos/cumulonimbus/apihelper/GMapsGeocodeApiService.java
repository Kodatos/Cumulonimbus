package com.kodatos.cumulonimbus.apihelper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GMapsGeocodeApiService {

    @GET("geocode/json")
    Call<ResponseBody> getCoordsFromAddress(@Query("address") String address);

    @GET("geocode/json")
    Call<ResponseBody> getAddressFromCoords(@Query("latlng") String latLng);
}
