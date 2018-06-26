package com.kodatos.cumulonimbus;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import com.kodatos.cumulonimbus.databinding.LocationPickerLayoutBinding;
import com.kodatos.cumulonimbus.uihelper.LocationRecyclerViewAdapter;
import com.kodatos.cumulonimbus.uihelper.NewLocationDialogFragment;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

public class LocationPickerActivity extends AppCompatActivity implements LocationRecyclerViewAdapter.LocationClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private LocationPickerLayoutBinding mBinding;
    private SharedPreferences locationSharedPreferences;
    private SharedPreferences weatherSharedPreferences;
    private LocationRecyclerViewAdapter adapter;

    private boolean hasCustomLocationChanged = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.location_picker_layout);
        setSupportActionBar(mBinding.toolbarLocationPicker);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        locationSharedPreferences = getSharedPreferences(KeyConstants.LOCATION_PREFERENCES_NAME, MODE_PRIVATE);
        weatherSharedPreferences = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

        List<String> adapterData = new ArrayList<>(locationSharedPreferences.getAll().keySet());
        if (adapterData.isEmpty())
            mBinding.listEmptyTextView.setVisibility(View.VISIBLE);
        String chosenCustomLocation = weatherSharedPreferences.getString(KeyConstants.CHOSEN_CUSTOM_LOCATION, null);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.locationPickerRecyclerView.setLayoutManager(lm);
        adapter = new LocationRecyclerViewAdapter(adapterData, adapterData.indexOf(chosenCustomLocation), this);
        mBinding.locationPickerRecyclerView.setAdapter(adapter);
        mBinding.locationPickerFab.setOnClickListener(v -> showAddLocationDialog());
    }

    private void showAddLocationDialog() {
        new NewLocationDialogFragment().show(getSupportFragmentManager(), "NEW_LOCATION_DIALOG");
    }

    @Override
    public void onLocationClick(String key) {
        weatherSharedPreferences.edit()
                .putString(KeyConstants.CHOSEN_CUSTOM_LOCATION, key)
                .putString(KeyConstants.CHOSEN_COORDINATES, locationSharedPreferences.getString(key, ""))
                .apply();
        if (!hasCustomLocationChanged) {
            hasCustomLocationChanged = true;
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        adapter.addLocation(key);
    }
}
