package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.NewLocationDialogLayoutBinding;
import com.kodatos.cumulonimbus.utils.CityValidatorUtil;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class NewLocationDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<String[]> {

    private NewLocationDialogLayoutBinding mBinding;
    private int LOADER_ID = 129;
    private boolean isInVerifyMode = false;
    private String verifiedLocation;
    private String verifiedCoordinates;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            dismiss();
            return null;
        }
        mBinding = DataBindingUtil.inflate(inflater, R.layout.new_location_dialog_layout, container, false);
        mBinding.negativeAction.setOnClickListener(v -> dismiss());
        mBinding.positiveAction.setOnClickListener(v -> {
            if (isInVerifyMode) {
                SharedPreferences locationSharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(KeyConstants.LOCATION_PREFERENCES_NAME, Context.MODE_PRIVATE);
                locationSharedPreferences.edit().putString(verifiedLocation, verifiedCoordinates).apply();
                dismiss();
            } else
                getLoaderManager().initLoader(LOADER_ID, null, NewLocationDialogFragment.this);
        });
        return mBinding.getRoot();
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID)
            return new LocationGeocoder(getContext(), mBinding.locationEditText.getText().toString(), getString(R.string.owm_api_key));
        else
            throw new UnsupportedOperationException();
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
        mBinding.verifyLocationGroup.setVisibility(View.VISIBLE);
        if (data == null) {
            mBinding.verifyLocationTitle.setText(R.string.location_search_failed);
            mBinding.geocodedLocationText.setText(R.string.location_search_failed_desc);
        } else {
            mBinding.verifyLocationTitle.setText("Verify:");
            verifiedLocation = data[0];
            verifiedCoordinates = data[1] + "/" + data[2];
            String coordsText = "Coordinates: " + data[1] + ", " + data[2];
            String displayText = data[0] + "\n" + coordsText;
            mBinding.geocodedLocationText.setText(displayText);
            isInVerifyMode = true;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {
        verifiedCoordinates = null;
        verifiedLocation = null;
        isInVerifyMode = false;
    }


    private static class LocationGeocoder extends AsyncTaskLoader<String[]> {

        private String location;
        private String api_key;

        LocationGeocoder(Context context, String location, String api_key) {
            super(context);
            this.location = location;
            this.api_key = api_key;
        }

        @Override
        public String[] loadInBackground() {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            String[] returnData = new String[3];
            try {
                Address address = geocoder.getFromLocationName(location, 1).get(0);
                if (address == null)
                    throw new IOException();
                returnData[0] = (address.getLocality() != null ? address.getLocality() + ", " : "") + address.getCountryName();
                returnData[1] = String.valueOf(address.getLatitude());
                returnData[2] = String.valueOf(address.getLongitude());
                return returnData;
            } catch (IOException e) {
                if (!Geocoder.isPresent())
                    Log.d(getClass().getSimpleName(), "No geocoder present");
                Log.d(getClass().getSimpleName(), "Geocoder failed. Using weather response");
                returnData = CityValidatorUtil.checkIfStringValid(location, api_key);
                return returnData;
            }
        }
    }
}
