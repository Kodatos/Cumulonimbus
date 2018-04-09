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

package com.kodatos.cumulonimbus.uihelper.welcome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.WeatherAPIService;
import com.kodatos.cumulonimbus.apihelper.models.CurrentWeatherModel;
import com.kodatos.cumulonimbus.databinding.PreferenceSlideLayoutBinding;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PreferenceSlideFragment extends Fragment implements ISlideBackgroundColorHolder, ISlidePolicy, LoaderManager.LoaderCallbacks<String> {

    private static String LOG_TAG = "prefernce_slide";
    private PreferenceSlideLayoutBinding mBinding;
    private boolean isSafeToProceed = false;
    private int LOADER_ID = 3121;

    private Bundle arguments;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.preference_slide_layout, container, false);
        arguments = Objects.requireNonNull(getArguments());
        mBinding.getRoot().setBackgroundColor(arguments.getInt(KeyConstants.SLIDE_BACKGROUND_COLOR));
        mBinding.preferenceSlideTitle.setText(getString(R.string.welcome_title_3));
        mBinding.preferenceSlideCurrentLocationCheck.setOnClickListener(v -> {
            boolean previousCheck = mBinding.preferenceSlideCurrentLocationCheck.isChecked();
            mBinding.preferenceSlideCurrentLocationCheck.setChecked(!previousCheck);
            if (previousCheck)
                mBinding.preferenceSlideCustomLocationInput.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
            else
                mBinding.preferenceSlideCustomLocationInput.setInputType(InputType.TYPE_NULL);
        });
        mBinding.preferenceSlideSyncButton.setOnClickListener(v -> {
            mBinding.preferenceSlideValidatingProgressBar.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(LOADER_ID, null, this);
        });
        return mBinding.getRoot();
    }

    @Override
    public int getDefaultBackgroundColor() {
        return arguments.getInt(KeyConstants.SLIDE_BACKGROUND_COLOR);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        mBinding.getRoot().setBackgroundColor(backgroundColor);
        Window activityWindow = Objects.requireNonNull(getActivity()).getWindow();
        activityWindow.setStatusBarColor(backgroundColor);
        activityWindow.setNavigationBarColor(backgroundColor);
    }

    @Override
    public boolean isPolicyRespected() {
        return isSafeToProceed;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        if (!isSafeToProceed) {
            Toast.makeText(getContext(), "Please validate settings before continuing", Toast.LENGTH_LONG).show();
            mBinding.preferenceSlideSyncButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_effect));
        }
    }

    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID)
            return new SettingsValidator(getContext(), !mBinding.preferenceSlideCurrentLocationCheck.isChecked(), mBinding.preferenceSlideCustomLocationInput.getText().toString(), getString(R.string.owm_api_key));
        else
            throw new IllegalArgumentException();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if ("no_internet".equals(data)) {
            Snackbar.make(mBinding.getRoot(), "Please enable your internet connection and try again", Snackbar.LENGTH_SHORT).show();
        } else if ("invalid_city".equals(data)) {
            Snackbar.make(mBinding.getRoot(), "You may have entered a non-existing city. Try again", Snackbar.LENGTH_SHORT).show();
        } else if ("valid".equals(data)) {
            //Check location service
            Log.d(LOG_TAG, "checking_location_services");
            FusedLocationProviderClient mFusedClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getContext()));
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1).setInterval(100).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(getContext());
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            //If location disabled, ask to enable
            task.addOnFailureListener(e -> {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(PreferenceSlideFragment.this.getActivity(), 2114);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                }
            });
            //If location enabled, request location once to get latest location data.
            task.addOnSuccessListener(locationSettingsResponse -> mFusedClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    mFusedClient.removeLocationUpdates(this);
                    isSafeToProceed = true;         //Everything is valid. User can now finish intro.

                    Log.d(LOG_TAG, "valid");
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
                    sp.edit().putString(getString(R.string.pref_custom_location_key), mBinding.preferenceSlideCurrentLocationCheck.isChecked() ? "Enter City Here" : mBinding.preferenceSlideCustomLocationInput.getText().toString())
                            .putBoolean(getString(R.string.pref_curr_location_key), mBinding.preferenceSlideCurrentLocationCheck.isChecked())
                            .putBoolean(KeyConstants.FIRST_TIME_RUN, false)         //Intro completed successfully
                            .apply();

                    Button syncButton = PreferenceSlideFragment.this.mBinding.preferenceSlideSyncButton;
                    syncButton.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color._01d_background));
                    syncButton.setTextColor(Color.WHITE);
                }
            }, null));
        }
        mBinding.preferenceSlideValidatingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
    }

    private static class SettingsValidator extends AsyncTaskLoader<String> {

        private boolean isCustomLocationEnabled;
        private String custom_location;
        private String api_key;

        SettingsValidator(Context context, boolean isCustomLocationEnabled, String custom_location, String api_key) {
            super(context);
            this.isCustomLocationEnabled = isCustomLocationEnabled;
            this.custom_location = custom_location;
            this.api_key = api_key;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public String loadInBackground() {
            //Check internet
            Log.d(LOG_TAG, "validating");
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
            if (info == null || !info.isConnectedOrConnecting()) {
                Log.d(LOG_TAG, "no_internet");
                return "no_internet";
            }
            Log.d(LOG_TAG, "internet_fine");
            //If user entered custom location, validate it.
            if (isCustomLocationEnabled) {
                Log.d(LOG_TAG, "custom_location");
                Retrofit currentWeatherRetrofit = new Retrofit.Builder()
                        .baseUrl("http://api.openweathermap.org/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                WeatherAPIService checkerService = currentWeatherRetrofit.create(WeatherAPIService.class);
                Call<CurrentWeatherModel> currentWeatherModelCall = checkerService.getCurrentWeatherByString(custom_location, api_key, "metric");
                try {
                    Response<CurrentWeatherModel> response = currentWeatherModelCall.execute();
                    if (!response.isSuccessful()) {
                        JSONObject jsonError = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                        String errorCode = String.valueOf(jsonError.getInt("cod"));
                        if ("404".equals(errorCode) || "400".equals(errorCode)) {
                            Log.d(LOG_TAG, "invalid_city");
                            return "invalid_city";
                        }
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            return "valid";
        }
    }
}