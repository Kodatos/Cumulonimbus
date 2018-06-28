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

package com.kodatos.cumulonimbus;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.kodatos.cumulonimbus.utils.KeyConstants;

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
        findPreference(customLocationKey).setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
            startActivityForResult(intent, 9110);
            return true;
        });
        findPreference(customLocationKey).setSummary(getPreferenceScreen().getSharedPreferences().getString(KeyConstants.CHOSEN_CUSTOM_LOCATION, getString(R.string.pref_custom_location_def)));
        findPreference(customLocationKey).setEnabled(!getPreferenceScreen().getSharedPreferences().getBoolean(currentLocationKey, true));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(currentLocationKey)) {
            findPreference(customLocationKey).setEnabled(!getPreferenceScreen().getSharedPreferences().getBoolean(currentLocationKey, true));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9110 && resultCode == Activity.RESULT_OK) {
            findPreference(customLocationKey).setSummary(getPreferenceScreen().getSharedPreferences().getString(KeyConstants.CHOSEN_CUSTOM_LOCATION, getString(R.string.pref_custom_location_def)));
            getActivity().setResult(Activity.RESULT_OK);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
}
