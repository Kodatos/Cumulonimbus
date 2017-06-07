package com.kodatos.cumulonimbus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.kodatos.cumulonimbus.apihelper.SyncOWMService;
import com.kodatos.cumulonimbus.databinding.ActivityMainBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.uihelper.MainRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{

    private ActivityMainBinding mBinding;
    private MainRecyclerViewAdapter mAdapter = null;
    private RecyclerView mRecyclerView;
    private SharedPreferences mSharedPreferences;
    private static final int LOADER_ID = 301;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        Toolbar toolbar = mBinding.toolbar;
        setSupportActionBar(toolbar);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        /*if(sharedPreferences.getBoolean("first_run", true)){
            boolean connectionresult = startSync(0);
            if(!connectionresult){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("No Internet Connection Available")
                        .setMessage("An internet connection is needed before starting the app for the first time. Try opening it with internet")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MainActivity.this.finish();
                            }
                        });
                builder.create().show();
            }
            sharedPreferences.edit().putBoolean("First_run", false).apply();
        }
        else {
            boolean b = startSync(1);
            if(!b){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("No Internet Connection Available")
                        .setMessage("An internet connection is needed for updating. Try again later")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        }*/
        mRecyclerView = mBinding.testMainRecyclerview;
        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(lm);
        mAdapter = new MainRecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(LOADER_ID,null,this);
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
                startActivity(intent);
                break;
            case R.id.action_refresh:
                startSync(1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public boolean getConnectionStatus(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info!=null && info.isConnectedOrConnecting();
    }

    public void startSync(final int action){
        if(!getConnectionStatus()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("No Internet Connection Available")
                    .setMessage("An internet connection is needed for updating. Try again later")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if(action==0)
                                finish();
                        }
                    });
            builder.create().show();
            return;
        }
        Intent intent = new Intent(this, SyncOWMService.class);
        if(action==0)
            intent.setAction(SyncOWMService.CREATE_ACTION);
        else if(action==1)
            intent.setAction(SyncOWMService.UPDATE_ACTION);
        startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id==LOADER_ID){
            Uri uri = WeatherDBContract.WeatherDBEntry.CONTENT_URI;
            return new CursorLoader(this, uri, null, null, null, null);
        }
        else
            throw new UnsupportedOperationException("Such a loader not implemented");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount()==0){
            startSync(0);
        }
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_custom_location_key))){
            startSync(1);
            Toast.makeText(this, "Updated location to "+sharedPreferences.getString(key, "NULL"), Toast.LENGTH_SHORT).show();
        }
    }
}
