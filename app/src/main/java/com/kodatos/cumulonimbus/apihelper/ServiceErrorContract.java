package com.kodatos.cumulonimbus.apihelper;

/**
 * Contains string constants for use in broadcasting error details from {@link com.kodatos.cumulonimbus.apihelper.SyncOWMService} to main screen
 */
public class ServiceErrorContract {

    public static final String BROADCAST_INTENT_FILTER = "com.kodatos.cumulonimbus.service_error_filter";
    public static final String ERROR_LOCATION = "location_error";
    public static final String ERROR_GEOCODER = "geocoding_error";
    public static final String ERROR_REVERSE_GEOCODER = "reverse_geocoding_error";
    public static final String ERROR_RESPONSE = "api_response_error";
    public static final String ERROR_DETAILS_REQUIRES_SERVICE_RESTART = "restart";
    public static final String ERROR_DETAILS_LOCATION_DISABLED = "location_disabled";
    public static final String ERROR_DETAILS_NULL = "null";
    public static final String ERROR_DETAILS_IO = "io";
}
