package com.kodatos.cumulonimbus;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ActivityWeatherDetailBinding;
import com.kodatos.cumulonimbus.uihelper.DetailActivityDataModel;
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
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        getWindow().setReenterTransition(fade);

        setSupportActionBar(mBinding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.weatherImageView.setTransitionName(getIntent().getStringExtra(getString(R.string.forecats_image_transistion_key)));
        mModel = Parcels.unwrap(getIntent().getParcelableExtra(getString(R.string.weather_detail_parcel_name)));
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
        int imageId = getResources().getIdentifier("ic_"+mModel.getIcon_id().split("/")[forecastToDisplayIndex],"drawable", getPackageName());
        int iconTint = getIconTint(mModel.getIcon_id().split("/")[forecastToDisplayIndex]);
        DetailActivityDataModel bindingModel = MiscUtils.getDetailModelfromDBModel(mModel, day, imageId, iconTint, metric, forecastToDisplayIndex);
        Log.d(getClass().getName(), bindingModel.tempMain+" "+bindingModel.tempMin+" "+bindingModel.tempMax);
        mBinding.setDataModel(bindingModel);
        startPostponedEnterTransition();
    }

    private int getIconTint(String iconID){
        int colorRID = R.color.colorAccent;
        if("01d".equals(iconID))
            colorRID = R.color._01d_icon_tint;
        else if("01n".equals(iconID))
            colorRID = R.color._01n_icon_tint;
        else if(iconID.contains("02"))
            colorRID = R.color._02d_icon_tint;
        else if(iconID.contains("03"))
            colorRID = R.color._03d_icon_tint;
        else if(iconID.contains("04"))
            colorRID = R.color._04d_icon_tint;
        else if(iconID.contains("09") || iconID.contains("10"))
            colorRID = R.color._09d_icon_tint;
        else if(iconID.contains("11"))
            colorRID = R.color._11d_icon_tint;
        else if(iconID.contains("13"))
            colorRID = R.color._13d_icon_tint;
        else if(iconID.contains("50"))
            colorRID = R.color._50d_icon_tint;
        return ContextCompat.getColor(this, colorRID);
    }
}
