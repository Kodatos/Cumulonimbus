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

import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.kodatos.cumulonimbus.uihelper.welcome.PreferenceSlideFragment;
import com.kodatos.cumulonimbus.uihelper.welcome.SimpleSlideFragment;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class WelcomeActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SimpleSlideFragment.newInstance(getString(R.string.welcome_title_1), getString(R.string.welcome_description_1), R.drawable.ic_launcher_foreground, ContextCompat.getColor(this, R.color.first_slide_background)));
        addSlide(SimpleSlideFragment.newInstance(getString(R.string.welcome_title_2), getString(R.string.welcome_description_2), R.drawable.ic_location_permission, ContextCompat.getColor(this, R.color.second_slide_background)));

        addSlide(PreferenceSlideFragment.newInstance(ContextCompat.getColor(this, R.color.third_slide_background)));

        addSlide(SimpleSlideFragment.newInstance(getString(R.string.welcome_title_4), getString(R.string.welcome_description_4), R.drawable.ic_sun_emoticon, ContextCompat.getColor(this, R.color._01d_background)));
        //askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        setColorTransitionsEnabled(true);
        showSkipButton(false);
        setWizardMode(true);

    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        if (newFragment == null) {
            setResult(RESULT_OK);
            finish();
        }
    }

}
