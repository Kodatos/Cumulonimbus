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

/**
 * Contains string constants for use in broadcasting error details from {@link com.kodatos.cumulonimbus.apihelper.SyncOWMService} to main screen
 */
public class ServiceErrorContract {

    public static final String BROADCAST_INTENT_FILTER = "com.kodatos.cumulonimbus.service_error_filter";
    public static final String ERROR_LOCATION = "location_error";
    public static final String ERROR_GEOCODER = "geocoding_error";
    public static final String ERROR_REVERSE_GEOCODER = "reverse_geocoding_error";
    public static final String ERROR_RESPONSE = "api_response_error";
    public static final String ERROR_GENERIC = "error";
    public static final String ERROR_DETAILS_NULL = "null";
    public static final String ERROR_DETAILS_IO = "io";

    public static final String SERVICE_ERROR_TYPE = "error_type";
    public static final String SERVICE_ERROR_DETAILS = "error_details";
}
