package com.kodatos.cumulonimbus.uihelper;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.Locale;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ForecastRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder>{

    private Cursor mCursor = null;
    private Context mContext;

    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ForecastRecyclerviewItemBinding binding;

        public MainRecyclerViewHolder(ForecastRecyclerviewItemBinding recycleItemBinding) {
            super(recycleItemBinding.getRoot());
            this.binding = recycleItemBinding;
        }

        public void bind(DBModel dbModel){
            binding.setDbmodel(dbModel);
            int offset = getAdapterPosition()+1;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_WEEK, offset);
            binding.dayTextview.setText(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean metric = sp.getBoolean(mContext.getString(R.string.pref_metrics_key), true);
            binding.forecastTempTextview.setText(dbModel.getUsefulTemp(metric));
            int imageid = mContext.getResources().getIdentifier("_"+dbModel.getIcon_id(),"drawable",mContext.getPackageName());
            binding.forecastImage.setImageDrawable(mContext.getDrawable(imageid));
            binding.executePendingBindings();
        }
    }

    public MainRecyclerViewAdapter (Context context){
        mContext = context;
    }

    @Override
    public MainRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ForecastRecyclerviewItemBinding recycleItemBinding = ForecastRecyclerviewItemBinding.inflate(layoutInflater, parent, false);
        return new MainRecyclerViewHolder(recycleItemBinding);
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewHolder holder, int position) {
        DBModel dbModel = getDBModelFromCursor(position);
        holder.bind(dbModel);
    }

    @Override
    public int getItemCount() {
        if(null==mCursor)
            return 0;
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(0);
    }

    private DBModel getDBModelFromCursor(int position) {
        mCursor.moveToPosition(position);
        long id = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry._ID));
        String main = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_MAIN));
        String desc = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WEATHER_DESC));
        float temp = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP));
        float temp_min = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MIN));
        float temp_max = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_TEMP_MAX));
        float pressure = mCursor.getFloat(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_PRESSURE));
        String wind = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_WIND));
        long humidity = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_HUMIDITY));
        long clouds = mCursor.getLong(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_CLOUDS));
        String icon_id = mCursor.getString(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_ICON_ID));
        return new DBModel(id,main,desc,temp,temp_min,temp_max,pressure,humidity,wind,clouds,icon_id);
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
