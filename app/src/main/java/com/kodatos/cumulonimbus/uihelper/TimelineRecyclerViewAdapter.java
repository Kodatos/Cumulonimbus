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
import android.view.View;
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

    @NonNull
    @Override
    public TimelineRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ForecastTimelineRecyclerviewItemBinding binding = ForecastTimelineRecyclerviewItemBinding.inflate(inflater, parent, false);
        return new TimelineRecyclerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineRecyclerViewHolder holder, int position) {
        String iconIdAtPosition = iconIds.get(position);
        String temperatureAtPosition = temperatures.get(position);
        int imageId = MiscUtils.getResourceIDForIconID(mContext, iconIdAtPosition);
        String displayTemperature = temperatureAtPosition + "\u00B0";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String time = sdf.format(getCalendarHour(position));
        holder.bind(time, displayTemperature, imageId, position == expandedPosition);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineRecyclerViewHolder holder, int position, @NonNull List<Object> payloads) {
        if(payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads);
        else {
            int imageId = Integer.MIN_VALUE;
            String temperature = null;
            Bundle payload = (Bundle) payloads.get(0);
            for(String key : payload.keySet()){
                if("icon_id".equals(key)){
                    imageId = MiscUtils.getResourceIDForIconID(mContext, payload.getString(key));
                }
                else if("temperature".equals(key)){
                    temperature = payload.getString(key) + "\u00B0";
                }
            }
            holder.partialBind(imageId, temperature);
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

    public class TimelineRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ForecastTimelineRecyclerviewItemBinding binding;

        TimelineRecyclerViewHolder(ForecastTimelineRecyclerviewItemBinding binding) {
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

        void partialBind(int imageID, String temperature){
            if(imageID != Integer.MIN_VALUE)
                binding.timelineWeatherImageView.setImageDrawable(mContext.getDrawable(imageID));
            if(temperature != null)
                binding.timelineTemperatureView.setText(temperature);
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
