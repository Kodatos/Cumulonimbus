package com.kodatos.cumulonimbus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ActivityWeatherDetailBinding;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;
import com.kodatos.cumulonimbus.uihelper.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

public class WeatherDetailActivity extends AppCompatActivity implements TimelineRecyclerViewAdapter.TimelineItemClickListener {

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

        Fade fade = new Fade();
        fade.setDuration(1000);
        fade.excludeTarget(mBinding.toolbar, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        getWindow().setReenterTransition(fade);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Enable animation on binding data
        mBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public boolean onPreBind(ViewDataBinding binding) {
                TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
                return super.onPreBind(binding);
            }
        });

        mBinding.weatherImageView.setTransitionName(getIntent().getStringExtra(getString(R.string.forecats_image_transistion_key)));
        mModels = Parcels.unwrap(getIntent().getParcelableExtra(getString(R.string.weather_detail_parcel_name)));
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.timelineRecyclerView.setLayoutManager(lm);
        initialize();
    }

    private void initialize() {
        day = getIntent().getIntExtra(getString(R.string.weather_detail_day_name), 0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        metric = sp.getBoolean(getString(R.string.pref_metrics_key), true);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(sp.getLong(getString(R.string.last_update_date_key),0)));
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
        TimelineRecyclerViewAdapter adapter = new TimelineRecyclerViewAdapter(this, iconIds, temperatures, initialExpandedPosition);
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
            backgroundColorAnimator.setDuration(1000);
            backgroundColorAnimator.addUpdateListener(animation -> {
                int currentColor = (int) animation.getAnimatedValue();
                changeBackgroundColor(currentColor);
            });
            backgroundColorAnimator.start();
        }
        previousBackgroundColor = updatedBackgroundColor;
    }

    @Override
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
