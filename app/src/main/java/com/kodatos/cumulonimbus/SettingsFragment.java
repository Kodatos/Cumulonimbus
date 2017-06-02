package com.kodatos.cumulonimbus;



import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_custom_location_key)).setSummary(getPreferenceScreen().getSharedPreferences().getString(getString(R.string.pref_custom_location_key), ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_custom_location_key))){
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
