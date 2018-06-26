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

package com.kodatos.cumulonimbus.uihelper;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.databinding.ForecastTimelineRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.utils.AdapterDiffUtils;
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

    private TimelineItemClickListener timelineItemClickListener = null;

    private int expandedPosition;

    public TimelineRecyclerViewAdapter(List<String> iconIds, List<String> temperatures, int expandedPosition, TimelineItemClickListener timelineItemClickListener) {
        this.iconIds = iconIds;
        this.temperatures = temperatures;
        if (expandedPosition != Integer.MIN_VALUE)
            this.timelineItemClickListener = timelineItemClickListener;
        this.expandedPosition = expandedPosition;
    }

    @NonNull
    @Override
    public TimelineRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ForecastTimelineRecyclerviewItemBinding binding = ForecastTimelineRecyclerviewItemBinding.inflate(inflater, parent, false);
        TimelineRecyclerViewHolder holder = new TimelineRecyclerViewHolder(binding);
        if (timelineItemClickListener != null) {
            holder.binding.getRoot().setOnClickListener(v -> updateExpandedPosition(holder.getAdapterPosition()));
        }
        return holder;
    }

    private void updateExpandedPosition(int newPosition) {
        if (newPosition == expandedPosition)
            return;
        int previousExpandedPosition = expandedPosition;
        expandedPosition = newPosition;
        timelineItemClickListener.onTimelineItemClick(newPosition);
        notifyItemChanged(previousExpandedPosition);
        notifyItemChanged(expandedPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineRecyclerViewHolder holder, int position) {
        String iconIdAtPosition = iconIds.get(position);
        String temperatureAtPosition = temperatures.get(position);
        String displayTemperature = temperatureAtPosition + "\u00B0";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String time = sdf.format(getCalendarHour(position));

        holder.bind(time, displayTemperature, iconIdAtPosition, position == expandedPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineRecyclerViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads);
        else {
            String iconID = null;
            String temperature = null;
            Bundle payload = (Bundle) payloads.get(0);
            for(String key : payload.keySet()){
                if("icon_id".equals(key)){
                    iconID = payload.getString(key);
                }
                else if("temperature".equals(key)){
                    temperature = payload.getString(key) + "\u00B0";
                }
            }
            holder.partialBind(iconID, temperature);
        }
    }

    @Override
    public int getItemCount() {
        return iconIds == null ? 0 : iconIds.size();
    }

    public void setData(@Nullable List<String> iconIds, @Nullable List<String> temperatures) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new AdapterDiffUtils.TimelineRVDiffCallback(this.iconIds, this.temperatures, iconIds, temperatures));
        this.iconIds = iconIds;
        this.temperatures = temperatures;
        result.dispatchUpdatesTo(this);
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

    static class TimelineRecyclerViewHolder extends RecyclerView.ViewHolder {

        ForecastTimelineRecyclerviewItemBinding binding;

        TimelineRecyclerViewHolder(ForecastTimelineRecyclerviewItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(String time, String temperature, String iconID, boolean isCurrentlyExpanded) {
            binding.timelineTimeView.setText(time);
            binding.timelineTemperatureView.setText(temperature);
            Context context = binding.getRoot().getContext();
            binding.timelineWeatherImageView.setImageDrawable(context.getDrawable(MiscUtils.getResourceIDForIconID(context, iconID)));
            if (isCurrentlyExpanded) {
                binding.getRoot().setBackgroundColor(ContextCompat.getColor(binding.getRoot().getContext(), R.color.whiteOneShadeDarker));
            } else {
                binding.getRoot().setBackgroundColor(Color.WHITE);
            }
        }

        void partialBind(String iconID, String temperature) {
            if (iconID != null) {
                Context context = binding.getRoot().getContext();
                binding.timelineWeatherImageView.setImageDrawable(context.getDrawable(MiscUtils.getResourceIDForIconID(context, iconID)));
            }
            if(temperature != null)
                binding.timelineTemperatureView.setText(temperature);
        }

    }

}
