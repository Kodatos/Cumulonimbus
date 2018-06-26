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


import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kodatos.cumulonimbus.databinding.ForecastRecyclerviewItemBinding;
import com.kodatos.cumulonimbus.utils.AdapterDiffUtils;

import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.MainRecyclerViewHolder> {

    private ParentCallback parentCallback;

    private List<ForecastCalculatedData> modelList = null;

    public MainRecyclerViewAdapter (ParentCallback callback){
        parentCallback = callback;
    }

    @NonNull
    @Override
    public MainRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ForecastRecyclerviewItemBinding recycleItemBinding = ForecastRecyclerviewItemBinding.inflate(layoutInflater, parent, false);
        return new MainRecyclerViewHolder(recycleItemBinding, parentCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull MainRecyclerViewHolder holder, int position) {
        ForecastCalculatedData model = modelList.get(position);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return modelList == null ? 0 : 4;
    }

    public void setData(List<ForecastCalculatedData> modelList){

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new AdapterDiffUtils.MainRVDiffCallback(this.modelList, modelList));
        this.modelList = modelList;
        result.dispatchUpdatesTo(this);
        //notifyDataSetChanged();
    }

    public void unregisterCallback(){
        parentCallback = null;
    }

    public interface ParentCallback {
        void onForecastItemClick(int position, ImageView forecastImageView);
    }

    static class MainRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ForecastRecyclerviewItemBinding binding;

        MainRecyclerViewHolder(ForecastRecyclerviewItemBinding recycleItemBinding, ParentCallback parentCallback) {
            super(recycleItemBinding.getRoot());
            this.binding = recycleItemBinding;
            binding.getRoot().setOnClickListener(v -> parentCallback.onForecastItemClick(getAdapterPosition(), binding.forecastImage));
        }

        // Method to create a calculated data model and bind it to the layout
        public void bind(ForecastCalculatedData model) {
            binding.setCalculateddata(model);
            binding.executePendingBindings();
        }
    }
}
