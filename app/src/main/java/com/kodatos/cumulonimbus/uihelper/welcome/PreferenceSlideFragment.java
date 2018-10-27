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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.github.paolorotolo.appintro.ISlideSelectionListener;
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
import com.google.android.material.snackbar.Snackbar;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.PreferenceSlideLayoutBinding;
import com.kodatos.cumulonimbus.uihelper.NewLocationDialogFragment;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class PreferenceSlideFragment extends Fragment implements ISlideBackgroundColorHolder, ISlideSelectionListener, ISlidePolicy {

    private static String LOG_TAG = "prefernce_slide";
    private PreferenceSlideLayoutBinding mBinding;
    private boolean isSafeToProceed = false;

    private static final String SLIDE_BACKGROUND_COLOR = "back_color";

    private SharedPreferences defaultSharedPreferences;

    private Bundle arguments;

    public PreferenceSlideFragment() {
    }

    public static PreferenceSlideFragment newInstance(int color) {
        PreferenceSlideFragment preferenceSlideFragment = new PreferenceSlideFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(SLIDE_BACKGROUND_COLOR, color);
        preferenceSlideFragment.setArguments(arguments);
        return preferenceSlideFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.preference_slide_layout, container, false);
        arguments = Objects.requireNonNull(getArguments());
        mBinding.getRoot().setBackgroundColor(arguments.getInt(SLIDE_BACKGROUND_COLOR));
        mBinding.preferenceSlideTitle.setText(getString(R.string.welcome_title_3));
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return mBinding.getRoot();
    }

    private void setUpInteractions() {
        boolean currentLocationEnabled = defaultSharedPreferences.getBoolean(getString(R.string.pref_curr_location_key), false);
        mBinding.preferenceSlideCurrentLocationCheck.setChecked(currentLocationEnabled);
        mBinding.preferenceSlideCustomLocationInput.setEnabled(!currentLocationEnabled);

        mBinding.preferenceSlideCurrentLocationCheck.setOnClickListener(v -> {
            boolean previousCheck = mBinding.preferenceSlideCurrentLocationCheck.isChecked();
            mBinding.preferenceSlideCurrentLocationCheck.setChecked(!previousCheck);
            if (previousCheck)
                mBinding.preferenceSlideCustomLocationInput.setEnabled(true);
            else {
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 9981);
                }
                mBinding.preferenceSlideCustomLocationInput.setEnabled(false);
            }
        });
        mBinding.preferenceSlideCustomLocationInput.setOnClickListener(v -> {
            NewLocationDialogFragment fragment = new NewLocationDialogFragment();
            fragment.setListener(((verifiedLocation, verifiedCoordinates) -> {
                defaultSharedPreferences.edit()
                        .putString(KeyConstants.CHOSEN_CUSTOM_LOCATION, verifiedLocation)
                        .putString(KeyConstants.CHOSEN_COORDINATES, verifiedCoordinates)
                        .apply();
                Objects.requireNonNull(getContext())
                        .getSharedPreferences(KeyConstants.LOCATION_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                        .putString(verifiedLocation, verifiedCoordinates)
                        .apply();
            }));
            fragment.show(getFragmentManager(), "NEW_LOCATION_FRAGMENT");
        });
        mBinding.preferenceSlideSyncButton.setOnClickListener(v -> validateSettings());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(getContext(), "Please provide location permission for current location access", Toast.LENGTH_SHORT)
                    .show();
            mBinding.preferenceSlideCurrentLocationCheck.setChecked(false);
            mBinding.preferenceSlideCustomLocationInput.setEnabled(true);
        }
    }

    @Override
    public int getDefaultBackgroundColor() {
        return arguments.getInt(SLIDE_BACKGROUND_COLOR);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        mBinding.getRoot().setBackgroundColor(backgroundColor);
        Window activityWindow = Objects.requireNonNull(getActivity()).getWindow();
        activityWindow.setStatusBarColor(backgroundColor);
        activityWindow.setNavigationBarColor(backgroundColor);
    }

    @SuppressLint("MissingPermission")
    private void validateSettings() {
        ConnectivityManager cm = (ConnectivityManager) Objects.requireNonNull(getContext()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        if (info == null || !info.isConnected()) {
            Snackbar.make(mBinding.getRoot(), "Please enable your internet connection and try again", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (mBinding.preferenceSlideCurrentLocationCheck.isChecked()) {
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
                    setNowSafeToProceed();
                }
            }, null));
        } else {
            if (!defaultSharedPreferences.contains(KeyConstants.CHOSEN_CUSTOM_LOCATION)) {
                Snackbar.make(mBinding.getRoot(), "Please enter any other location if you do not want to use your current", Snackbar.LENGTH_SHORT).show();
            } else {
                setNowSafeToProceed();
            }
        }
    }

    @Override
    public boolean isPolicyRespected() {
        return isSafeToProceed;
    }

    private void setNowSafeToProceed() {
        isSafeToProceed = true;         //Everything is valid. User can now finish intro.
        Log.d(LOG_TAG, "valid");
        Button syncButton = PreferenceSlideFragment.this.mBinding.preferenceSlideSyncButton;
        syncButton.setBackgroundTintList(ContextCompat.getColorStateList(Objects.requireNonNull(getContext()), R.color._01d_background));
        defaultSharedPreferences.edit()
                .putBoolean(getString(R.string.pref_curr_location_key), mBinding.preferenceSlideCurrentLocationCheck.isChecked())
                .putBoolean(KeyConstants.FIRST_TIME_RUN, false)         //Intro completed successfully
                .apply();
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        if (!isSafeToProceed) {
            Toast.makeText(getContext(), "Please validate settings before continuing", Toast.LENGTH_LONG).show();
            mBinding.preferenceSlideSyncButton.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shake_effect));
        }
    }

    @Override
    public void onSlideSelected() {
        setUpInteractions();
    }

    @Override
    public void onSlideDeselected() {

    }
}