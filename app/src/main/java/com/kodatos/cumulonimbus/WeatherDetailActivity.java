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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ActivityWeatherDetailBinding;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;
import com.kodatos.cumulonimbus.uihelper.adapters.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.KeyConstants;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

public class WeatherDetailActivity extends AppCompatActivity {

    private ActivityWeatherDetailBinding mBinding;
    private ArrayList<DBModel> mModels;
    private boolean justOpened = true;
    private int previousBackgroundColor;

    private int day;        // Day for which details are shown
    private boolean metric;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_weather_detail);
        postponeEnterTransition();

        //region Region : enter exit transitions
        Fade fade = new Fade();
        fade.setDuration(500);
        fade.excludeTarget(mBinding.toolbar, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        getWindow().setReenterTransition(fade);

        Transition sharedElementTransition = TransitionInflater.from(this).inflateTransition(R.transition.detail_shared_transition);
        getWindow().setSharedElementEnterTransition(sharedElementTransition);
        getWindow().setSharedElementExitTransition(sharedElementTransition);
        getWindow().setSharedElementReturnTransition(sharedElementTransition);
        getWindow().setSharedElementReenterTransition(sharedElementTransition);
        //endregion

        setSupportActionBar(mBinding.toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        /*Enable animation on binding data
        mBinding.addOnRebindCallback(new OnRebindCallback<ActivityWeatherDetailBinding>(){
            @Override
            public boolean onPreBind(ActivityWeatherDetailBinding binding) {
                TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
                return super.onPreBind(binding);
            }
        });*/

        mBinding.weatherImageView.setTransitionName(getIntent().getStringExtra(KeyConstants.FORECAST_IMAGE_TRANSITION_KEY));
        mModels = getIntent().getParcelableArrayListExtra(KeyConstants.WEATHER_DETAIL_PARCEL_NAME);
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.timelineRecyclerView.setLayoutManager(lm);
        initialize();
    }

    private void initialize() {
        day = getIntent().getIntExtra(KeyConstants.WEATHER_DETAIL_DAY_NAME, 0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        metric = sp.getBoolean(getString(R.string.pref_metrics_key), true);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(sp.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        int initialExpandedPosition = calendar.get(Calendar.HOUR_OF_DAY) / 3;
        bindData(initialExpandedPosition);

        List<String> iconIds = new ArrayList<>();
        List<String> temperatures = new ArrayList<>();
        for (DBModel model : mModels) {
            iconIds.add(model.getIcon_id());
            temperatures.add(model.getTemp());
            //Carry over UV Index data available from one model to all 8 models
            model.setUvIndex(mModels.get(initialExpandedPosition).getUvIndex());
        }
        TimelineRecyclerViewAdapter adapter = new TimelineRecyclerViewAdapter(iconIds, temperatures, initialExpandedPosition, this::onTimelineItemClick);
        mBinding.timelineRecyclerView.setAdapter(adapter);

        startPostponedEnterTransition();
    }

    private void bindData(int position) {
        DetailActivityDataModel bindingModel = MiscUtils.getDetailModelFromDBModel(this, mModels.get(position), day, metric);
        mBinding.setDataModel(bindingModel);

        int currentColor = MiscUtils.getBackgroundColorForIconID(this, mModels.get(position).getIcon_id());
        if (!justOpened)
            changeBackgroundColorWithAnimation(currentColor);
        else {
            changeBackgroundColor(currentColor);
            previousBackgroundColor = currentColor;
            justOpened = false;
        }
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, currentColor);
        setTaskDescription(taskDescription);
    }

    private void changeBackgroundColor(int updatedBackgroundColor) {
        mBinding.toolbar.setBackgroundColor(updatedBackgroundColor);
        getWindow().getDecorView().setBackgroundColor(updatedBackgroundColor);
        getWindow().setStatusBarColor(updatedBackgroundColor);
        getWindow().setNavigationBarColor(updatedBackgroundColor);
    }

    private void changeBackgroundColorWithAnimation(int updatedBackgroundColor) {
        if (updatedBackgroundColor != previousBackgroundColor) {
            ValueAnimator backgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), previousBackgroundColor, updatedBackgroundColor);
            backgroundColorAnimator.setDuration(500);
            backgroundColorAnimator.addUpdateListener(animation -> {
                int currentColor = (int) animation.getAnimatedValue();
                changeBackgroundColor(currentColor);
            });
            backgroundColorAnimator.start();
        }
        previousBackgroundColor = updatedBackgroundColor;
    }

    //Click listener for timeline recyclerview
    public void onTimelineItemClick(int position) {
        //The wind direction icon is rotated through the shortest angle before binding new data
        float fromAngle = mBinding.windDirectionImageView.getRotation();
        float toAngle = Float.parseFloat(mModels.get(position).getWind().split("/")[1]);
        float angleOffset = toAngle - fromAngle;
        if (angleOffset > 180)
            angleOffset -= 360;
        else if (angleOffset < -180)
            angleOffset += 360;
        mBinding.windDirectionImageView.animate().rotationBy(angleOffset).setDuration(200).setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                //Animate weather image if not the same as previous
                if (MiscUtils.getResourceIDForIconID(WeatherDetailActivity.this, mModels.get(position).getIcon_id()) != mBinding.getDataModel().weatherImageID)
                    mBinding.weatherImageView.animate().alpha(0.0f).setDuration(200);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                bindData(position);
                mBinding.weatherImageView.animate().alpha(1.0f).setDuration(200); //Harmless if not animated to 0 above
            }
        });
    }
}
