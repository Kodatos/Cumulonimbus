package com.kodatos.cumulonimbus.uihelper;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ForecastRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.datahelper.WeatherDBContract;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder> implements View.OnClickListener{

    private Cursor mCursor = null;
    private Context mContext;
    private ForecastItemClickListener itemClickListener;


    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ForecastRecyclerviewItemBinding binding;

        public MainRecyclerViewHolder(ForecastRecyclerviewItemBinding recycleItemBinding) {
            super(recycleItemBinding.getRoot());
            this.binding = recycleItemBinding;
            binding.getRoot().setOnClickListener(this);
        }

        // Method to create a calculated data model and bind it to the layout
        public void bind(DBModel dbModel){
            int offset = getAdapterPosition()+1;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_WEEK, offset);
            String displayDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean metric = sp.getBoolean(mContext.getString(R.string.pref_metrics_key), true);
            calendar.setTime(new Date(sp.getLong(mContext.getString(R.string.last_update_date_key),0)));
            int forecastToDisplayIndex = (calendar.get(Calendar.HOUR_OF_DAY)/3)-1;
            int imageId = mContext.getResources().getIdentifier("ic_"+dbModel.getIcon_id().split("/")[forecastToDisplayIndex],"drawable",mContext.getPackageName());
            DBModelCalculatedData calculatedData = new DBModelCalculatedData(imageId, MiscUtils.makeTemperaturePretty(dbModel.getTempList().split("/")[forecastToDisplayIndex], metric), displayDay, dbModel.getWeather_main(), dbModel.getWeather_desc());
            binding.setCalculateddata(calculatedData);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onForecastItemClick(getDBModelFromCursor(getAdapterPosition()+1), getAdapterPosition()+1, binding.forecastImage);
        }
    }

    public MainRecyclerViewAdapter (Context context){
        mContext = context;
        try {
            itemClickListener = (ForecastItemClickListener)context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MainRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ForecastRecyclerviewItemBinding recycleItemBinding = ForecastRecyclerviewItemBinding.inflate(layoutInflater, parent, false);
        recycleItemBinding.itemConstraintLayout.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {

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
        return new DBModel(id,main,desc,temp,temp_min,temp_max,pressure,humidity,wind,clouds,icon_id,uvIndex,rain_3h);
    }

    public void swapCursor(Cursor newCursor){
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public interface ForecastItemClickListener{
        void onForecastItemClick(DBModel intentModel, int position, ImageView forecastImageView);
    }
}
