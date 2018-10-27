package com.kodatos.cumulonimbus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.kodatos.cumulonimbus.databinding.LocationPickerLayoutBinding;
import com.kodatos.cumulonimbus.uihelper.NewLocationDialogFragment;
import com.kodatos.cumulonimbus.uihelper.SwipeToDeleteHelper;
import com.kodatos.cumulonimbus.uihelper.adapters.LocationRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.KeyConstants;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LocationPickerActivity extends AppCompatActivity implements LocationRecyclerViewAdapter.LocationClickListener {

    private LocationPickerLayoutBinding mBinding;
    private SharedPreferences locationSharedPreferences;
    private SharedPreferences defaultSharedPreferences;
    private LocationRecyclerViewAdapter adapter;

    private boolean hasCustomLocationChanged = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.location_picker_layout);
        setSupportActionBar(mBinding.toolbarLocationPicker);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color._09d_background));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color._09d_background));

        locationSharedPreferences = getSharedPreferences(KeyConstants.LOCATION_PREFERENCES_NAME, MODE_PRIVATE);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        List<String> adapterData = new ArrayList<>(locationSharedPreferences.getAll().keySet());
        if (adapterData.isEmpty())
            mBinding.listEmptyTextView.setVisibility(View.VISIBLE);
        String chosenCustomLocation = defaultSharedPreferences.getString(KeyConstants.CHOSEN_CUSTOM_LOCATION, null);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mBinding.locationPickerRecyclerView.setLayoutManager(lm);
        adapter = new LocationRecyclerViewAdapter(adapterData, adapterData.indexOf(chosenCustomLocation), this);
        mBinding.locationPickerRecyclerView.setAdapter(adapter);
        ItemTouchHelper swipeToDeleteHelper = new ItemTouchHelper(new SwipeToDeleteHelper(this) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String removedKey = adapter.removeLocation(viewHolder.getAdapterPosition());
                locationSharedPreferences.edit().remove(removedKey).apply();
                if (locationSharedPreferences.getAll().isEmpty()) {
                    defaultSharedPreferences.edit()
                            .remove(KeyConstants.CHOSEN_CUSTOM_LOCATION)
                            .remove(KeyConstants.CHOSEN_COORDINATES)
                            .apply();
                    mBinding.listEmptyTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        Toast.makeText(this, "Swipe Left to Delete!", Toast.LENGTH_SHORT).show();
        swipeToDeleteHelper.attachToRecyclerView(mBinding.locationPickerRecyclerView);
        mBinding.locationPickerFab.setOnClickListener(v -> showAddLocationDialog());
    }

    private void showAddLocationDialog() {
        NewLocationDialogFragment fragment = new NewLocationDialogFragment();
        fragment.setListener((location, coordinates) -> {
            if (locationSharedPreferences.contains(location)) {
                Snackbar.make(mBinding.locationPickerCoordinatorLayout, "This location already exists!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (mBinding.listEmptyTextView.getVisibility() == View.VISIBLE)
                mBinding.listEmptyTextView.setVisibility(View.GONE);
            locationSharedPreferences.edit().putString(location, coordinates).apply();
            adapter.addLocation(location);
        });
        fragment.show(getSupportFragmentManager(), "NEW_LOCATION_DIALOG");
    }

    @Override
    public void onLocationClick(String key) {
        defaultSharedPreferences.edit()
                .putString(KeyConstants.CHOSEN_CUSTOM_LOCATION, key)
                .putString(KeyConstants.CHOSEN_COORDINATES, locationSharedPreferences.getString(key, ""))
                .apply();
        if (!hasCustomLocationChanged) {
            hasCustomLocationChanged = true;
            setResult(RESULT_OK);
        }
    }

    @Override
    protected void onDestroy() {
        adapter.unregisterCallback();
        super.onDestroy();
    }
}
