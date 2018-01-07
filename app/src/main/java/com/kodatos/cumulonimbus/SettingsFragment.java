package com.kodatos.cumulonimbus;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    String currentLocationKey;
    String customLocationKey;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
        currentLocationKey = getString(R.string.pref_curr_location_key);
        customLocationKey = getString(R.string.pref_custom_location_key);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(customLocationKey).setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.pref_custom_location_key), ""));
        findPreference(customLocationKey).setEnabled(!getPreferenceScreen().getSharedPreferences().getBoolean(currentLocationKey, true));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(customLocationKey)) {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
            if (!getPreferenceScreen().getSharedPreferences().getBoolean(currentLocationKey, true))
                Toast.makeText(getActivity(), "Updated location. Please refresh at main screen", Toast.LENGTH_SHORT).show();
        } else if (key.equals(currentLocationKey)) {
            findPreference(customLocationKey).setEnabled(!getPreferenceScreen().getSharedPreferences().getBoolean(currentLocationKey, true));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
