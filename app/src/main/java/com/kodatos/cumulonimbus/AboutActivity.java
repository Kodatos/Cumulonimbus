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
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.kodatos.cumulonimbus.databinding.ActivityAboutBinding;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import de.psdev.licensesdialog.LicensesDialogFragment;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding mBinding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        setSupportActionBar(mBinding.toolbarAbout);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        Window window = getWindow();

        Slide transition = new Slide();
        transition.setDuration(200);
        transition.setSlideEdge(Gravity.END);
        window.setEnterTransition(transition);
        window.setExitTransition(transition);
        window.setReturnTransition(transition);
        window.setReenterTransition(transition);

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        LicensesDialogFragment licensesFragment = new LicensesDialogFragment.Builder(this)
                .setNotices(R.raw.notices)
                .setIncludeOwnLicense(true).build();
        mBinding.licensesLayout.setOnClickListener(v -> licensesFragment.show(getSupportFragmentManager(), null));

    }
}
