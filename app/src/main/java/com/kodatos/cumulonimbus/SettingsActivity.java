package com.kodatos.cumulonimbus;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;

import com.kodatos.cumulonimbus.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        setSupportActionBar(mBinding.toolbarSettings);

        Slide transition = new Slide();
        transition.setDuration(200);
        transition.setSlideEdge(Gravity.RIGHT);
        getWindow().setEnterTransition(transition);
        getWindow().setExitTransition(transition);
        getWindow().setReturnTransition(transition);
        getWindow().setReenterTransition(transition);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }
}
