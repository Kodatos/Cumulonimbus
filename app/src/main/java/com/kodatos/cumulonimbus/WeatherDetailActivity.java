package com.kodatos.cumulonimbus;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kodatos.cumulonimbus.databinding.ActivityWeatherDetailBinding;

public class WeatherDetailActivity extends AppCompatActivity {

    private ActivityWeatherDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_weather_detail);
    }
}
