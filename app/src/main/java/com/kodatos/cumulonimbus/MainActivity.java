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
import android.graphics.Color;
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
import android.support.v4.graphics.ColorUtils;
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
import com.kodatos.cumulonimbus.uihelper.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.MiscUtils;
import com.kodatos.cumulonimbus.utils.UVIndexActivity;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MainRecyclerViewAdapter.ForecastItemClickListener {

    private static final int LOADER_ID = 301;
    private static final int PERMISSION_REQUEST_ID = 140;

    private ActivityMainBinding mBinding;
    private MainRecyclerViewAdapter forecastAdapter = null;
    private TimelineRecyclerViewAdapter currentTimelineAdapter = null;

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences weatherSharedPreferences;

    private int previousBackgroundColor;
    private int hiddenLayoutHeight;
    private boolean justOpened = true;
    private int todayForecastCount;

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

        previousBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(getString(R.string.current_weather_icon_id_key), ""));
        //changeBackgroundColor(previousBackgroundColor);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.forecastLayout.mainRecyclerview.setLayoutManager(lm);
        LinearLayoutManager hlm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.forecastLayout.currentTimelineRecyclerView.setLayoutManager(hlm);
        forecastAdapter = new MainRecyclerViewAdapter(this);
        forecastAdapter.setHasStableIds(true);
        mBinding.forecastLayout.mainRecyclerview.setAdapter(forecastAdapter);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
        } else
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);

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

    // Implement necessary listeners to UI elements
    private void setUpUIInteractions() {
        mBinding.mainUISwipeRefreshLayout.setOnRefreshListener(() -> startSync(1));

        //Enable animated view hiding by clicking on expand arrow
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

    public boolean getConnectionStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        return info != null && info.isConnectedOrConnecting();
    }

    public void startSync(final int action) {
        if (!getConnectionStatus()) {
            MiscUtils.displayDialogMessage(this, "No Internet Connection Available", "An internet connection is needed for updating. Try again later", action == 0);
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            return;
        }
        Intent intent = new Intent(this, SyncOWMService.class);
        if (action == 0)
            intent.setAction(SyncOWMService.CREATE_ACTION);
        else if (action == 1)
            intent.setAction(SyncOWMService.UPDATE_ACTION);
        startService(intent);
    }

    //LoaderCallback Overrides
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            Uri uri = WeatherDBContract.WeatherDBEntry.CONTENT_URI;
            return new CursorLoader(this, uri, null, null, null, null);
        } else
            throw new UnsupportedOperationException("Such a loader not implemented");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            startSync(0);
        } else if (data.getCount() > 32) {
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            forecastAdapter.swapCursor(data);
            bindCurrentWeatherData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            } else
                MiscUtils.displayDialogMessage(this, "Location Required", "This app cannot run without location permissions", true);
        }
    }

    @Override
    public void onForecastItemClick(int position, ImageView forecastImageView) {
        ArrayList<DBModel> intentModels = new ArrayList<>();

        //Generate string that corresponds to the date
        /*Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(defaultSharedPreferences.getLong(getString(R.string.last_update_date_key), 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        int startingRow = 8 - (calendar.get(Calendar.HOUR_OF_DAY) / 3);*/
        int startingRow = todayForecastCount + (position * 8);

        //Add all 8 models to the list
        for (int i = startingRow; i < startingRow + 8; i++)
            intentModels.add(forecastAdapter.getDBModelFromCursor(i));

        //Assign a unique transition name to the image
        forecastImageView.setTransitionName(String.valueOf(position) + "forecast_image");
        String imageTransitionName = String.valueOf(position) + "forecast_image";
        Intent intent = new Intent(this, WeatherDetailActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(forecastImageView, imageTransitionName),
                Pair.create(mBinding.forecastLayout.mainRecyclerview, mBinding.forecastLayout.mainRecyclerview.getTransitionName()),
                Pair.create(mBinding.forecastLayout.currentTimelineCard, mBinding.forecastLayout.currentTimelineCard.getTransitionName())
        );
        intent.putExtra(getString(R.string.weather_detail_parcel_name), Parcels.wrap(intentModels));
        intent.putExtra(getString(R.string.weather_detail_day_name), position + 1);
        intent.putExtra(getString(R.string.forecats_image_transistion_key), imageTransitionName);
        startActivity(intent, options.toBundle());
    }

    //Evaluate and bind data to layout while changing other UI elements on sync.
    private void bindCurrentWeatherData() {
        DBModel intermediateModel = forecastAdapter.getDBModelFromCursor(0);

        SimpleDateFormat sf = new SimpleDateFormat("H:mm", Locale.getDefault());
        sf.setTimeZone(TimeZone.getDefault());

        boolean metric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        long visibility = weatherSharedPreferences.getLong(getString(R.string.current_weather_visibility), 0);
        long sunriseInMillis = weatherSharedPreferences.getLong(getString(R.string.current_weather_sunrise_key), 0) * 1000;
        long sunsetInMillis = weatherSharedPreferences.getLong(getString(R.string.current_weather_sunset_key), 0) * 1000;
        String sunrise = sf.format(new Date(sunriseInMillis));
        String sunset = sf.format(new Date(sunsetInMillis));
        String lastUpdated = MiscUtils.getLastUpdatedStringFromMillis(System.currentTimeMillis(), defaultSharedPreferences.getLong(getString(R.string.last_update_date_key), 0));
        String locationAndBoolean = weatherSharedPreferences.getString(getString(R.string.location_name_key), "Delhi/false");

        CurrentWeatherLayoutDataModel layoutDataModel = MiscUtils.getCurrentWeatherDataFromDBModel(this, intermediateModel, metric, visibility, sunrise, sunset, lastUpdated, locationAndBoolean);
        mBinding.setCurrentWeatherData(layoutDataModel);

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(defaultSharedPreferences.getLong(getString(R.string.last_update_date_key), 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        todayForecastCount = 8 - (calendar.get(Calendar.HOUR_OF_DAY) / 3);
        bindCurrentTimeline();

        int updatedBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(getString(R.string.current_weather_icon_id_key), "01d"));
        if (!justOpened)
            changeBackgroundColorWithAnimation(updatedBackgroundColor);
        else {
            changeBackgroundColor(updatedBackgroundColor);
            justOpened = false;
        }

        int specialBackgroundColor = ColorUtils.setAlphaComponent(updatedBackgroundColor, 175);
        specialBackgroundColor = Color.rgb(Color.red(specialBackgroundColor) + 10, Color.green(specialBackgroundColor) + 10, Color.blue(specialBackgroundColor) + 10);
        mBinding.forecastCard.setCardBackgroundColor(specialBackgroundColor);

        mBinding.mainUISwipeRefreshLayout.setColorSchemeColors(MiscUtils.getIconTint(this, intermediateModel.getIcon_id()));
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, updatedBackgroundColor);
        setTaskDescription(taskDescription);

        if (mBinding.placeholderImageView.getVisibility() == View.VISIBLE) {
            mBinding.placeholderImageView.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBinding.placeholderImageView.setVisibility(View.GONE);
                    super.onAnimationEnd(animation);
                }
            });
        }
    }

    private void bindCurrentTimeline() {
        List<String> iconIds = new ArrayList<>();
        List<String> temperatures = new ArrayList<>();

        for (int i = 1; i < todayForecastCount; i++) {
            DBModel dbModel = forecastAdapter.getDBModelFromCursor(i);
            iconIds.add(dbModel.getIcon_id());
            temperatures.add(dbModel.getTemp());
        }

        if (currentTimelineAdapter == null) {
            currentTimelineAdapter = new TimelineRecyclerViewAdapter(this, iconIds, temperatures, Integer.MIN_VALUE);
            mBinding.forecastLayout.currentTimelineRecyclerView.setAdapter(currentTimelineAdapter);
        } else {
            currentTimelineAdapter.setIconIds(iconIds);
            currentTimelineAdapter.setTemperatures(temperatures);
            currentTimelineAdapter.notifyDataSetChanged();
        }
    }

    //Utility method to change background color of the activity
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
