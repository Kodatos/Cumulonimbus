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

package com.kodatos.cumulonimbus.utils;

public class KeyConstants {

    private KeyConstants(){}

    //Current weather preference keys
    public static final String CURRENT_WEATHER_SUNRISE_KEY = "sunrise_in_utc";
    public static final String CURRENT_WEATHER_SUNSET_KEY = "sunset_in_utc";
    public static final String CURRENT_WEATHER_ICON_ID_KEY = "icon_id_for_back_color";
    public static final String CURRENT_WEATHER_VISIBILITY_KEY = "visibility_in_m";
    public static final String LOCATION_NAME_KEY = "location_name_key";
    public static final String LAST_UPDATE_DATE_KEY = "LUDOH4F";

    public static final String CACHED_COORDS_KEY = "from_coords";
    public static final String CACHED_ADDRESS_KEY = "to_address";

    //Detail activity transition keys
    public static final String FORECAST_IMAGE_TRANSITION_KEY = "forecast_image_transition_name";
    public static final String WEATHER_DETAIL_PARCEL_NAME = "weather_details_data_models";
    public static final String WEATHER_DETAIL_DAY_NAME = "weather_details_day";

    //First time run preference key
    public static final String FIRST_TIME_RUN = "boolean_first_time";

    //Strings for Location List Preferences
    public static final String LOCATION_PREFERENCES_NAME = "location_shared_pref";
    public static final String CHOSEN_CUSTOM_LOCATION = "chosen_custom_location";
    public static final String CHOSEN_COORDINATES = "custom_location_coordinate";

    //Graphs Activity bundle keys
    public static final String TEMPERATURE_CHART_DATA = "temperature_chart_data";
    public static final String WIND_CHART_DATA = "wind_chart_data";
    public static final String RAIN_CHART_DATA = "rain_chart_data";
}
