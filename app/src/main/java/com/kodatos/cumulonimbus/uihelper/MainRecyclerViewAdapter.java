package com.kodatos.cumulonimbus.uihelper;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ForecastRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder>{

    private Cursor mCursor = null;
    private Context mContext;

    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ForecastRecyclerviewItemBinding binding;

        public MainRecyclerViewHolder(ForecastRecyclerviewItemBinding recycleItemBinding) {
            super(recycleItemBinding.getRoot());
            this.binding = recycleItemBinding;
        }

        // Method to create a calculated data model and bind it and given model to the layout
        public void bind(DBModel dbModel){
            binding.setDbmodel(dbModel);
            int offset = getAdapterPosition()+1;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_WEEK, offset);
            String displayDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean metric = sp.getBoolean(mContext.getString(R.string.pref_metrics_key), true);
            int imageId = mContext.getResources().getIdentifier("ic_"+dbModel.getIcon_id(),"drawable",mContext.getPackageName());
            DBModelCalculatedData calculatedData = new DBModelCalculatedData(imageId, MiscUtils.makeTemperaturePretty(dbModel.getTemp()), displayDay);
            binding.setCalculateddata(calculatedData);
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
        DBModel dbModel = getDBModelFromCursor(position+1);         //Since current weather is excluded, the first row of cursor is skipped
        holder.bind(dbModel);
    }

    @Override
    public int getItemCount() {
        if(null==mCursor)
            return 0;
        return mCursor.getCount()-1;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(0);
    }

    /**
     * Creates and returns a model object from the member Cursor to send to binding.
     * @param position Position for which data is required
     * @return A DBModel object containing required display data
     */
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
        double uvIndex = mCursor.getDouble(mCursor.getColumnIndex(WeatherDBContract.WeatherDBEntry.COLUMN_UV_INDEX));
        return new DBModel(id,main,desc,temp,temp_min,temp_max,pressure,humidity,wind,clouds,icon_id,uvIndex);
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }
}
