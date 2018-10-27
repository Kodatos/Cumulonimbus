package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.NewLocationDialogLayoutBinding;
import com.kodatos.cumulonimbus.utils.CityValidatorUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;

public class NewLocationDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<String[]> {

    private NewLocationDialogLayoutBinding mBinding;
    private int LOADER_ID = 129;
    private boolean isInVerifyMode = false;
    private String verifiedLocation;
    private String verifiedCoordinates;

    private LocationVerifiedListener listener;

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
                listener.onLocationVerified(verifiedLocation, verifiedCoordinates);
                dismiss();
            } else
                getLoaderManager().restartLoader(LOADER_ID, null, NewLocationDialogFragment.this);
        });
        return mBinding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(LocationVerifiedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] data) {
        mBinding.verifyLocationGroup.setVisibility(View.VISIBLE);
        if (data == null) {
            mBinding.verifyLocationTitle.setText(R.string.location_search_failed);
            mBinding.geocodedLocationText.setText(R.string.location_search_failed_desc);
        } else {
            mBinding.verifyLocationTitle.setText(R.string.location_verify_title);
            verifiedLocation = data[0];
            verifiedCoordinates = data[1] + "/" + data[2];
            String coordsText = "Coordinates: " + data[1] + ", " + data[2];
            String displayText = data[0] + "\n" + coordsText;
            mBinding.geocodedLocationText.setText(displayText);
            mBinding.positiveAction.setText("Accept");
            isInVerifyMode = true;
        }
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID)
            return new LocationGeocoder(getContext(), mBinding.locationEditText.getText().toString(), getString(R.string.owm_api_key));
        else
            throw new UnsupportedOperationException();
    }

    public interface LocationVerifiedListener {
        void onLocationVerified(String verifiedLocation, String verifiedCoordinates);
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
            onContentChanged();
        }

        @Override
        protected void onStartLoading() {
            if (takeContentChanged())
                forceLoad();
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        @Override
        public String[] loadInBackground() {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            String[] returnData = new String[3];
            try {
                List<Address> addresses = geocoder.getFromLocationName(location, 1);
                if (addresses.size() == 0)
                    throw new IOException();
                Address address = addresses.get(0);
                if (address == null)
                    throw new IOException();
                returnData[0] = (address.getSubLocality() != null ? address.getSubLocality() + ", " : "") + address.getLocality() + ", " + address.getCountryName();
                DecimalFormat df = new DecimalFormat("#0.####");
                returnData[1] = df.format(address.getLatitude());
                returnData[2] = df.format(address.getLongitude());
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
