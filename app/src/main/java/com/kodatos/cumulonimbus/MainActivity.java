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
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Fade;
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
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.apihelper.ServiceErrorContract;
import com.kodatos.cumulonimbus.apihelper.SyncOWMService;
import com.kodatos.cumulonimbus.databinding.ActivityMainBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.uihelper.CurrentWeatherLayoutDataModel;
import com.kodatos.cumulonimbus.uihelper.MainRecyclerViewAdapter;
import com.kodatos.cumulonimbus.uihelper.TimelineRecyclerViewAdapter;
import com.kodatos.cumulonimbus.utils.KeyConstants;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MainRecyclerViewAdapter.ParentCallback {

    private static final int LOADER_ID = 301;
    private static final int PERMISSION_REQUEST_ID = 140;
    private static final int ENABLE_LOCATION_REQUEST_ID = 2043;
    private static final String LOG_TAG = "Main Activity";

    private ActivityMainBinding mBinding;
    private MainRecyclerViewAdapter forecastAdapter = null;
    private TimelineRecyclerViewAdapter currentTimelineAdapter = null;

    private SharedPreferences defaultSharedPreferences;
    private SharedPreferences weatherSharedPreferences;
    private ServiceErrorBroadcastReceiver mErrorReceiver;
    private Cursor mCursor = null;

    private int previousBackgroundColor;
    private int iconTintColor;
    private int hiddenLayoutHeight;
    private boolean justOpened = true;
    private int todayForecastCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.toolbarMain);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        getWindow().setReturnTransition(fade);
        getWindow().setReenterTransition(fade);
        //endregion

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        weatherSharedPreferences = getSharedPreferences("weather_display_pref", MODE_PRIVATE);

        previousBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(KeyConstants.CURRENT_WEATHER_ICON_ID_KEY, ""));

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBinding.forecastLayout.mainRecyclerview.setLayoutManager(lm);
        LinearLayoutManager hlm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.forecastLayout.currentTimelineRecyclerView.setLayoutManager(hlm);
        forecastAdapter = new MainRecyclerViewAdapter(this);
        forecastAdapter.setHasStableIds(true);
        mBinding.forecastLayout.mainRecyclerview.setAdapter(forecastAdapter);

        mErrorReceiver = new ServiceErrorBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mErrorReceiver, new IntentFilter(ServiceErrorContract.BROADCAST_INTENT_FILTER));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_ID);
        } else
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        setUpUIInteractions();
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

        mBinding.currentLayout.currentShadesImageView.setOnClickListener(v -> {
            //Open UV info activity
            Intent intent = new Intent(this, UVIndexActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        });
    }

    //Utility method to check internet connection
    public boolean getConnectionStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm != null ? cm.getActiveNetworkInfo() : null;
        return info != null && info.isConnectedOrConnecting();
    }

    //Call the service to update data. If action is 0 then create rows, else update
    public void startSync(final int action) {
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
        if (data.getCount() == 0) { //If database is empty, start sync
            startSync(0);
        } else if (data.getCount() > 32) {  //Bind new data only when all required rows are updated
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
            mCursor = data;
            forecastAdapter.notifyDataSetChanged();
            if (null == mCursor || mCursor.getCount() < 33)
                forecastAdapter.setCount(0);
            else
                forecastAdapter.setCount(4);
            bindCurrentWeatherData();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }
    //endregion

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
            } else
                displayDialogMessage("Location Required", "This app cannot run without location permissions", true);
        }
    }

    // Click listener for daily forecast recyclerview
    @Override
    public void onForecastItemClick(int position, ImageView forecastImageView) {
        ArrayList<DBModel> intentModels = new ArrayList<>();

        int startingRow = todayForecastCount + (position * 8);

        //Add all 8 models to the list
        for (int i = startingRow; i < startingRow + 8; i++)
            intentModels.add(getDBModelFromCursor(i));

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
        intent.putExtra(KeyConstants.WEATHER_DETAIL_PARCEL_NAME, Parcels.wrap(intentModels));
        intent.putExtra(KeyConstants.WEATHER_DETAIL_DAY_NAME, position + 1);
        intent.putExtra(KeyConstants.FORECAST_IMAGE_TRANSITION_KEY, imageTransitionName);
        startActivity(intent, options.toBundle());
    }

    @Override
    public DBModel getDBModelFromCursor(int position) {
        mCursor.moveToPosition(position);
        long id = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry._ID));
        String main = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN));
        String desc = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC));
        String temp = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP));
        float temp_min = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN));
        float temp_max = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX));
        float pressure = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE));
        String wind = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WIND));
        long humidity = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY));
        long clouds = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS));
        String icon_id = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID));
        double uvIndex = mCursor.getDouble(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX));
        double rain_3h = mCursor.getDouble(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_RAIN_3H));
        return new DBModel(id, main, desc, temp, temp_min, temp_max, pressure, humidity, wind, clouds, icon_id, uvIndex, rain_3h);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    //region Region : Data Binding

    //Evaluate and bind data to layout while changing other UI elements on sync.
    private void bindCurrentWeatherData() {
        DBModel intermediateModel = getDBModelFromCursor(0);

        SimpleDateFormat sf = new SimpleDateFormat("H:mm", Locale.getDefault());
        sf.setTimeZone(TimeZone.getDefault());

        //Generate all time related strings
        boolean metric = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_metrics_key), true);
        long visibility = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_VISIBILITY_KEY, 0);
        long sunriseInMillis = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_SUNRISE_KEY, 0) * 1000;
        long sunsetInMillis = weatherSharedPreferences.getLong(KeyConstants.CURRENT_WEATHER_SUNSET_KEY, 0) * 1000;
        String sunrise = sf.format(new Date(sunriseInMillis));
        String sunset = sf.format(new Date(sunsetInMillis));
        String lastUpdated = MiscUtils.getLastUpdatedStringFromMillis(System.currentTimeMillis(), defaultSharedPreferences.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0));
        String locationAndBoolean = weatherSharedPreferences.getString(KeyConstants.LOCATION_NAME_KEY, "Delhi/false");

        //Generate and bind data
        CurrentWeatherLayoutDataModel layoutDataModel = MiscUtils.getCurrentWeatherDataFromDBModel(this, intermediateModel, metric, visibility, sunrise, sunset, lastUpdated, locationAndBoolean);
        mBinding.setCurrentWeatherData(layoutDataModel);

        //Find how many forecast records are for today
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(defaultSharedPreferences.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0)));
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        todayForecastCount = 8 - (calendar.get(Calendar.HOUR_OF_DAY) / 3);
        bindCurrentTimeline();

        int updatedBackgroundColor = MiscUtils.getBackgroundColorForIconID(this, weatherSharedPreferences.getString(KeyConstants.CURRENT_WEATHER_ICON_ID_KEY, "01d"));
        if (!justOpened)
            changeBackgroundColorWithAnimation(updatedBackgroundColor);
        else {
            //If just opened, do not animate
            changeBackgroundColor(updatedBackgroundColor);
            justOpened = false;
        }

        //Change colors of swipe refresh layout and recents menu color
        iconTintColor = MiscUtils.getIconTint(this, intermediateModel.getIcon_id());
        mBinding.mainUISwipeRefreshLayout.setColorSchemeColors(iconTintColor);
        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), null, updatedBackgroundColor);
        setTaskDescription(taskDescription);

        //Fade out splash image at first run
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

        //Generate timeline data
        for (int i = 1; i < todayForecastCount; i++) {
            DBModel dbModel = getDBModelFromCursor(i);
            iconIds.add(dbModel.getIcon_id());
            temperatures.add(dbModel.getTemp());
        }

        //Initialize timeline adapter when needed, and set new data
        if (currentTimelineAdapter == null) {
            currentTimelineAdapter = new TimelineRecyclerViewAdapter(this, iconIds, temperatures, Integer.MIN_VALUE);
            currentTimelineAdapter.setHasStableIds(true);
            mBinding.forecastLayout.currentTimelineRecyclerView.setAdapter(currentTimelineAdapter);
        } else {
            currentTimelineAdapter.setData(iconIds, temperatures);
        }
    }

    //endregion

    //region Region : Color Changers

    //Utility method to change background color of the activity
    private void changeBackgroundColor(int updatedBackgroundColor) {
        mBinding.toolbarMain.setBackgroundColor(updatedBackgroundColor);
        getWindow().getDecorView().setBackgroundColor(updatedBackgroundColor);
        getWindow().setStatusBarColor(updatedBackgroundColor);
        getWindow().setNavigationBarColor(updatedBackgroundColor);
        int cardLighterColor = Color.rgb(Color.red(updatedBackgroundColor) + 30, Color.green(updatedBackgroundColor) + 30, Color.blue(updatedBackgroundColor) + 30);
        int cardDarkerColor = Color.rgb(Color.red(updatedBackgroundColor) + 5, Color.green(updatedBackgroundColor) + 5, Color.blue(updatedBackgroundColor) + 5);
        //mBinding.forecastCard.setCardBackgroundColor(specialBackgroundColor);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[]{cardDarkerColor, cardLighterColor});
        float px = getResources().getDimension(R.dimen.common_card_corner_radius) * (getResources().getDisplayMetrics().density);
        gradientDrawable.setCornerRadius(px);
        mBinding.forecastCard.setBackground(gradientDrawable);
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
        Custom broadcast receiver that receives resolvable errors from the background service
        and notifies the user to take action, if any.
     */

    private class ServiceErrorBroadcastReceiver extends BroadcastReceiver {

        private String errorType;
        private String errorDetails;
        private String message;


        @Override
        public void onReceive(Context context, Intent intent) {
            errorType = intent.getStringExtra(ServiceErrorContract.SERVICE_ERROR_TYPE);
            errorDetails = intent.getStringExtra(ServiceErrorContract.SERVICE_ERROR_DETAILS);
            Log.d(LOG_TAG, "error received : " + errorType + " " + errorDetails);
            switch (errorType) {
                case ServiceErrorContract.ERROR_LOCATION:
                    handleLocationError();
                    break;
                case ServiceErrorContract.ERROR_GEOCODER:
                    handleGeocodingError();
                    break;
                case ServiceErrorContract.ERROR_REVERSE_GEOCODER:
                    handleReverseGeocodingError();
                    break;

                case ServiceErrorContract.ERROR_RESPONSE:
                    handleAPIResponseError();
                    break;
            }
            mBinding.mainUISwipeRefreshLayout.setRefreshing(false);
        }

        private void handleGeocodingError() {
            if (ServiceErrorContract.ERROR_DETAILS_NULL.equals(errorDetails)) {
                message = "Couldn't find the custom location you entered. Please try again or change it";
                showSnackbarForError(message, "Change Location", v -> openSettingsActivity());
            } else if (ServiceErrorContract.ERROR_DETAILS_IO.equals(errorDetails)) {
                handleIOError();
            }
        }

        private void handleReverseGeocodingError() {
            if (ServiceErrorContract.ERROR_DETAILS_NULL.equals(errorDetails)) {
                message = "Couldn't get the name for your coordinates. Try refreshing later";
                showSnackbarForError(message, null, null);
            } else if (ServiceErrorContract.ERROR_DETAILS_IO.equals(errorDetails)) {
                handleIOError();
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
                    Log.d(LOG_TAG, "location_enabled");
                    mFusedClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult.getLocations().size() > 0) {
                                //Location successfully retrieved
                                Log.d(LOG_TAG, "location_requested");
                                message = "Location was not available but issue may be resolved now. Try again";
                                showSnackbarForError(message, "Refresh", v -> startSync(errorDetails.contains(SyncOWMService.CREATE_ACTION) ? 0 : 1));
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
                    Log.d(LOG_TAG, "location_disabled");
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
                        Log.d(LOG_TAG, "Location disabled but not resolvable");
                    }
                });

            } else if (ServiceErrorContract.ERROR_DETAILS_IO.equals(errorDetails)) {
                handleIOError();
            }
        }
    }
    //endregion
}