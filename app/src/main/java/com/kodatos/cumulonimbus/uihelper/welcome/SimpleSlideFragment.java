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
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.github.paolorotolo.appintro.ISlideSelectionListener;
import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.SimpleSlideLayoutBinding;

import java.util.Objects;

public class SimpleSlideFragment extends Fragment implements ISlidePolicy, ISlideBackgroundColorHolder, ISlideSelectionListener {

    private static final String SLIDE_BACKGROUND_COLOR = "slidebackcolor";
    private static final String SLIDE_TITLE = "slidetitle";
    private static final String SLIDE_DESC = "slidedesc";
    private static final String SLIDE_DRAWABLE = "slidedrawable";
    private SimpleSlideLayoutBinding mBinding;
    private boolean isSafeToProceed = true;

    public SimpleSlideFragment() {
    }

    public static SimpleSlideFragment newInstance(String title, String description, int drawableID, int color) {
        SimpleSlideFragment fragment = new SimpleSlideFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SLIDE_BACKGROUND_COLOR, color);
        bundle.putString(SLIDE_TITLE, title);
        bundle.putString(SLIDE_DESC, description);
        bundle.putInt(SLIDE_DRAWABLE, drawableID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.simple_slide_layout, container, false);
        Bundle arguments = Objects.requireNonNull(getArguments());
        mBinding.simpleSlideTitle.setText(arguments.getString(SLIDE_TITLE));
        mBinding.simpleSlideDescription.setText(arguments.getString(SLIDE_DESC));
        mBinding.simpleSlideImageView.setImageDrawable(getResources().getDrawable(arguments.getInt(SLIDE_DRAWABLE)));
        if (arguments.getInt(SLIDE_DRAWABLE) == R.drawable.ic_launcher_foreground) {
            mBinding.simpleSlideImageView.setScaleX(1.5f);
            mBinding.simpleSlideImageView.setScaleY(1.5f);
        }
        mBinding.getRoot().setBackgroundColor(arguments.getInt(SLIDE_BACKGROUND_COLOR));
        return mBinding.getRoot();
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 9987);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9987) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isSafeToProceed = true;
            }
        }
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getArguments().getInt(SLIDE_BACKGROUND_COLOR);
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        mBinding.getRoot().setBackgroundColor(backgroundColor);
        Window activityWindow = Objects.requireNonNull(getActivity()).getWindow();
        activityWindow.setStatusBarColor(backgroundColor);
        activityWindow.setNavigationBarColor(backgroundColor);
    }

    @Override
    public boolean isPolicyRespected() {
        return isSafeToProceed;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        Toast.makeText(getContext(), "Please grant the required permission!", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(this::requestLocationPermission, 1000);
    }

    @Override
    public void onSlideSelected() {
        if (getArguments().getInt(SLIDE_DRAWABLE) == R.drawable.ic_location_permission) {
            isSafeToProceed = false;
            requestLocationPermission();
        }
    }

    @Override
    public void onSlideDeselected() {

    }
}
