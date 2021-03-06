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

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.apihelper.ServiceErrorContract;
import com.kodatos.cumulonimbus.apihelper.SyncOWMService;
import com.kodatos.cumulonimbus.databinding.ActivityMainBinding;
import com.kodatos.cumulonimbus.datahelper.DBModelsAndDataPresenter;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.uihelper.InfoDialogFragment;
import com.kodatos.cumulonimbus.uihelper.MainActivityDataModel;
import com.kodatos.cumulonimbus.uihelper.adapters.MainRecyclerViewAdapter;
import com.kodatos.cumulonimbus.uihelper.adapters.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.KeyConstants;
import com.kodatos.cumulonimbus.utils.MiscUtils;
import com.kodatos.cumulonimbus.utils.ServiceLocator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MainRecyclerViewAdapter.ParentCallback {

    private static final int LOADER_ID = 301;
    private static final int PERMISSION_REQUEST_ID = 140;
    private static final int ENABLE_LOCATION_REQUEST_ID = 2043;
    private static final int WELCOME_ACTIVITY_REQUEST_ID = 230;
    private static final int SETTINGS_ACTIVITY_REQUEST_ID = 1201;
    private static final int LOCATION_PICKER_ACTIVITY_REQUEST_ID = 1292;
    private static final String LOG_TAG = "Main Activity";

    private ActivityMainBinding mBinding;
    private MainRecyclerViewAdapter forecastAdapter = null;
    private TimelineRecyclerViewAdapter currentTimelineAdapter = null;

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences weatherSharedPreferences;
    private ServiceErrorBroadcastReceiver mErrorReceiver;

    private DBModelsAndDataPresenter mDataPresenter = null;

    private int previousBackgroundColor;
    private int iconTintColor;
    private int hiddenLayoutHeight;
    private boolean justOpened = true;
    private int todayForecastCount;
    private boolean mIsMetric = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbarMain);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherSharedPreferences = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

        mDataPresenter = new DBModelsAndDataPresenter();
        /*if (savedInstanceState != null) {
            mDataPresenter.populateModelsFromParcel(savedInstanceState.getParcelableArrayList("bundled_data"));
        }*/

        if (defaultSharedPreferences.getBoolean(KeyConstants.FIRST_TIME_RUN, true)) {
            Intent introActivityIntent = new Intent(this, WelcomeActivity.class);
            startActivityForResult(introActivityIntent, WELCOME_ACTIVITY_REQUEST_ID);
        } else {
            initialize();
        }

    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mErrorReceiver, new IntentFilter(ServiceErrorContract.BROADCAST_INTENT_FILTER));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mErrorReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (forecastAdapter != null)
            forecastAdapter.unregisterCallback();
        super.onDestroy();
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
                openSettingsActivity();
                break;
            case R.id.action_refresh:
                mBinding.mainUISwipeRefreshLayout.setRefreshing(true);
                startSync(1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WELCOME_ACTIVITY_REQUEST_ID)
            if (resultCode == RESULT_OK)
                initialize();
            else
                finish();
        else if (requestCode == SETTINGS_ACTIVITY_REQUEST_ID || requestCode == LOCATION_PICKER_ACTIVITY_REQUEST_ID)
            if (resultCode == RESULT_OK) {
                mBinding.mainUISwipeRefreshLayout.setRefreshing(true);
                startSync(1);
            }
    }

    private void initialize() {
        //Calculate height of hidden layout
        mBinding.currentLayout.currentHiddenLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                hiddenLayoutHeight = mBinding.currentLayout.currentHiddenLayout.getHeight();
                mBinding.currentLayout.currentHiddenLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mBinding.currentLayout.currentHiddenLayout.setVisibility(View.GONE);
            }
        });

        //region Region : enter exit transitions
        Fade fade = new Fade();
        fade.setDuration(500);
        fade.addTarget(mBinding.currentLayout.currentLayoutAlwaysVisible);
        fade.addTarget(mBinding.forecastLayout.getRoot());
        getWindow().setExitTransition(fade);
        getWindow().setReenterTransition(fade);

        Transition sharedElementTransition = TransitionInflater.from(this).inflateTransition(R.transition.detail_shared_transition);
        getWindow().setSharedElementExitTransition(sharedElementTransition);
        getWindow().setSharedElementReenterTransition(sharedElementTransition);
        //endregion

        //previousBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(KeyConstants.CURRENT_WEATHER_ICON_ID_KEY, ""));

        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mBinding.forecastLayout.mainRecyclerview.setLayoutManager(lm);
        forecastAdapter = new MainRecyclerViewAdapter(this);
        mBinding.forecastLayout.mainRecyclerview.setAdapter(forecastAdapter);

        LinearLayoutManager hlm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.forecastLayout.currentTimelineRecyclerView.setLayoutManager(hlm);
        currentTimelineAdapter = new TimelineRecyclerViewAdapter(null, null, Integer.MIN_VALUE, null);
        mBinding.forecastLayout.currentTimelineRecyclerView.setAdapter(currentTimelineAdapter);

        mErrorReceiver = new ServiceErrorBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mErrorReceiver, new IntentFilter(ServiceErrorContract.BROADCAST_INTENT_FILTER));

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[]{0, 0});
        float px = getResources().getDimension(R.dimen.common_card_corner_radius) * (getResources().getDisplayMetrics().density);
        gradientDrawable.setCornerRadii(new float[]{px, px, px, px, 0, 0, 0, 0});
        mBinding.forecastCard.setBackground(gradientDrawable);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        setUpUIInteractions();
    }

    // Implement necessary listeners to UI elements
    private void setUpUIInteractions() {
        //Sync on swipe
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

        View.OnClickListener infoDialogEnabledViewListener = v -> {
            if (v.getId() == R.id.locationTextView && !defaultSharedPreferences.getBoolean(getString(R.string.pref_curr_location_key), false)) {
                Intent locationPickerIntent = new Intent(this, LocationPickerActivity.class);
                startActivityForResult(locationPickerIntent, LOCATION_PICKER_ACTIVITY_REQUEST_ID);
                return;
            }
            InfoDialogFragment infoDialogFragment = createInfoDialog(v.getId());
            if (infoDialogFragment != null)
                infoDialogFragment.show(getSupportFragmentManager(), "INFO_DIALOG_FRAGMENT");
        };

        for (View view : new View[]{mBinding.currentLayout.currentTemperatureImageView, mBinding.currentLayout.locationTextView, mBinding.currentLayout.currentRainImageView,
                mBinding.currentLayout.currentWindImageView, mBinding.currentLayout.currentShadesImageView}) {
            view.setOnClickListener(infoDialogEnabledViewListener);
        }

        mBinding.currentLayout.currentMeterImageView.setOnClickListener(v -> {
            Intent graphIntent = new Intent(this, GraphsActivity.class);
            graphIntent.putExtra(KeyConstants.TEMPERATURE_CHART_DATA, mDataPresenter.getTemperatureChartData());
            graphIntent.putExtra(KeyConstants.WIND_CHART_DATA, mDataPresenter.getWindChartData(mIsMetric));
            graphIntent.putExtra(KeyConstants.RAIN_CHART_DATA, mDataPresenter.getRainChartData());
            startActivity(graphIntent);
        });

    }

    public InfoDialogFragment createInfoDialog(int viewID) {
        switch (viewID) {
            case R.id.currentTemperatureImageView:
                return ServiceLocator.createFeelsLikeDialog(this, mDataPresenter.getFeelsLikeTemperatureDescription(this, mIsMetric));
            case R.id.currentRainImageView:
                return ServiceLocator.createRainInfoDialog(this);
            case R.id.currentWindImageView:
                return ServiceLocator.createWindInfoDialog(this);
            case R.id.currentShadesImageView:
                return ServiceLocator.createUVInfoDialog(this);
            case R.id.locationTextView:
                return ServiceLocator.createLocationInfoDialog(this, mBinding.mainUISwipeRefreshLayout.isRefreshing());
        }
        return null;
    }

    //Utility method to check internet connection
    private boolean getConnectionStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        return info != null && info.isConnected();
    }

    //Call the service to update data. If action is 0 then create rows, else update
    private void startSync(final int action) {
        if (!getConnectionStatus()) {
            displayDialogMessage("No Internet Connection Available", "An internet connection is needed for updating. Try again later", action == 0);
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

    //region LoaderCallback Overrides
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID) {
            Uri uri = WeatherDBContract.WeatherDBEntry.CONTENT_URI;
            return new CursorLoader(this, uri, null, null, null, null);
        } else
            throw new UnsupportedOperationException("Such a loader not implemented");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) { //If database is empty, start sync
            startSync(0);
        } else if (data.getCount() > 32) {  //Bind new data only when all required rows are updated
            Log.d(LOG_TAG, "change_observed");
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            //Check if already traversed Cursor is provided again
            if (!data.moveToNext())
                data.moveToFirst();
            data.moveToPrevious();
            mDataPresenter.populateModelsFromCursor(data);
            bindCurrentWeatherData();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mDataPresenter.invalidateModels();
        forecastAdapter.setData(null);
        currentTimelineAdapter.setData(null, null);
    }
    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSync(1);
            } else
                displayDialogMessage("Permission Required", "Location permission is required for accessing current location", false);
        }
    }

    // Click listener for daily forecast recyclerview
    @Override
    public void onForecastItemClick(int position, ImageView forecastImageView) {
        //Assign a unique transition name to the image
        forecastImageView.setTransitionName(String.valueOf(position) + "forecast_image");
        String imageTransitionName = String.valueOf(position) + "forecast_image";

        Intent intent = new Intent(this, WeatherDetailActivity.class);
        //Enable shared element transitions
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(forecastImageView, imageTransitionName),
                Pair.create(mBinding.forecastLayout.mainRecyclerview, mBinding.forecastLayout.mainRecyclerview.getTransitionName()),
                Pair.create(mBinding.forecastLayout.currentTimelineCard, mBinding.forecastLayout.currentTimelineCard.getTransitionName())
        );
        intent.putParcelableArrayListExtra(KeyConstants.WEATHER_DETAIL_PARCEL_NAME, mDataPresenter.getModelsForOneForecastDay(position, todayForecastCount));
        intent.putExtra(KeyConstants.WEATHER_DETAIL_DAY_NAME, position + 1);
        intent.putExtra(KeyConstants.FORECAST_IMAGE_TRANSITION_KEY, imageTransitionName);
        startActivity(intent, options.toBundle());
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        ActivityCompat.startActivityForResult(this, intent, SETTINGS_ACTIVITY_REQUEST_ID, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    //region Region : Data Binding

    //Evaluate and bind data to layout while changing other UI elements on sync.
    private void bindCurrentWeatherData() {
        DBModel intermediateModel = mDataPresenter.getCurrentWeatherModel();

        SimpleDateFormat sf = new SimpleDateFormat("H:mm", Locale.getDefault());
        sf.setTimeZone(TimeZone.getDefault());

        //Generate all time related strings
        mIsMetric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        long visibility = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_VISIBILITY_KEY, 0);
        long sunriseInMillis = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_SUNRISE_KEY, 0) * 1000;
        long sunsetInMillis = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_SUNSET_KEY, 0) * 1000;
        String sunrise = sf.format(new Date(sunriseInMillis));
        String sunset = sf.format(new Date(sunsetInMillis));
        long lastUpdatedInMillis = defaultSharedPreferences.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0);
        String lastUpdated = MiscUtils.getLastUpdatedStringFromMillis(System.currentTimeMillis(), lastUpdatedInMillis);
        String locationAndBoolean = weatherSharedPreferences.getString(KeyConstants.LOCATION_NAME_KEY, "Delhi/false");

        //Generate and bind data
        MiscUtils.CurrentWeatherExtraData currentWeatherExtraData = new MiscUtils.CurrentWeatherExtraData(visibility, sunrise, sunset, lastUpdated, locationAndBoolean);
        MainActivityDataModel layoutDataModel = MiscUtils.getCurrentWeatherDataFromDBModel(this, intermediateModel, mIsMetric, currentWeatherExtraData);
        mBinding.setCurrentWeatherData(layoutDataModel);

        int updatedBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, intermediateModel.getIcon_id());
        if (!justOpened) {
            changeBackgroundColorWithAnimation(updatedBackgroundColor);
        } else {
            //If just opened, do not animate
            changeBackgroundColor(updatedBackgroundColor);
            previousBackgroundColor = updatedBackgroundColor;
            justOpened = false;
        }

        //Change colors of swipe refresh layout and recents menu color
        iconTintColor = MiscUtils.getIconTint(this, intermediateModel.getIcon_id());
        mBinding.mainUISwipeRefreshLayout.setColorSchemeColors(iconTintColor);
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, updatedBackgroundColor);
        setTaskDescription(taskDescription);

        //Fade out splash image at first run
        if (mBinding.placeholderImageView.getVisibility() == View.VISIBLE) {
            mBinding.placeholderImageView.setVisibility(View.GONE);
        }

        if (mDataPresenter.isEmpty())
            forecastAdapter.setData(null);
        else {
            forecastAdapter.setData(mDataPresenter.getForecastModelForEachDay(this, mIsMetric, lastUpdatedInMillis));
        }

        //Find how many forecast records are for today
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(defaultSharedPreferences.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        todayForecastCount = 8 - (calendar.get(Calendar.HOUR_OF_DAY) / 3);
        bindCurrentTimeline();

    }

    private void bindCurrentTimeline() {
        List<String> iconIds = new ArrayList<>();
        List<String> temperatures = new ArrayList<>();
        //Generate timeline data
        for (DBModel dbModel : mDataPresenter.getAllAvailableModelsForToday(todayForecastCount)) {
            iconIds.add(dbModel.getIcon_id());
            temperatures.add(dbModel.getTemp());
        }
        currentTimelineAdapter.setData(iconIds, temperatures);
    }

    //endregion

    //region Region : Color Changers

    //Utility method to change background color of the activity
    private void changeBackgroundColor(int updatedBackgroundColor) {
        mBinding.toolbarMain.setBackgroundColor(updatedBackgroundColor);
        getWindow().getDecorView().setBackgroundColor(updatedBackgroundColor);
        getWindow().setStatusBarColor(updatedBackgroundColor);
        getWindow().setNavigationBarColor(updatedBackgroundColor);
        int cardLighterColor = Color.rgb(Color.red(updatedBackgroundColor) + 25, Color.green(updatedBackgroundColor) + 25, Color.blue(updatedBackgroundColor) + 25);
        int cardDarkerColor = Color.rgb(Color.red(updatedBackgroundColor) + 5, Color.green(updatedBackgroundColor) + 5, Color.blue(updatedBackgroundColor) + 5);
        ((GradientDrawable) mBinding.forecastCard.getBackground()).setColors(new int[]{cardDarkerColor, cardLighterColor});
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
    //endregion

    //region Region : Message pop-ups for User

    private void displayDialogMessage(String title, String message, final boolean kill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    if (kill) finish();
                });
        AlertDialog dialog = builder.create();
        if (kill) {
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    //Utility method to show snackbar with the given message and an action, if it is not null
    private void showSnackbarForError(String message, @Nullable String action, @Nullable View.OnClickListener onClickAction) {
        Log.d(LOG_TAG, "snackbar shown");
        Snackbar snackbar = Snackbar.make(mBinding.mainCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        if (action != null)
            snackbar.setAction(action, onClickAction).setActionTextColor(iconTintColor);
        snackbar.show();
    }

    //endregion

    //region Region : Broadcast Receiver

    /*
        Custom broadcast receiver that receives resolvable errors from the background service, checks them
        and notifies the user to take action, if any.
     */

    private class ServiceErrorBroadcastReceiver extends BroadcastReceiver {

        private String ERROR_LOG_TAG = "error_receiver";

        private String errorType;
        private String errorDetails;
        private String message;

        //Flag to indicate whether refresh animation should be stopped
        private boolean shouldStopRefreshingAnim = true;
        //Flag to check if an error is being resolved so as to suppress new errors for the duration
        private boolean isResolvingError = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            errorType = intent.getStringExtra(ServiceErrorContract.SERVICE_ERROR_TYPE);
            errorDetails = intent.getStringExtra(ServiceErrorContract.SERVICE_ERROR_DETAILS);
            if (isResolvingError) {
                Log.d(ERROR_LOG_TAG, "suppressing error: " + errorType);
                return;             //Suppress any errors if already resolving one.
            }
            isResolvingError = true;
            Log.d(ERROR_LOG_TAG, "error received : " + errorType + " " + errorDetails);
            shouldStopRefreshingAnim = true;
            if (ServiceErrorContract.ERROR_DETAILS_IO.equals(errorDetails)) {
                handleIOError();
            } else {
                switch (errorType) {
                    case ServiceErrorContract.ERROR_LOCATION:
                        handleLocationError();
                        break;
                    case ServiceErrorContract.ERROR_REVERSE_GEOCODER:
                        handleReverseGeocodingError();
                        break;
                    case ServiceErrorContract.ERROR_RESPONSE:
                        handleAPIResponseError();
                        break;
                    case ServiceErrorContract.ERROR_NO_PERMISSION:
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
                        break;
                    case ServiceErrorContract.ERROR_NO_LOCATION_CHOSEN:
                        handleNoLocationChosenError();
                        break;
                }
            }
            mBinding.mainUISwipeRefreshLayout.setRefreshing(!shouldStopRefreshingAnim);
            isResolvingError = false;
        }

        private void handleNoLocationChosenError() {
            if (ServiceErrorContract.ERROR_DETAILS_NULL.equals(errorDetails)) {
                message = "No custom location has been chosen by you. Please choose or add one";
                showSnackbarForError(message, "Choose Location", v -> openSettingsActivity());
            }
        }

        private void handleReverseGeocodingError() {
            if (ServiceErrorContract.ERROR_DETAILS_NULL.equals(errorDetails)) {
                message = "Couldn't get the name for your coordinates. Try refreshing later";
                showSnackbarForError(message, null, null);
            }
        }

        private void handleAPIResponseError() {
            int apiErrorCode = Integer.parseInt(errorDetails.split(":")[0]);
            String title = "Response Error";
            String message = "Weather data contained an error.";
            String action = "Show More";
            String dialogMessage;
            switch (apiErrorCode) {
                case 401:
                    dialogMessage = "Invalid API key. Contact developer to resolve soon";
                    break;
                case 404:
                    dialogMessage = "Couldn't find your city. Change it in settings, if possible";
                    break;
                default:
                    dialogMessage = errorDetails.split(":")[1];
            }
            showSnackbarForError(message, action, v -> displayDialogMessage(title, dialogMessage, false));
        }

        private void handleIOError() {
            message = "Had an unfortunate network error. Please try again";
            showSnackbarForError(message, null, null);
        }

        @SuppressLint("MissingPermission")
        //Permissions already handled in onCreate. Will never reach here if not given
        private void handleLocationError() {
            if (errorDetails.contains(ServiceErrorContract.ERROR_DETAILS_NULL)) {
                FusedLocationProviderClient mFusedClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setNumUpdates(1).setInterval(100).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                SettingsClient client = LocationServices.getSettingsClient(MainActivity.this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnSuccessListener(MainActivity.this, locationSettingsResponse -> {
                    Log.d(ERROR_LOG_TAG, "location_enabled");
                    mFusedClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult.getLocations().size() > 0) {
                                //Location successfully retrieved
                                Log.d(LOG_TAG, "location_requested");
                                shouldStopRefreshingAnim = false;               //Another sync is started here. Don't stop refresh animation
                                startSync(errorDetails.contains(SyncOWMService.CREATE_ACTION) ? 0 : 1);
                            } else {
                                //Ask user to enter a custom location if location couldn't be found
                                message = "Retrieving your current location gave no results. Please try again or set a custom location";
                                showSnackbarForError(message, "Set Location", v -> openSettingsActivity());
                            }
                            mFusedClient.removeLocationUpdates(this);
                        }
                    }, null);
                });
                task.addOnFailureListener(MainActivity.this, e -> {
                    Log.d(ERROR_LOG_TAG, "location_disabled");
                    if (e instanceof ResolvableApiException) {
                        ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                        message = "Location provider is disabled. Please enable it";
                        showSnackbarForError(message, "Open Settings", v -> {
                            try {
                                resolvableApiException.startResolutionForResult(MainActivity.this, ENABLE_LOCATION_REQUEST_ID);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                        });
                    } else {
                        Log.d(ERROR_LOG_TAG, "Location disabled but not resolvable");
                    }
                });

            }
        }
    }
    //endregion
}