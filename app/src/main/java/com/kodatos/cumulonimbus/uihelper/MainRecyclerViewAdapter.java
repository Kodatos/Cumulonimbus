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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kodatos.cumulonimbus.R;
import com.kodatos.cumulonimbus.apihelper.DBModel;
import com.kodatos.cumulonimbus.databinding.ForecastRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.utils.KeyConstants;
import com.kodatos.cumulonimbus.utils.MiscUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder> {

    private Context mContext;
    private ParentCallback parentCallback;
    private int count;


    public MainRecyclerViewAdapter (Context context){
        mContext = context;
        try {
            parentCallback = (ParentCallback) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public MainRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ForecastRecyclerviewItemBinding recycleItemBinding = ForecastRecyclerviewItemBinding.inflate(layoutInflater, parent, false);
        return new MainRecyclerViewHolder(recycleItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewHolder holder, int position) {
        DBModel dbModel = parentCallback.getDBModelFromCursor((getProperPositionForCursor(position)));
        holder.bind(dbModel);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public long getItemId(int position) {
        return parentCallback.getDBModelFromCursor(getProperPositionForCursor(position)).getId();
    }

    private int getProperPositionForCursor(int position) {
        //Since current weather is excluded, the first row of cursor is skipped
        //Also, every 8th row after first row is the required data for the upcoming days to be displayed on main screen
        return ((position + 1) * 8);
    }

    public interface ParentCallback {
        void onForecastItemClick(int position, ImageView forecastImageView);

        DBModel getDBModelFromCursor(int position);
    }

    public class MainRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ForecastRecyclerviewItemBinding binding;

        MainRecyclerViewHolder(ForecastRecyclerviewItemBinding recycleItemBinding) {
            super(recycleItemBinding.getRoot());
            this.binding = recycleItemBinding;
            binding.getRoot().setOnClickListener(this);
        }

        // Method to create a calculated data model and bind it to the layout
        public void bind(DBModel dbModel) {
            int offset = getAdapterPosition() + 1;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_WEEK, offset);
            String displayDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
            boolean metric = sp.getBoolean(mContext.getString(R.string.pref_metrics_key), true);
            calendar.setTime(new Date(sp.getLong(KeyConstants.LAST_UPDATE_DATE_KEY, 0)));
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            int imageId = MiscUtils.getResourceIDForIconID(mContext, dbModel.getIcon_id());
            DBModelCalculatedData calculatedData = new DBModelCalculatedData(imageId, MiscUtils.makeTemperaturePretty(dbModel.getTemp(), metric), displayDay, dbModel.getWeather_main(), dbModel.getWeather_desc());
            binding.setCalculateddata(calculatedData);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View view) {
            parentCallback.onForecastItemClick(getAdapterPosition(), binding.forecastImage);
        }
    }
}
