package com.kodatos.cumulonimbus;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Fade;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.apihelper.SyncOWMService;
import com.kodatos.cumulonimbus.databinding.ActivityMainBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.uihelper.CurrentWeatherLayoutDataModel;
import com.kodatos.cumulonimbus.uihelper.MainRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.MiscUtils;
import com.kodatos.cumulonimbus.utils.UVIndexActivity;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MainRecyclerViewAdapter.ForecastItemClickListener {

    private static final int LOADER_ID = 301;

    private ActivityMainBinding mBinding;
    private MainRecyclerViewAdapter mAdapter = null;

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences weatherSharedPreferences;

    private int previousBackgroundColor;
    private int hiddenLayoutHeight;
    private boolean justOpened = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbarMain);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mBinding.currentLayout.currentHiddenLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                hiddenLayoutHeight = mBinding.currentLayout.currentHiddenLayout.getHeight();
                mBinding.currentLayout.currentHiddenLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mBinding.currentLayout.currentHiddenLayout.setVisibility(View.GONE);
            }
        });

        Fade fade = new Fade();
        fade.setDuration(1000);
        fade.addTarget(mBinding.currentLayout.currentLayoutAlwaysVisible);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        getWindow().setReenterTransition(fade);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherSharedPreferences = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

        previousBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(getString(R.string.current_weather_icon_id_key), "01d"));
        changeBackgroundColor(previousBackgroundColor);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.forecastLayout.mainRecyclerview.setLayoutManager(lm);
        mAdapter = new MainRecyclerViewAdapter(this);
        mAdapter.setHasStableIds(true);
        mBinding.forecastLayout.mainRecyclerview.setAdapter(mAdapter);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 140);
        }
        else
            getSupportLoaderManager().initLoader(LOADER_ID,null,this);

        setUpUIInteractions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                break;
            case R.id.action_refresh:
                mBinding.mainUISwipeRefreshLayout.setRefreshing(true);
                startSync(1);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpUIInteractions() {
        mBinding.mainUISwipeRefreshLayout.setOnRefreshListener(() -> startSync(1));

        /* Enable animated view hiding by clicking on expand arrow */
        mBinding.currentLayout.expandArrow.setOnClickListener(v -> {
            //Decide on the visibility and alpha to achieve
            int toVisibility = mBinding.currentLayout.currentHiddenLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            float toAlpha = toVisibility == View.GONE ? 0.0f : 1.0f;

            mBinding.currentLayout.currentHiddenLayout.animate().alpha(toAlpha).setDuration(500).setListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (toVisibility == View.VISIBLE)
                        //Make visible before animating alpha
                        mBinding.currentLayout.currentHiddenLayout.setVisibility(toVisibility);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (toVisibility == View.GONE) {
                        //Set visibility as gone after animating alpha to 0 and bring back translated views
                        mBinding.currentLayout.currentHiddenLayout.setVisibility(toVisibility);
                        mBinding.forecastCard.setTranslationY(0);
                        v.setTranslationY(0);
                    }
                    //Rotate arrow at end
                    v.animate().rotation(v.getRotation() == 180 ? 0 : 180);
                }
            }).setUpdateListener(animation -> {
                //Calculate instantaneous displacement from original position
                float translation = toVisibility == View.VISIBLE ? -hiddenLayoutHeight + animation.getAnimatedFraction() * hiddenLayoutHeight : -animation.getAnimatedFraction() * hiddenLayoutHeight;
                mBinding.forecastCard.setTranslationY(translation);
                v.setTranslationY(translation);
            });
        });

        mBinding.currentLayout.currentShadesImageView.setOnClickListener(v -> {
            Intent intent = new Intent(this, UVIndexActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }

    public boolean getConnectionStatus(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        return info!=null && info.isConnectedOrConnecting();
    }

    public void startSync(final int action){
        if(!getConnectionStatus()){
            MiscUtils.displayDialogMessage(this, "No Internet Connection Available", "An internet connection is needed for updating. Try again later", action == 0);
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            return;
        }
        Intent intent = new Intent(this, SyncOWMService.class);
        if(action==0)
            intent.setAction(SyncOWMService.CREATE_ACTION);
        else if(action==1)
            intent.setAction(SyncOWMService.UPDATE_ACTION);
        startService(intent);
    }

    /*
        LoaderCallback Overrides
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==LOADER_ID){
            Uri uri = WeatherDBContract.WeatherDBEntry.CONTENT_URI;
            return new CursorLoader(this, uri, null, null, null, null);
        } else
            throw new UnsupportedOperationException("Such a loader not implemented");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0){
            startSync(0);
        }
        else {
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            mAdapter.swapCursor(data);
            bindCurrentWeatherData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==140){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getSupportLoaderManager().initLoader(LOADER_ID,null,this);
            }
            else
                MiscUtils.displayDialogMessage(this, "Location Required", "This app cannot run without location permissions", true);
        }
    }

    @Override
    public void onForecastItemClick(int position, ImageView forecastImageView) {
        ArrayList<DBModel> intentModels = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(defaultSharedPreferences.getLong(getString(R.string.last_update_date_key), 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        int startingRow = 8 - (calendar.get(Calendar.HOUR_OF_DAY) / 3);
        startingRow += position * 8;
        for (int i = startingRow; i < startingRow + 8; i++)
            intentModels.add(mAdapter.getDBModelFromCursor(i));

        forecastImageView.setTransitionName(String.valueOf(position)+"forecast_image");
        String imageTransitionName =  String.valueOf(position)+"forecast_image";
        Intent intent = new Intent(this, WeatherDetailActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(forecastImageView, imageTransitionName),
                Pair.create(mBinding.forecastCard, mBinding.forecastCard.getTransitionName())
                );
        intent.putExtra(getString(R.string.weather_detail_parcel_name), Parcels.wrap(intentModels));
        intent.putExtra(getString(R.string.weather_detail_day_name), position + 1);
        intent.putExtra(getString(R.string.forecats_image_transistion_key), imageTransitionName);
        startActivity(intent, options.toBundle());
    }

    private void bindCurrentWeatherData(){
        DBModel intermediateModel = mAdapter.getDBModelFromCursor(0);
        boolean metric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        long visibility = weatherSharedPreferences.getLong(getString(R.string.current_weather_visibility), 0);
        SimpleDateFormat sf = new SimpleDateFormat("H:mm", Locale.getDefault());
        sf.setTimeZone(TimeZone.getDefault());
        long sunriseInMillis = weatherSharedPreferences.getLong(getString(R.string.current_weather_sunrise_key), 0)*1000;
        long sunsetInMillis = weatherSharedPreferences.getLong(getString(R.string.current_weather_sunset_key), 0)*1000;
        Date sunriseDate = new Date(sunriseInMillis);
        String sunrise = sf.format(sunriseDate);
        String sunset = sf.format(new Date(sunsetInMillis));
        String lastUpdated = MiscUtils.getLastUpdatedStringFromMillis(System.currentTimeMillis(), defaultSharedPreferences.getLong(getString(R.string.last_update_date_key), 0));
        String locationAndBoolean = weatherSharedPreferences.getString(getString(R.string.location_name_key), "Delhi,IN/false");
        CurrentWeatherLayoutDataModel layoutDataModel = MiscUtils.getCurrentWeatherDataFromDBModel(this, intermediateModel, metric, visibility, sunrise, sunset, lastUpdated, locationAndBoolean);
        mBinding.setCurrentWeatherData(layoutDataModel);
        int updatedBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(getString(R.string.current_weather_icon_id_key), "01d"));
        if (!justOpened)
            changeBackgroundColorWithAnimation(updatedBackgroundColor);
        else
            justOpened = false;

        mBinding.forecastLayout.forecastHeading.setTextColor(updatedBackgroundColor);
        mBinding.mainUISwipeRefreshLayout.setColorSchemeColors(MiscUtils.getIconTint(this, intermediateModel.getIcon_id()));
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, updatedBackgroundColor);
        setTaskDescription(taskDescription);
    }

    private void changeBackgroundColor(int updatedBackgroundColor) {
        mBinding.toolbarMain.setBackgroundColor(updatedBackgroundColor);
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
}
