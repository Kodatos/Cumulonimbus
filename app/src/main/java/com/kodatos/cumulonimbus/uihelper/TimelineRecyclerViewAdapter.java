package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.ForecastTimelineRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimelineRecyclerViewAdapter extends RecyclerView.Adapter<TimelineRecyclerViewAdapter.TimelineRecyclerViewHolder> {

    private String[] iconIds;
    private String[] temperatures;
    private Context mContext;
    private long lastUpdated;


    public TimelineRecyclerViewAdapter(String iconIdString, String temperatureString, Context context) {
        iconIds = iconIdString.split("/");
        temperatures = temperatureString.split("/");
        mContext = context;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        lastUpdated = sp.getLong(mContext.getString(R.string.last_update_date_key), 0);
    }

    @Override
    public TimelineRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ForecastTimelineRecyclerviewItemBinding binding = ForecastTimelineRecyclerviewItemBinding.inflate(inflater, parent, false);
        return new TimelineRecyclerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TimelineRecyclerViewHolder holder, int position) {
        String iconIdAtPosition = iconIds[position];
        String temperatureAtPosition = temperatures[position];
        int imageId = MiscUtils.getResourceIDForIconID(mContext, iconIdAtPosition);
        String displayTemperature = String.valueOf(temperatureAtPosition) + "\u00B0";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String time = sdf.format(getCalendarHour(position));
        holder.bind(time, displayTemperature, imageId, isCurrentlyExpanded(position, lastUpdated));
    }

    @Override
    public int getItemCount() {
        return 8;
    }

    private Date getCalendarHour(int position) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.HOUR, position * 3);
        return calendar.getTime();
    }

    private boolean isCurrentlyExpanded(int position, long lastUpdated) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.setTime(new Date(lastUpdated));
        return calendar.get(Calendar.HOUR_OF_DAY) / 3 == position;
    }

    public class TimelineRecyclerViewHolder extends RecyclerView.ViewHolder {

        ForecastTimelineRecyclerviewItemBinding binding;

        public TimelineRecyclerViewHolder(ForecastTimelineRecyclerviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String time, String temperature, int imageID, boolean isCurrentlyExpanded) {
            binding.timelineTimeView.setText(time);
            binding.timelineTemperatureView.setText(temperature);
            binding.timelineWeatherImageView.setImageDrawable(mContext.getDrawable(imageID));
            if (isCurrentlyExpanded)
                binding.getRoot().setBackgroundColor(Color.parseColor("#f5f5f5"));
        }
    }

}
