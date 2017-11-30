package com.kodatos.cumulonimbus;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Fade;
import android.util.Log;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ActivityWeatherDetailBinding;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;
import com.kodatos.cumulonimbus.uihelper.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.parceler.Parcels;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class WeatherDetailActivity extends AppCompatActivity {

    private ActivityWeatherDetailBinding mBinding;
    private DBModel mModel;

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
        mBinding.weatherImageView.setTransitionName(getIntent().getStringExtra(getString(R.string.forecats_image_transistion_key)));
        mModel = Parcels.unwrap(getIntent().getParcelableExtra(getString(R.string.weather_detail_parcel_name)));
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.timelineRecyclerView.setLayoutManager(lm);
        bindData();
    }

    private void bindData(){
        int day = getIntent().getIntExtra(getString(R.string.weather_detail_day_name), 0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean metric = sp.getBoolean(getString(R.string.pref_metrics_key), true);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(sp.getLong(getString(R.string.last_update_date_key),0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        int forecastToDisplayIndex = calendar.get(Calendar.HOUR_OF_DAY)/3;
        int currentColor = MiscUtils.getBackgroundColorForIconID(this, mModel.getIcon_id().split("/")[forecastToDisplayIndex]);
        mBinding.toolbar.setBackgroundColor(currentColor);
        getWindow().getDecorView().setBackgroundColor(currentColor);
        getWindow().setStatusBarColor(currentColor);
        getWindow().setNavigationBarColor(currentColor);
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, currentColor);
        setTaskDescription(taskDescription);
        DetailActivityDataModel bindingModel = MiscUtils.getDetailModelFromDBModel(this, mModel, day, metric, forecastToDisplayIndex);
        Log.d(getClass().getName(), bindingModel.tempMain+" "+bindingModel.tempMin+" "+bindingModel.tempMax);
        mBinding.setDataModel(bindingModel);
        TimelineRecyclerViewAdapter adapter = new TimelineRecyclerViewAdapter(mModel.getIcon_id(), mModel.getTempList(), this);
        mBinding.timelineRecyclerView.setAdapter(adapter);
        startPostponedEnterTransition();
    }
}
