package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.ForecastTimelineRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimelineRecyclerViewAdapter extends RecyclerView.Adapter<TimelineRecyclerViewAdapter.TimelineRecyclerViewHolder> {

    private List<String> iconIds;
    private List<String> temperatures;

    private Context mContext;
    private TimelineItemClickListener timelineItemClickListener;

    private int expandedPosition;

    public TimelineRecyclerViewAdapter(Context context, List<String> iconIds, List<String> temperatures, int expandedPosition) {
        this.iconIds = iconIds;
        this.temperatures = temperatures;
        mContext = context;
        if (expandedPosition != Integer.MIN_VALUE)
            this.timelineItemClickListener = (TimelineItemClickListener) context;
        this.expandedPosition = expandedPosition;
    }

    @Override
    public TimelineRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ForecastTimelineRecyclerviewItemBinding binding = ForecastTimelineRecyclerviewItemBinding.inflate(inflater, parent, false);
        return new TimelineRecyclerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(TimelineRecyclerViewHolder holder, int position) {
        String iconIdAtPosition = iconIds.get(position);
        String temperatureAtPosition = temperatures.get(position);
        int imageId = MiscUtils.getResourceIDForIconID(mContext, iconIdAtPosition);
        String displayTemperature = String.valueOf(temperatureAtPosition) + "\u00B0";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String time = sdf.format(getCalendarHour(position));
        holder.bind(time, displayTemperature, imageId, position == expandedPosition);
    }

    @Override
    public int getItemCount() {
        return iconIds.size();
    }

    @Override
    public long getItemId(int position) {
        return getCalendarHour(position).hashCode();
    }

    public void setData(List<String> iconIds, List<String> temperatures) {
        int itemCountChange = iconIds.size() - getItemCount();
        this.iconIds = iconIds;
        this.temperatures = temperatures;
        if (itemCountChange > 0) {
            notifyItemRangeInserted(0, itemCountChange);
            notifyItemRangeChanged(itemCountChange, iconIds.size());
        } else if (itemCountChange < 0) {
            notifyItemRangeRemoved(0, -itemCountChange);
            notifyItemRangeChanged(0, iconIds.size());
        } else
            notifyDataSetChanged();
    }

    private Date getCalendarHour(int position) {
        int startingPosition = 8 - iconIds.size();
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.HOUR, (startingPosition + position) * 3);
        return calendar.getTime();
    }

    //Callback to containing activity
    public interface TimelineItemClickListener {
        void onTimelineItemClick(int position);
    }

    public class TimelineRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ForecastTimelineRecyclerviewItemBinding binding;

        public TimelineRecyclerViewHolder(ForecastTimelineRecyclerviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            if (expandedPosition != Integer.MIN_VALUE)
                binding.getRoot().setOnClickListener(this);
        }

        public void bind(String time, String temperature, int imageID, boolean isCurrentlyExpanded) {
            binding.timelineTimeView.setText(time);
            binding.timelineTemperatureView.setText(temperature);
            binding.timelineWeatherImageView.setImageDrawable(mContext.getDrawable(imageID));
            if (isCurrentlyExpanded) {
                binding.getRoot().setBackgroundColor(ContextCompat.getColor(mContext, R.color.whiteOneShadeDarker));
            } else {
                binding.getRoot().setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public void onClick(View v) {
            int previousExpandedPosition = expandedPosition;
            expandedPosition = getAdapterPosition();
            timelineItemClickListener.onTimelineItemClick(getAdapterPosition());
            notifyItemChanged(previousExpandedPosition);
            notifyItemChanged(expandedPosition);
        }
    }

}
